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
 * Created on 30.03.2005 by cs
 *
 */
package net.sf.ojts.datainput.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.ParserException;

import net.sf.ojts.datainput.DataInputHandler;
import net.sf.ojts.datainput.Result;
import net.sf.ojts.datainput.ResultItem;
import net.sf.ojts.datainput.exceptions.CannotHandleSubjectException;
import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.DBAccess;
import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.jdo.Alias;
import net.sf.ojts.jdo.DataItem;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.DoubleDataItem;
import net.sf.ojts.jdo.ObserverDataSourceConfiguration;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.CurrencyPair;
import net.sf.ojts.jdo.subject.Fund;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.jdo.subject.Share;
import net.sf.ojts.jdo.subject.Subject;
import net.sf.ojts.util.DateUtil;

/**
 * @author cs
 *
 */
public class OnVistaHTMLDataInputHandler implements DataInputHandler {

	protected DataSource                        data_source_ = null;
	protected static SimpleDateFormat           SDF_         = null;
	protected static Calendar                   CAL_         = null;
	protected HttpWebUtils                      webutils_    = null;

	static {
		TimeZone TZ          = TimeZone.getTimeZone("GMT");
		String   DATE_FORMAT = "dd.MM.yy";// "yyyy-MM-dd HH:mm:ss";
		SDF_                 = new java.text.SimpleDateFormat(DATE_FORMAT, Locale.GERMANY);
		SDF_.setTimeZone(TZ);
		CAL_                 = Calendar.getInstance(TZ);
	}
	
	public OnVistaHTMLDataInputHandler(DataSource data_source) {
		data_source_         = data_source;
		webutils_            = new HttpWebUtils();
	}

	public Result read(Date start, Date end, Subject subject, MarketPlace market) throws IOException, CannotHandleSubjectException, DBException {
		Logger log = Logger.getLogger(this.getClass());

		if(null == subject) {
		    // it does not matter what market is given, even null if fine, because we anyway get only one set of data from onvista
		    String msg = "OnVistaHTMLDataInputHandler.read(): Subject was given as NULL!";
		    log.error(msg);
		    throw new CannotHandleSubjectException(msg);		    
		}
		
		if(start.after(end)) {
		    Date tmp = start;
		    start    = end;
		    end      = tmp;
		}

		// the historical price service from yahoo does not have data for today
		end = DateUtil.predictLastAvailableDate(data_source_.getDataLagInDays(), end);
	    if(start.after(end)) {
	        start = end;
	    }
		
		// the historical price service from onvista has a coarse grained time granularity, only values of
		// 3, 6, 12, 24, ... month of price values are available
		// besides that onvista does not provide a way to identify the market place from where the prices will come from
		// therefore i always put as market XETRA
        subject = DBAccessConvenience.downcastSubject(subject);
		
		SecurityPaper sp = null;
		if(subject instanceof Share || subject instanceof Fund || subject instanceof CurrencyPair) {
		    sp = (SecurityPaper) subject;
		} else {
			String msg = "OnVistaHTMLDataInputHandler.read(): can only handle subjects of type Share or Fund."; 
			log.info(msg);
			throw new CannotHandleSubjectException(msg);		        		    
		}
		
	    return readSecurityPaper(start, end, sp);		
	}		

