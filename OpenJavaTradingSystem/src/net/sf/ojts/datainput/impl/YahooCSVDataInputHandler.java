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
 * Created on 29.08.2004 by CS
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
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.jdo.subject.Share;
import net.sf.ojts.jdo.subject.Subject;
import net.sf.ojts.util.DateUtil;

/**
 * @author CS
 *
 */
public class YahooCSVDataInputHandler implements DataInputHandler {
	
	protected DataSource                        data_source_ = null;
	protected static SimpleDateFormat           SDF_         = null;
	protected static Calendar                   CAL_         = null;
	protected HttpWebUtils                      webutils_    = null;
	
	static {
		TimeZone TZ          = TimeZone.getTimeZone("GMT");
		String   DATE_FORMAT = "d-MMM-yy";// "yyyy-MM-dd HH:mm:ss";
		SDF_                 = new java.text.SimpleDateFormat(DATE_FORMAT, new Locale(""));
		SDF_.setTimeZone(TZ);
		CAL_                 = Calendar.getInstance(TZ);	    
	}
	
	public YahooCSVDataInputHandler(DataSource data_source) {
		data_source_ = data_source;
		Logger log = Logger.getLogger(this.getClass());
		
		webutils_            = new HttpWebUtils();
	}

	public Result read(Date start, Date end, Subject subject, MarketPlace market) throws IOException, CannotHandleSubjectException, DBException {
		Logger log = Logger.getLogger(this.getClass());
		
		if(null == subject || null == market) {
		    String msg = "YahooCSVDataInputHandler.read(): Either subject or market were given as NULL!";
		    log.error(msg);
		    throw new CannotHandleSubjectException(msg);		    
		}
		
        subject = DBAccessConvenience.downcastSubject(subject);
        
		Share equity = null;
		if(!(subject instanceof Share)) {
			String msg = "YahooCSVDataInputHandler.read(): can only handle subjects of type Share."; 
			log.info(msg);
			throw new CannotHandleSubjectException(msg);
		} else {
		    equity = (Share) subject;
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

	    String query = null;
		// first find the date ranges that still need to be fetched.
		Date[][] intervals_still_to_read = DateRangeUtils.findIntervalsStillToRead(DBAccessConvenience.getObserver("yahoo"), DBAccessConvenience.getDataSourceType("csv"), market, subject, start, end);
		
		// get observer data source configurations
		ObserverDataSourceConfiguration[] info = DBAccessConvenience.getObserverDataSourceConfiguration(data_source_, market);
		if(null == info || info.length == 0) {
		    /*
		     * the reason why there has to be a set of observer data source configurations for every market is, because in my opinion it is not
		     * clear that an observer reports for all markets the same data? e.g. if the observer has a special agreement with one market then the info might be
		     * more exhaustive than for other markets.
		     */
		    String msg = "YahooCSVDataInputHandler.read(): There is no observer data source configuration for the market: " + market.getName();
		    log.error(msg);
		    throw new CannotHandleSubjectException(msg);
		}
		
		Result container = new Result(info);
		List ritemslist  = new ArrayList();
		
		for(int i = 0; i < intervals_still_to_read.length; i++) {
		    readCSV(ritemslist, container, info, market, equity, intervals_still_to_read[i][0], intervals_still_to_read[i][1]);
		}
		
		ResultItem[] ritems = new ResultItem[ritemslist.size()];
		int i = 0;
		for (Iterator iter = ritemslist.iterator(); iter.hasNext();) {
			ResultItem element = (ResultItem) iter.next();
			ritems[i] = element; i++;
		}
		container.setResultItems(ritems);
		
		return container;
	}
	
	protected String prepareUrl(String patternurl, Observer observer, MarketPlace market, SecurityPaper equity, Date start, Date end) throws DBException {
		Calendar c = Calendar.getInstance();
	    String url = patternurl;
		c.setTime(start);
		url = url.replaceAll("\\|SMS\\|"      , "" + (c.get(Calendar.MONTH)));
		url = url.replaceAll("\\|SDS\\|"      , "" + c.get(Calendar.DAY_OF_MONTH));
		url = url.replaceAll("\\|SYYYYS\\|"   , "" + c.get(Calendar.YEAR));
		c.setTime(end);
		url = url.replaceAll("\\|EME\\|"      , "" + (c.get(Calendar.MONTH)));
		url = url.replaceAll("\\|EDE\\|"      , "" + c.get(Calendar.DAY_OF_MONTH));
		url = url.replaceAll("\\|EYYYYE\\|"   , "" + c.get(Calendar.YEAR));

		
		String query = "SELECT al FROM " + Alias.class.getName() + " al WHERE al.type.observerLink = $1 and al.market = $2 and al.subject = $3 ";
		Alias  alias = null;
		{
			Object[] query_arguments = {data_source_.getObserverLink(), market, equity};
			try {
                alias = (Alias) DBAccess.getObjectFromDatabase(query, query_arguments, true);
            } catch (Exception e) {
                throw new DBException(e);
            }
		}

		url = url.replaceAll("\\|YahooSYM\\|", alias.getAlias());
		return url;
	}

	protected static Date readCSVSingle(List ritemslist, Result container, ObserverDataSourceConfiguration[] info, SecurityPaper equity, Date start, Date end, String content) {
		Logger log = Logger.getLogger(YahooCSVDataInputHandler.class);

		String[] line = content.split("\n");
		
		Date date = null;
		for (int i = 0; i < line.length; i++) {
			String l = line[i];
			String[] element = l.split(",");
			try {
				date = SDF_.parse(element[0]);
			} catch (ParseException e1) {
				// this is not a date, therefore continue
				continue;
			}
			if(date.before(start) || date.after(end))
			    continue;
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
				String svalue = element[column];
				double value = -1.0;
				try {
					value = Double.parseDouble(svalue);
				} catch (NumberFormatException e2) {
					log.error("YahooCSVDataInputHandler.read(): NumberFormatException while trying to parse svalue: '" + svalue + "'");
					continue;
				}
				DoubleDataItem item = new DoubleDataItem(date, equity, info[j]);
				item.setValue(value);
				items[j] = item;
			}
		}		  
		
		return date;
	}

	protected String readCSV(List ritemslist, Result container, ObserverDataSourceConfiguration[] info, MarketPlace market, SecurityPaper equity, Date start, Date end) throws DBException {
	    int    size       = -1;
	    String allcontent = "";
	    Date tmpend       = end;
		do {
		    size =  ritemslist.size();
			// 1) build url string to fetch data from.
			// http://de.table.finance.yahoo.com/table.csv?a=|SMS|&b=|SDS|&c=|SYYYYS|&d=|EME|&e=|EDE|&f=|EYYYYE|&s=|YahooSYM|&y=0&g=d&ignore=.csv

			// find the intervals that still have to be read
			// check if we can find data for this read saved in the filesystem cache
			String url     = prepareUrl(data_source_.getUrl(), data_source_.getObserverLink(), market, equity, start, tmpend);			
			String content = webutils_.getContent(url);
			allcontent += content;
			
			// yahoo only returns data for 200 days in one go starting from the end date. 
			Date first_date_in_partial_result = readCSVSingle(ritemslist, container, info, equity, start, end, content);
			if(null == first_date_in_partial_result) // no valid content line was read
			    break;
			
			tmpend = DateUtil.dateOneDayBefore(first_date_in_partial_result);
			// the condition size < ritemslist.size() is used to "protect" against week-ends, when tmpend.after(start) would always be true, but no
			// data would be read for the week-end
		} while(tmpend.after(start) && size < ritemslist.size());
		

		return allcontent;
	}
		
}
