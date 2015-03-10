/*
 *  The OpenJavaTradingSystem (http://ojts.sourceforge.net/)  
 *  is meant to be a common infrastructure to develop stock trading 
 *  systems. It consists of four parts:
 *   * the gathering of raw data over the internet
 *   * the recognition of trading signals
 *   * a visualisation module and
 *   * modules to connect to the programmatic interfaces of trading platforms like banks. 
 *  Copyright (C) 2004 Christian Schuhegger, Manuel Gonzalez Berges
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/*
 * Created on 10.04.2005
 *
 */
package net.sf.ojts.functionality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import net.sf.ojts.datainput.exceptions.CannotHandleSubjectException;
import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.exceptions.OJTSFunctionalityException;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.portfolio.Position;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.math.statistics.AggregateFunctions;
import net.sf.ojts.math.types.util.InterpolationStrategy;
import net.sf.ojts.math.types.util.LinearInterpolationStrategy;
import net.sf.ojts.math.types.vpd.ValuesPerDate;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer.VPDDoubleSequence;
import net.sf.ojts.pjdo.DoubleDataItem;
import net.sf.ojts.pjdo.ValueAndUnit;
import net.sf.ojts.util.DateUtil;
import net.sf.ojts.util.context.CurrencyConversionContext;
import net.sf.ojts.util.context.DataInputContext;
import net.sf.ojts.util.context.DefaultContexts;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.PersistenceException;

/**
 * @author cs
 *
 */
public class FNPortfolio {

    public static Position createPosition(DataSource preferred_ds, Date when, SecurityPaper securitypaper, MarketPlace bought_at, double quantity, double price_per_piece, double charges, Unit currency) {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    
        Position result = new Position();
        result.setOpen(when);
		result.setSubject(securitypaper);
        result.setBoughtAt(bought_at);
		result.setQuantity(quantity);
        result.setOpenPrice(price_per_piece);
		result.setOpenCharges(charges);
        result.setOpenPriceCurrency(currency);
        result.setPreferredDataSource(preferred_ds);
		return result;
    }

    public static Position sellPosition(Position position, Date when, MarketPlace sell_at, double price_per_piece, double charges, Unit currency) {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    
        Position result = position;
        result.setClose(when);
        result.setSoldAt(sell_at);
        result.setClosePrice(price_per_piece);
		result.setCloseCharges(charges);
        result.setClosePriceCurrency(currency);
		return result;
    }

    public static Position getPosition(int id) {
        return (Position) DBAccess.getObjectById(Position.class, id);        
    }

    public static Position[] getPositionsForSecurityPaper(SecurityPaper securitypaper, boolean only_open_positions) {
    	Logger log = Logger.getLogger(FNPortfolio.class);
        Position[] result = new Position[0];
        
        String   query            = "SELECT ppos FROM " + Position.class.getName() + " ppos WHERE ppos.subject = $1 ";
        if(only_open_positions)
            query += "and is_undefined(ppos.close)";
        Object[] query_arguments  = {securitypaper};
        try {
            Object[] oresult = DBAccess.getObjectsFromDatabase(query, query_arguments);
            if(null == oresult) return null;
            result = new Position[oresult.length];
            for (int i = 0; i < oresult.length; i++) {
                result[i] = (Position) oresult[i];                
            }
        } catch (PersistenceException e) {
            log.error("A PersistenceException was thrown while trying to retrieve positions for securitypaper: " + securitypaper.getName(), e);
            return null; // null indicates error in contrast to an array with 0 length.
        } catch (Exception e) {
            log.error("Some Exception was thrown while trying to retrieve positions for securitypaper: " + securitypaper.getName(), e);
            return null; // null indicates error in contrast to an array with 0 length.            
        }
        
        return result;
    }

