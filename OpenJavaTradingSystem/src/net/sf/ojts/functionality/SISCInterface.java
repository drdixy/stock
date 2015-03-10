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
import java.util.Date;

import net.sf.ojts.datainput.Result;
import net.sf.ojts.datainput.exceptions.CannotHandleSubjectException;
import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.exceptions.OJTSFunctionalityException;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.vpd.OHLCDatasetImpl;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer;
import net.sf.ojts.util.DateUtil;
import net.sf.ojts.util.context.CurrencyConversionContext;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.PersistenceException;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.OHLCDataset;

/**
 * @author cs
 *
 */
public class SISCInterface {

    public static Result fetchData(String observer, String start, String end, String equity, String market) {
    	Logger log = Logger.getLogger(SISCInterface.class);
        //e.g. fetchData("yahoo-csv", "2004-06-27", "2004-09-03", "DE0007500001", "XETRA");
        //a null in the end-date means up to now.
        //e.g. fetchData("yahoo-csv", "2004-06-27", null, "DE0007500001", "XETRA");
    
        // find the equity		
        String        query               = null;
        SecurityPaper eqt                 = DBAccessConvenience.getSecurityPaper(equity);
    	
        // find the marketplace
        MarketPlace marketplace           = DBAccessConvenience.getMarketPlace(market);
    	
        // call the data input handler
        Date sdate = null;
        Date edate = null;
    	try {
    	    sdate = DateUtil.truncate(DateUtil.parseDate(start));
    	    edate = DateUtil.truncate(DateUtil.getToday());
    	    if(null != end)
    	        edate = DateUtil.truncate(DateUtil.parseDate(end));
    	    return DataInput.fetchData(DBAccessConvenience.getDataSource(observer), sdate, edate, eqt, marketplace);
    	} catch (Exception e) {
    		log.error("While trying to fetch data for equity " + equity + " at market " + market + " via observer " + observer + " an exception was thrown!", e);
    	}
    	return null;
    }

    /*
    public static CurrencyConversion[] fetchCurrencyData(String observer, String start, String end, String sfrom, String sto) {
    	Logger log = Logger.getLogger(SISCInterface.class);

    	Unit from = DBAccessConvenience.getUnit(sfrom);
    	Unit to   = DBAccessConvenience.getUnit(sto);
    	
        // call the data input handler
        Date sdate = null;
        Date edate = null;
    	try {
    	    sdate = DateUtil.truncate(DateUtil.parseDate(start));
    	    edate = DateUtil.truncate(DateUtil.getToday());
    	    if(null != end)
    	        edate = DateUtil.truncate(DateUtil.parseDate(end));
    	    return DataInput.fetchCurrencyData(observer, sdate, edate, from, to);
    	} catch (Exception e) {
    		log.error("While trying to fetch data for the currency pair " + sfrom + "," + sto + " from observer " + observer + " an exception was thrown!", e);
    	}
    	return null;
    }

    public static Value schemeEvaluate(String expression) {
    	Logger log = Logger.getLogger(SISCInterface.class);
    
    	Interpreter current = null;//Context.currentInterpreter();
    	Value val = null;
    	
    	try {
            if (null == current) {
            	AppContext ctx = new AppContext();
            	Context.register("appname", ctx);
            	current = Context.enter("appname");
            	ctx.loadEnv(current, new SeekableDataInputStream(new BufferedRandomAccessInputStream("sisc/sisc.shp","r")));
            }
            val = current.eval(expression);
        } catch (IOException e) {
            log.error("While trying to evaluate the scheme expression: " + expression + " an IOException was raised.", e);
        } catch (ClassNotFoundException e) {
            log.error("While trying to evaluate the scheme expression: " + expression + "  ClassNotFoundException was raised.", e);
        } catch (SchemeException e) {
            log.error("While trying to evaluate the scheme expression: " + expression + " a SchemeException was raised.", e);
        }
    	Context.exit();
    	
        return val;
    }
    */

