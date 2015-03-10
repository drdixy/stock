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
package net.sf.ojts.datainput;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

import net.sf.ojts.jdo.DataItem;
import net.sf.ojts.jdo.DataSourceType;
import net.sf.ojts.jdo.ObserverDataSourceConfiguration;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.Subject;
import net.sf.ojts.util.DateUtil;

/**
 * @author CS
 *
 */
public class Result {
	protected ResultItem[]                      resultItems = new ResultItem[0];
	protected ObserverDataSourceConfiguration[] info        = new ObserverDataSourceConfiguration[0];
	
	
	protected Date    start   = DateUtil.getToday();
	protected Date    end     = DateUtil.getToday();
	protected Subject subject = null;
		
	public Result() {
		super();
	}
	public Result(ObserverDataSourceConfiguration[] info) {
		super();
		this.info  = info;
	}
	
	public ObserverDataSourceConfiguration[] getInfo() {
		return info;
	}
	public void setInfo(ObserverDataSourceConfiguration[] info) {
		this.info = info;
	}
	public ResultItem[] getResultItems() {
		return resultItems;
	}
	public void setResultItems(ResultItem[] resultItems) {
		this.resultItems = resultItems;
		calculateStartAndEnd();
	}

	public String getObserverName() {
	    if(null != getObserver())
	        return getObserver().getName();
	    else
	        return null;
	}
	
	public Observer getObserver() {
	    Observer observer = null;
		ObserverDataSourceConfiguration[] info = getInfo();
		if(null != info && 0 != info.length) {
		    observer = info[0].getObserverDataSource().getObserverLink();
		}
	    return observer;	
	}

	public String getDataSourceTypeName() {
	    if(null != getDataSourceType())
	        return getDataSourceType().getName();
	    else
	        return null;
	}
	
	public DataSourceType getDataSourceType() {
	    DataSourceType dstype = null;
		ObserverDataSourceConfiguration[] info = getInfo();
		if(null != info && 0 != info.length) {
		    dstype = info[0].getObserverDataSource().getType();
		}
	    return dstype;	
	}
	
	public String getMarketPlaceName() {
	    if(null != getMarketPlace())
	        return getMarketPlace().getName();
	    else
	        return null;
	}
	
	public MarketPlace getMarketPlace() {
	    MarketPlace market = null;
		ObserverDataSourceConfiguration[] info = getInfo();
		if(null != info && 0 != info.length) {
		    market   = info[0].getObservedAt();
		}
	    return market;	
	}
	
	public String getSubjectName() {
	    if(null != getSubject())
	        return getSubject().getName();
	    else
	        return null;
	}
	
	public Subject getSubject() {
	    Subject subject = this.subject;
		ResultItem[] ritems = getResultItems();
		if(null != ritems && 0 != ritems.length) {
		    DataItem[] ditems = ritems[0].getValues();
    		if(null != ditems && 0 != ditems.length) {
    		    subject  = ditems[0].getSubject();
    		}
		}
	    return subject;
	}
	
	public void calculateStartAndEnd() {
		Logger log = Logger.getLogger(this.getClass());

        try {
            end = DateUtil.parseDate("1900-01-01");
        } catch (ParseException e) {
            log.error("date parse exception", e);
        }
        for(int i = 0; i < resultItems.length; i++) {            
            DataItem[] data_item = resultItems[i].getValues();
            Date date = resultItems[i].getDate();
            if(date.before(start))
                start = date;
            if(date.after(end))
                end = date;
        }	    
	}
	
    public Date getEnd() {
        return end;
    }
    public Date getStart() {
        return start;
    }
    public void setEnd(Date end) {
        if(resultItems.length == 0)
            this.end = end;
    }
    public void setStart(Date start) {
        if(resultItems.length == 0)
            this.start = start;
    }
    public void setSubject(Subject subject) {
        if(resultItems.length == 0)
            this.subject = subject;
    }
}