    public static Position fillMissingFieldsInPosition(DataSource observer_and_type, Position position, MarketPlace market) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
        Position result = (Position)position.copy();
        if(null != result.getClose())
            return result; // nothing to be done, this position was already closed some time ago
        // here we calculate the position as if we would have sold it the last trading day
        net.sf.ojts.pjdo.DoubleDataItem current_value = FNPortfolio.getLatestClosingPriceValue(observer_and_type, position.getSubject(), market);
        result.setClose(current_value.getDate());
        result.setSoldAt(market);
        result.setClosePrice(current_value.getValue());
        result.setClosePriceCurrency(DBAccessConvenience.getUnit(current_value.getUnit()));
        
        return result;
    }

    public static net.sf.ojts.pjdo.DoubleDataItem evaluatePositionAbsolutePerPiece(Position position, CurrencyConversionContext currency_context) throws OJTSFunctionalityException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    	net.sf.ojts.pjdo.DoubleDataItem result = null;
        
        if(null == position.getClose()) {
            log.error("Tried to absolutely evaluate position which is not yet closed: " + position.getSubject().getName());
            return null;
        }
        
		double open0 = position.getOpenPrice() + position.getOpenCharges() / position.getQuantity();
		net.sf.ojts.pjdo.DoubleDataItem open  = currency_context.convert(position.getOpen(), open0, position.getOpenPriceCurrency());
		double close0 = position.getClosePrice() + position.getCloseCharges() / position.getQuantity();
		net.sf.ojts.pjdo.DoubleDataItem close = currency_context.convert(position.getClose(), close0, position.getClosePriceCurrency());
        result = new net.sf.ojts.pjdo.DoubleDataItem(position.getClose(), close.getValue() - open.getValue(), currency_context.getTargetCurrency().getName());

		return result;
    }

    public static net.sf.ojts.pjdo.DoubleDataItem evaluatePositionRelative(Position position, CurrencyConversionContext currency_context) throws OJTSFunctionalityException {
        net.sf.ojts.pjdo.DoubleDataItem absolute = evaluatePositionAbsolutePerPiece(position, currency_context);
		double open0 = position.getOpenPrice() + position.getOpenCharges() / position.getQuantity();
		net.sf.ojts.pjdo.DoubleDataItem open  = currency_context.convert(position.getOpen(), open0, position.getOpenPriceCurrency());
        double value0 = absolute.getValue() / open.getValue();
        double value1 = Math.round(value0 * 10000) / 10000.0;
        net.sf.ojts.pjdo.DoubleDataItem result = new net.sf.ojts.pjdo.DoubleDataItem(absolute.getDate(), value1, "%");
        return result;
    }

    public static net.sf.ojts.pjdo.DoubleDataItem evaluatePositionRelativeYearly(Position position, CurrencyConversionContext currency_context) throws OJTSFunctionalityException {
        net.sf.ojts.pjdo.DoubleDataItem absolute = evaluatePositionAbsolutePerPiece(position, currency_context);
		double open0 = position.getOpenPrice() + position.getOpenCharges() / position.getQuantity();
		net.sf.ojts.pjdo.DoubleDataItem open  = currency_context.convert(position.getOpen(), open0, position.getOpenPriceCurrency());
        double value0 = 1.0 + absolute.getValue() / open.getValue();
        double number_of_days = DateUtil.getNumberOfDaysBetween(position.getOpen(), position.getClose()); 
        double pow = 365.0 / number_of_days;
        double yearly = Math.pow(value0, pow) - 1.0;
        double value1 = Math.round(yearly * 10000) / 10000.0;
        net.sf.ojts.pjdo.DoubleDataItem result = new net.sf.ojts.pjdo.DoubleDataItem(absolute.getDate(), value1, getPercentage().getName());
        return result;
    }
	
	protected static Unit percentage_ = null;
	public static Unit getPercentage() {
		if(null != percentage_)
			return percentage_;
		percentage_ = DBAccessConvenience.getUnit("%");
		return percentage_;
	}

    public static net.sf.ojts.pjdo.DoubleDataItem evaluatePositionAbsoluteYearly(Position position,  CurrencyConversionContext currency_context) throws OJTSFunctionalityException {
        net.sf.ojts.pjdo.DoubleDataItem result = evaluatePositionRelativeYearly(position, currency_context);

		double open0 = position.getOpenPrice() + position.getOpenCharges();
		net.sf.ojts.pjdo.DoubleDataItem open  = currency_context.convert(position.getOpen(), open0, position.getOpenPriceCurrency());
		
		// "forecast" what the value that the security paper will have in one year.
        double value = result.getValue() * open.getValue() * position.getQuantity();
        result.setValue(value);
        result.setUnit(currency_context.getTargetCurrency().getName());
        
        return result;
    }

	protected static net.sf.ojts.pjdo.DoubleDataItem[] getStartAndEndValues(DataSource observer_and_type, SecurityPaper subject, MarketPlace market, Date sdate, Date edate, CurrencyConversionContext currency_context) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
	    
    	net.sf.ojts.pjdo.DoubleDataItem svalue0 = FNPortfolio.getFirstClosingPriceValueAfter(observer_and_type, subject, market, sdate);
    	net.sf.ojts.pjdo.DoubleDataItem svalue  = currency_context.convert(sdate,svalue0.getValue(), DBAccessConvenience.getUnit(svalue0.getUnit()));
        net.sf.ojts.pjdo.DoubleDataItem evalue0 = FNPortfolio.getLastClosingPriceValueBefore(observer_and_type, subject, market, edate);
    	net.sf.ojts.pjdo.DoubleDataItem evalue  = currency_context.convert(edate,evalue0.getValue(), DBAccessConvenience.getUnit(evalue0.getUnit()));

		return new net.sf.ojts.pjdo.DoubleDataItem[]{svalue, evalue};
	}
	
    public static ValueAndUnit evaluatePerformanceAbsolute(DataSource observer_and_type, SecurityPaper subject, MarketPlace market, Date sdate, Date edate, CurrencyConversionContext currency_context) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    
		net.sf.ojts.pjdo.DoubleDataItem[] values = getStartAndEndValues(observer_and_type, subject, market, sdate, edate, currency_context); 
    	net.sf.ojts.pjdo.DoubleDataItem svalue  = values[0];
    	net.sf.ojts.pjdo.DoubleDataItem evalue  = values[1];
    
        return new ValueAndUnit(evalue.getValue() - svalue.getValue(), svalue.getUnit());
    }

    public static ValueAndUnit evaluatePerformanceRelative(DataSource observer_and_type, SecurityPaper subject, MarketPlace market, Date sdate, Date edate, CurrencyConversionContext currency_context) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    
		net.sf.ojts.pjdo.DoubleDataItem[] values = getStartAndEndValues(observer_and_type, subject, market, sdate, edate, currency_context); 
    	net.sf.ojts.pjdo.DoubleDataItem svalue  = values[0];
    	net.sf.ojts.pjdo.DoubleDataItem evalue  = values[1];
        
        double value0 = (evalue.getValue() - svalue.getValue()) / svalue.getValue(); 
        double value1 = Math.round(value0 * 10000) / 10000.0;
        
        return new ValueAndUnit(value1, "%");
    }

    public static ValueAndUnit evaluatePerformanceRelativeYearly(DataSource observer_and_type, SecurityPaper subject, MarketPlace market, Date sdate, Date edate, CurrencyConversionContext currency_context) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    
    	double relative0 = 1.0 + evaluatePerformanceRelative(observer_and_type, subject, market, sdate, edate, currency_context).getValue(); 		
    
        double number_of_days = DateUtil.getNumberOfDaysBetween(sdate, edate); 
        double pow = 365.0 / number_of_days;
        
        double value0 = Math.pow(relative0, pow) - 1.0; 
        double value1 = Math.round(value0 * 10000) / 10000.0;
        
        return new ValueAndUnit(value1, "%");
    }

    public static ValueAndUnit evaluatePerformanceAbsoluteYearly(DataSource observer_and_type, SecurityPaper subject, MarketPlace market, Date sdate, Date edate, CurrencyConversionContext currency_context) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    
		net.sf.ojts.pjdo.DoubleDataItem[] values = getStartAndEndValues(observer_and_type, subject, market, sdate, edate, currency_context); 
    	net.sf.ojts.pjdo.DoubleDataItem svalue  = values[0];
    	net.sf.ojts.pjdo.DoubleDataItem evalue  = values[1];
        
        double value0 = 1.0 + (evalue.getValue() - svalue.getValue()) / svalue.getValue(); 
    
        double number_of_days = DateUtil.getNumberOfDaysBetween(sdate, edate); 
        double pow = 365.0 / number_of_days;
        double value1 = Math.pow(value0, pow) - 1.0;
        double value2 = svalue.getValue() * value1;
        
        return new ValueAndUnit(value2, svalue.getUnit());
    }

    public static net.sf.ojts.pjdo.DoubleDataItem getLatestClosingPriceValue(DataSource observer_and_type, SecurityPaper subject, MarketPlace market) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
        return FNPortfolio.getLastClosingPriceValueBefore(observer_and_type, subject, market, DateUtil.truncate(DateUtil.getToday())); 
    }

    public static net.sf.ojts.pjdo.DoubleDataItem getLastClosingPriceValueBefore(DataSource observer_and_type, SecurityPaper subject, MarketPlace market, Date when) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    		
        Date edate = when;
        Date sdate = DateUtil.add(edate, 0, 0, -14, 0, 0, 0, 0);
        
        ValuesPerDateContainer results = DataAnalysisAndVisualisation.getDataForSecurityPaper(observer_and_type, sdate, edate, subject, market, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);
        if(null == results || results.size() == 0)
            return null;
        
        // return the last element from the retrieved values.
        ValuesPerDate value = results.getItem(results.size() - 1); 
        net.sf.ojts.pjdo.DoubleDataItem result = new net.sf.ojts.pjdo.DoubleDataItem(value.getDate(), value.getValues()[0], value.getContainer().getUnitForColumn(0).getName());
        return result;
    }

    public static net.sf.ojts.pjdo.DoubleDataItem getFirstClosingPriceValueAfter(DataSource observer_and_type, SecurityPaper subject, MarketPlace market, Date when) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(FNPortfolio.class);
    		
        Date sdate = when;
        Date edate = DateUtil.add(sdate, 0, 0, +14, 0, 0, 0, 0);
        
        ValuesPerDateContainer results = DataAnalysisAndVisualisation.getDataForSecurityPaper(observer_and_type, sdate, edate, subject, market, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);
        if(null == results || results.size() == 0)
            return null;
        
        // return the first element from the retrieved values.
        ValuesPerDate value = results.getItem(0); 
        net.sf.ojts.pjdo.DoubleDataItem result = new net.sf.ojts.pjdo.DoubleDataItem(value.getDate(), value.getValues()[0], value.getContainer().getUnitForColumn(0).getName());
        return result;
    }
	
	public static ValuesPerDateContainer getClosingPricesForPortfolio(net.sf.ojts.jdo.subject.Portfolio portfolio, int extra_column_size) {
		assert extra_column_size >= 0;
    	Logger log = Logger.getLogger(FNPortfolio.class);
		ValuesPerDateContainer result = null;
		
		ArrayList buys  = new ArrayList();
		ArrayList sells = new ArrayList();
		Position[] positions = portfolio.getPositionsAsArray();
		ValuesPerDateContainer[] intermediate = new ValuesPerDateContainer[positions.length];
		for(int i = 0; i < positions.length; i++) {
			Position position = positions[i];
			position.setSubject((SecurityPaper)DBAccessConvenience.downcastSubject(position.getSubject()));
			DataSource  ds = position.getPreferredDataSource();
			MarketPlace mt = position.getBoughtAt();
			if(null == ds) {
				DataInputContext context = DataInputContext.getContext(position.getSubject().getClass()); 
				ds = context.getDataSource();
				mt = context.getMarketPlace();
			}
	        ValuesPerDateContainer data_closing = null;
			try {
				data_closing = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, position.getOpen(), position.getClose(), position.getSubject(), mt, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE, 0);
				CurrencyConversionContext currency_context = DefaultContexts.getDefaultCurrencyContextCache().getContextForTargetCurrency(portfolio.getDefaultCurrency());
		        int[] currency_column = {0};
		        VPDDoubleSequence currency_conversion_sequence = data_closing.new VPDDoubleSequence(currency_column, currency_column);
				currency_context.convert(currency_conversion_sequence, currency_conversion_sequence);
				data_closing.setUnitForColumn(currency_column[0], currency_context.getTargetCurrency());
				
				AggregateFunctions.constmultiply(currency_conversion_sequence, 0, currency_conversion_sequence, 0, position.getQuantity());
				
				double dbought = position.getOpenPrice() * position.getQuantity() + position.getOpenCharges();
				DoubleDataItem bought = currency_context.convert(position.getOpen(), dbought, position.getOpenPriceCurrency());
				data_closing.getItem(0).getValues()[0] = bought.getValue();
				buys.add(bought);
				if(null != position.getClose()) {
					double dsold = position.getClosePrice() * position.getQuantity() + position.getCloseCharges();
					DoubleDataItem sold = currency_context.convert(position.getClose(), dsold, position.getClosePriceCurrency());
					data_closing.getItem(data_closing.size() - 1).getValues()[0] = sold.getValue();					
					sells.add(sold);
				}
			} catch (Exception ex) {
				// ignore
				log.debug("Exception was thrown while trying to get data for position: " + position.getId() + "," + position.getSubject().getName(), ex);
			}
			if(null == data_closing) {
				data_closing = new ValuesPerDateContainer(1,new Unit[1]);
				data_closing.setUnitForColumn(0, new Unit());
			}
			intermediate[i] = data_closing;
		}
		InterpolationStrategy[] strategies = new InterpolationStrategy[intermediate.length];
		for(int i = 0; i < intermediate.length; i++) {
			if(null == positions[i].getClose())
				strategies[i] = new LinearInterpolationStrategy(LinearInterpolationStrategy.LAST_AVAILABLE_VALUE);
			else
				strategies[i] = new LinearInterpolationStrategy(LinearInterpolationStrategy.TO_ZERO);
		}
		result = ValuesPerDateContainer.merge(intermediate, strategies, extra_column_size + 4);
		result.setUnitForColumn(intermediate.length, portfolio.getDefaultCurrency());
		Iterator iter = buys.iterator();
		while(iter.hasNext()) {
			DoubleDataItem buy = (DoubleDataItem) iter.next();
			ValuesPerDate values = result.getItem(buy.getDate());
			values.getValues()[intermediate.length] += buy.getValue();
		}
		iter = sells.iterator();
		while(iter.hasNext()) {
			DoubleDataItem sell = (DoubleDataItem) iter.next();
			Date selldate = DateUtil.dateOneDayAfter(sell.getDate());
			ValuesPerDate values = result.getItem(selldate);
			if(null == values)
				values = result.addItem(selldate);
			values.getValues()[intermediate.length] -= sell.getValue();
		}

        VPDDoubleSequence input  = result.new VPDDoubleSequence(positions.length,0);
        VPDDoubleSequence output = result.new VPDDoubleSequence(3,intermediate.length + 1);
		result.setUnitForColumn(intermediate.length + 1, portfolio.getDefaultCurrency());
		AggregateFunctions.sum(input, output, 0);
		
        input  = result.new VPDDoubleSequence(result.getValuesSize(),0);
        VPDDoubleSequence buysell  = result.new VPDDoubleSequence(1,intermediate.length);
		AggregateFunctions.relative(input, intermediate.length + 1,  buysell, 0, output, 1);
		
		AggregateFunctions.log(1.05, input, intermediate.length + 2, output, 2);

		return result;
	}

}