	public Result readSecurityPaper(Date start, Date end, SecurityPaper securitypaper) throws IOException, CannotHandleSubjectException, DBException {
		Logger log = Logger.getLogger(this.getClass());

		MarketPlace market = DBAccessConvenience.getMarketPlace("UNDEFINED");// for onvista i do not know from which market the data is coming from?

		ObserverDataSourceConfiguration[] info = DBAccessConvenience.getObserverDataSourceConfiguration(data_source_, market);
		if(null == info || info.length == 0) {
		    String msg = "OnVistaHTMLDataInputHandler.readShare(): There is no observer data source configuration for the market: " + market.getName();
		    log.error(msg);
		    throw new CannotHandleSubjectException(msg);
		}		

		Date[][] intervals_still_to_read = DateRangeUtils.findIntervalsStillToRead(data_source_.getObserverLink(), data_source_.getType(), market, securitypaper, start, end);
		if(intervals_still_to_read.length == 0) {
			Result container = new Result(info);
			ResultItem[] ritems = new ResultItem[0];
			container.setResultItems(ritems);
			return container;
		} else {
		    start = intervals_still_to_read[0][0];
		}
		
		int months = howManyMonthBack(start);
			
		Result container = null;
		List ritemslist  = new ArrayList();
		
		
		String url = null;
		if(securitypaper instanceof CurrencyPair) {
		    url = prepareCurrencyUrl(data_source_.getUrl(), securitypaper, months); 		    
		} else {
		    url = prepareUrl(data_source_.getUrl(), data_source_.getObserverLink(), market, securitypaper, months); 
		}
					
		String content = webutils_.getContent(url);

		Parser parser = Parser.createParser(content, "ISO8859-1");
		try {
		    TableTag table = null;
			Node[] node = parser.extractAllNodesThatAre(TableTag.class);
			for(int i = 0; i < node.length; i++) {
                TableTag tt = (TableTag) node[i];
                if(tt.getRowCount() > 20) {
                    table = tt;
                    break;
                }
			}
            if(null == table) {
        		String msg = "OnVistaHTMLDataInputHandler.read(): did not find a table with more than 20 entires!"; 
        		log.error(msg);
        		throw new IOException(msg);
            }
            TableRow[] row = table.getRows();
    		container = new Result(info);
            for(int i = 0; i < row.length; i++) {
                Object[] element = getRowContent(row[i]);
                if(null == element)
                    continue;
                
                Date date = (Date) element[0];
    			ResultItem ritem = new ResultItem(container, date);
    			DataItem[] items = new DataItem[info.length];
    			ritem.setValues(items);
    			ritemslist.add(ritem);
    			for (int j = 0; j < info.length; j++) {
    				items[j] = null;
    				String scolumn = info[j].getColu();
    				int column = -1;
    				try {
    					column = Integer.parseInt(scolumn);
    				} catch (NumberFormatException e2) {
    					log.error("YahooCSVDataInputHandler.read(): NumberFormatException while trying to parse scolumn: '" + scolumn + "'");
    					continue;
    				}
    				DoubleDataItem item = new DoubleDataItem(date, securitypaper, info[j]);
    				Double value = (Double) element[column];
    				item.setValue(value.doubleValue());
    				items[j] = item;
    			}
                
            }

    		ResultItem[] ritems = new ResultItem[ritemslist.size()];
    		int i = 0;
    		for (Iterator iter = ritemslist.iterator(); iter.hasNext();) {
    			ResultItem element = (ResultItem) iter.next();
    			ritems[i] = element; i++;
    		}
    		container.setResultItems(ritems);
            
        } catch (ParserException e) {
    		String msg = "OnVistaHTMLDataInputHandler.read(): while parsing the html page a parser exception occured. Probably the page format changed."; 
    		log.error(msg);
    		throw new IOException(msg);		        
        }
		
		return container;
	}	
	
	protected Object[] getRowContent(TableRow row) {
		Logger log = Logger.getLogger(this.getClass());

		TableColumn[] column = row.getColumns();
	    Object[] result = new Object[column.length];
	    for(int i = 0; i < column.length; i++) {
	        String content = column[i].getStringText();
	        content = content.replaceAll("[^0-9.,]+", "");
	        if(0 == i) {
				try {
                    Date date = SDF_.parse(content);
                    result[0] = date;
                } catch (ParseException e) {
                    // if the first element is not a date we are not interested in this row
                    return null;
                }				
	        } else {
	            content = content.replaceAll(",", ".");
	            Double value = null;
	            try {
	                if(!content.trim().equals(""))
	                    value = new Double(Double.parseDouble(content));
                } catch (NumberFormatException e) {
            		String msg = "OnVistaHTMLDataInputHandler.getRowContent(): number format exception for string: '" + content + "'"; 
            		log.error(msg);
                }
                result[i] = value;	            
	        }
	    }
	    
	    return result;
	}

	public int howManyMonthBack(Date start) {
	    int result = 3;
	    boolean found = false;
	    do {	        
	        Date workdate = DateUtil.truncate(DateUtil.getToday());	        
	        workdate = DateUtil.add(workdate, 0, -1 * result, 0, 0, 0, 0, 0);
	        if(workdate.before(start))
	            found = true;
	        else
	            result = result * 2;
	    } while(!found && result < 12 * 20);
	    	    
	    return result;
	}

