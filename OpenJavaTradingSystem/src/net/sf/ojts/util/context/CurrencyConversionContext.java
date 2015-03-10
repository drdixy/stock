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
 * Created on 19.04.2005
 *
 */
package net.sf.ojts.util.context;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.functionality.DataAnalysisAndVisualisation;
import net.sf.ojts.functionality.exceptions.OJTSFunctionalityException;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.CurrencyPair;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.DoubleSequenceResult;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer;
import net.sf.ojts.pjdo.DoubleDataItem;
import net.sf.ojts.util.DateUtil;

/**
 * @author cs
 *
 */
public class CurrencyConversionContext {
    
    class CurrencyMap {
        
        protected CurrencyPair currency_pair_ = null;
        
        protected Date[]   conversion_history_dates_   = new Date[0];
        protected double[] conversion_history_factors_ = new double[0];
        
        protected Date     cache_start_ = DateUtil.getToday();
        protected Date     cache_end_   = DateUtil.getToday();
        
        CurrencyMap(Unit original) {
            currency_pair_ = DBAccessConvenience.getCurrencyPair(original, target_currency_);
            if(null == currency_pair_)
                throw new RuntimeException("Did not find currency pair for from currency: [" + original.getName() + "] to currency: " + target_currency_.getName());
        }
        
        DoubleDataItem convert(Date when, double value, DoubleDataItem result) throws OJTSFunctionalityException {
            when = DateUtil.truncate(when);
            result.setDate(when);
            // try to find date in conversion history
            if(when.before(cache_start_)) {
                Date new_cache_start = DateUtil.add(when,-1,0,0,0,0,0,0);// minus one year: read even one more year of data
                // date not found in cache
                ValuesPerDateContainer data = null; 
                try {
                    // get data already in sorted order :)
                    data = DataAnalysisAndVisualisation.getDataForSecurityPaper(data_source_,new_cache_start, cache_end_, currency_pair_, market_, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);
                } catch (Exception e) {
                    throw new OJTSFunctionalityException("In CurrencyConversionContext, cannont convert currency for pair:" + currency_pair_.getName(), e);
                }
                cache_start_ = data.getFirstDate();
                cache_end_   = data.getLastDate();
                
                // throw away old data and replace with new data
                int size = data.size();
                conversion_history_dates_   = new Date[size];
                conversion_history_factors_ = new double[size];
                for(int i = 0; i < size; i++) {
                    conversion_history_dates_[i]   = DateUtil.truncate(data.getItem(i).getDate());
                    conversion_history_factors_[i] = data.getItem(i).getValues()[0];
                }
            }
            if(!when.before(cache_end_)) {
                // we already tried to read the data up to today. if we cannot get the requested data then just
                // predict the future with the last available value:
                when = cache_end_;
                //throw new OJTSFunctionalityException("In CurrencyConversionContext, cannont read data for the future for conversion for currency pair:" + currency_pair_.getName() + " date requested: " + DateUtil.formatDate(when));                
            }
            int index = Arrays.binarySearch(conversion_history_dates_, when);
            if(index < 0) {
                // we need to interpolate
                int index_end   = -1 * (index + 1);
                int index_start = index_end - 1; 
                double tstart = conversion_history_dates_[index_start].getTime();
                double vstart = conversion_history_factors_[index_start];
                double tend   = conversion_history_dates_[index_end].getTime();
                double vend   = conversion_history_factors_[index_end];
                double twhen  = when.getTime(); 
                
                double f      = vstart + ((vend - vstart)/(tend - tstart)) * (twhen - tstart);
                result.setValue(f * value);
                result.setUnit(target_currency_.getName());                
            } else {
                // i can take the direct found data
                double f = conversion_history_factors_[index];
                result.setValue(f * value);
                result.setUnit(target_currency_.getName());                
            }
            return result;
        }        
    }
    
    protected Unit target_currency_ = null;
    
    protected HashMap currency_maps_ = new HashMap();
    
    protected DataSource  data_source_ = null;
    protected MarketPlace market_      = null;        
    
    public CurrencyConversionContext(Unit target, DataSource observer_and_type, MarketPlace market) {
        super();
        target_currency_ = target;
        data_source_     = observer_and_type;
        market_          = market;
    }

    public Unit getTargetCurrency() {
        return target_currency_; 
    }
    
    public DoubleDataItem convert(Date when, double value, Unit original) throws OJTSFunctionalityException {
        DoubleDataItem result = new DoubleDataItem(when, value, original.getName());
        if(original.getName().equals(target_currency_.getName()))
            return result;
        return convert(when, value, original, result);
    }
    
    protected DoubleDataItem convert(Date when, double value, Unit original, DoubleDataItem result) throws OJTSFunctionalityException {
        CurrencyMap cm = (CurrencyMap) currency_maps_.get(original.getName());
        if(null == cm) {
            cm = new CurrencyMap(original);
            currency_maps_.put(original.getName(), cm);
        }

        result = cm.convert(when, value, result);
        
        return result;
    }

    public void convert(DoubleSequence input, DoubleSequenceResult result) throws OJTSFunctionalityException {
        assert result != null && input != null && input.size() == result.size() && input.depth() > 0 && result.resultDepth() > 0 && input.depth() == result.resultDepth();

        DoubleDataItem storage = new DoubleDataItem();
        int depth = input.depth();
        int size  = input.size();
        for(int column = 0; column < depth; column++) {
            Unit original = input.getUnitForColumn(column);
            if(null == original)
                throw new OJTSFunctionalityException("The given unit is null!");
            if(original.getName().equals(target_currency_.getName())) {
                // just copy
                continue;
            }
            
            for(int i = 0; i < size; i++) {
                storage = convert((Date) input.getLabel(i), input.get(i, column), original, storage);
                result.setResult(i, column, storage.getValue());
            }
        }
    }    
}