    public static ValuesPerDateContainer getDataForSecurityPaper(String observer, String start, String end, String securitypaper, String market, int what) {
    	Logger log = Logger.getLogger(SISCInterface.class);
    	
        SecurityPaper subject     = DBAccessConvenience.getSecurityPaper(securitypaper);
        if(null == subject) {
            log.error("SecurityPaper: '" + securitypaper + "' is unknown!");
            return null;
        }
        MarketPlace   marketplace = DBAccessConvenience.getMarketPlace(market);
        if(null == marketplace) {
            log.error("MarketPlace: '" + market + "' is unknown!");
            return null;
        }
        
    	try {
    		Date sdate = DateUtil.truncate(DateUtil.parseDate(start));
    	    Date edate = DateUtil.truncate(DateUtil.getToday());
    	    if(null != end)
    	        edate = DateUtil.truncate(DateUtil.parseDate(end));
    	    return DataAnalysisAndVisualisation.getDataForSecurityPaper(DBAccessConvenience.getDataSource(observer), sdate, edate, subject, marketplace, what);
    	} catch(Exception e) {
    		log.error("Failed to get data for securitypaper " + securitypaper + " at market " + market + " via observer " + observer + " an exception was thrown!", e);
    	}
    	return null;
    }

    public static OHLCDataset getOHLCForSecurityPaper(String observer, String start, String end, String securitypaper, String market) throws PersistenceException, OJTSFunctionalityException {
    	Logger log = Logger.getLogger(SISCInterface.class);
    	
        SecurityPaper subject     = DBAccessConvenience.getSecurityPaper(securitypaper);
        if(null == subject) {
            log.error("SecurityPaper: '" + securitypaper + "' is unknown!");
            return null;
        }
        MarketPlace   marketplace = DBAccessConvenience.getMarketPlace(market);
        if(null == marketplace) {
            log.error("MarketPlace: '" + market + "' is unknown!");
            return null;
        }
        
    	try {
    		Date sdate = DateUtil.truncate(DateUtil.parseDate(start));
    	    Date edate = DateUtil.truncate(DateUtil.getToday());
    	    if(null != end)
    	        edate = DateUtil.truncate(DateUtil.parseDate(end));
    	    DoubleSequence sequence = DataAnalysisAndVisualisation.getOHLCForSecurityPaper(DBAccessConvenience.getDataSource(observer), sdate, edate, subject, marketplace);    	    
    	    return new OHLCDatasetImpl(sequence,new String[]{subject.getName()});  
    	} catch(Exception e) {
    		log.error("Failed to get ohlc data for securitypaper " + securitypaper + " at market " + market + " via observer " + observer + " an exception was thrown!", e);
    	}
    	return null;
    }    

    public static JFreeChart createCandlestickChartForSecurityPaper(String observer, String start, String end, String securitypaper, String market)  throws PersistenceException, OJTSFunctionalityException, DBException, CannotHandleSubjectException, IOException {
    	Logger log = Logger.getLogger(SISCInterface.class);
    	
        SecurityPaper subject     = DBAccessConvenience.getSecurityPaper(securitypaper);
        if(null == subject) {
            log.error("SecurityPaper: '" + securitypaper + "' is unknown!");
            return null;
        }
        MarketPlace   marketplace = DBAccessConvenience.getMarketPlace(market);
        if(null == marketplace) {
            log.error("MarketPlace: '" + market + "' is unknown!");
            return null;
        }
        
    	try {
    		Date sdate = DateUtil.truncate(DateUtil.parseDate(start));
    	    Date edate = DateUtil.truncate(DateUtil.getToday());
    	    if(null != end)
    	        edate = DateUtil.truncate(DateUtil.parseDate(end));
    	    return DataAnalysisAndVisualisation.createCandlestickChartForSecurityPaper(DBAccessConvenience.getDataSource(observer), sdate, edate, subject, marketplace);  
    	} catch(Exception e) {
    		log.error("Failed to create candlestick chart for securitypaper " + securitypaper + " at market " + market + " via observer " + observer + " an exception was thrown!", e);
    	}
    	return null;
    }    
    
    public static CurrencyConversionContext getCurrencyConversionContext(String observer, String market, String targetcurrency) {
    	Logger log = Logger.getLogger(SISCInterface.class);

    	Unit        ut = DBAccessConvenience.getUnit(targetcurrency);
        if(null == ut) {
            log.error("Target currency: '" + targetcurrency + "' is unknown!");
            return null;            
        }
        DataSource  ds = DBAccessConvenience.getDataSource(observer);
        if(null == ds) {
            log.error("Observer: '" + observer + "' is unknown!");
            return null;            
        }
        MarketPlace mt = DBAccessConvenience.getMarketPlace(market);
        if(null == ds) {
            log.error("MarketPlace: '" + market + "' is unknown!");
            return null;            
        }
        CurrencyConversionContext context = new CurrencyConversionContext(ut, ds, mt);
        return context;
    }
    
}