	protected String prepareUrl(String patternurl, Observer observer, MarketPlace market, SecurityPaper securitypaper, int months) throws DBException {
	    String url = patternurl;
	    
		url = url.replaceAll("\\|LM\\|", "" + months);
				
		String query = "SELECT al FROM " + Alias.class.getName() + " al WHERE al.type.observerLink = $1 and al.market = $2 and al.subject = $3 ";
		Alias  alias = null;
		{
			Object[] query_arguments = {data_source_.getObserverLink(), market, securitypaper};
			try {
                alias = (Alias) DBAccess.getObjectFromDatabase(query, query_arguments, true);
            } catch (Exception e) {
                throw new DBException(e);
            }
		}

		url = url.replaceAll("\\|ID\\|", alias.getAlias());
		return url;
	}

	protected String prepareCurrencyUrl(String patternurl, SecurityPaper securitypaper, int months) throws DBException {
	    String url = patternurl;
	    
		url = url.replaceAll("\\|LM\\|", "" + months);

		CurrencyPair cp = (CurrencyPair) securitypaper;
		Unit from = cp.getFrom();
		Unit to   = cp.getTo();
		url = url.replaceAll("\\|CF\\|", "" + from.getName());
		url = url.replaceAll("\\|CT\\|", "" + to.getName());
				
		return url;
	}
	
	/*
	protected static String ONVISTA_CURRENCY_URL =  "http://waehrungen.onvista.de/kursliste.html?ID_CURRENCY_FROM=|CF|&ID_CURRENCY_TO=|CT|&RANGE=|LM|M";
	// http://waehrungen.onvista.de/kursliste.html?ID_CURRENCY_FROM=EUR&ID_CURRENCY_TO=USD&RANGE=3M
    public CurrencyConversion[] read(Date start, Date end, Unit from, Unit to) throws IOException, CannotHandleSubjectException, DBException {
		Logger log = Logger.getLogger(this.getClass());
		
		CurrencyConversion[] result = new CurrencyConversion[0];

		if(null == from || null == to) {
		    // it does not matter what market is given, even null if fine, because we anyway get only one set of data from onvista
		    String msg = "OnVistaHTMLDataInputHandler.read(): Currencies were given as NULL!";
		    log.error(msg);
		    throw new CannotHandleSubjectException(msg);		    
		}
		
		if(start.after(end)) {
		    Date tmp = start;
		    start    = end;
		    end      = tmp;
		}

		int months = howManyMonthBack(start);
		
        String url = ONVISTA_CURRENCY_URL;
		url = url.replaceAll("\\|LM\\|", "" + months);

		url = url.replaceAll("\\|CF\\|", "" + from.getName());
		url = url.replaceAll("\\|CT\\|", "" + to.getName());
		
		Observer observer = data_source_.getObserverLink();
		
		String content = webutils_.getContent(url);
		
		Parser parser = Parser.createParser(content, "ISO8859-1");
		try {
		    TableTag table = null;
			Node[] node = parser.extractAllNodesThatAre(TableTag.class);
			for(int i = 0; i < node.length; i++) {
                TableTag tt = (TableTag) node[i];
                if(tt.getRowCount() > 20) {
                    table = tt;
                    break;
                }
			}
            if(null == table) {
        		String msg = "OnVistaHTMLDataInputHandler.read(): did not find a table with more than 20 entires!"; 
        		log.error(msg);
        		throw new IOException(msg);
            }
            TableRow[] row = table.getRows();
            //CurrencyConversion[] conversions = new CurrencyConversion[row.];
            ArrayList list = new ArrayList();
            for(int i = 0; i < row.length; i++) {
                Object[] element = getRowContent(row[i]);
                if(null == element)
                    continue;
                
                Date date = (Date) element[0];
                Double open  = (Double) element[1];
                Double low   = (Double) element[2];
                Double high  = (Double) element[3];
                Double close = (Double) element[4];
                CurrencyConversion conversion = new CurrencyConversion(observer, date, from, to, open.doubleValue(), high.doubleValue(), low.doubleValue(), close.doubleValue());
                list.add(conversion);
            }
            result = new CurrencyConversion[list.size()];
            for(int i = 0; i < result.length; i++) {
                result[i] = (CurrencyConversion) list.get(i);
            }
        } catch (ParserException e) {
    		String msg = "OnVistaHTMLDataInputHandler.read(): while parsing the html page a parser exception occured. Probably the page format changed."; 
    		log.error(msg);
    		throw new IOException(msg);		        
        }
		
		return result;
    }
    */
	
}
