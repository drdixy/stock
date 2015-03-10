/*
 * Created on 21.11.2004 by cs
 *
 */
package net.sf.ojts.datainput.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.PersistenceException;

import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.DBAccess;
import net.sf.ojts.functionality.exceptions.NoDBResultsException;
import net.sf.ojts.jdo.DataInputRegistry;
import net.sf.ojts.jdo.DataSourceType;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.Subject;
import net.sf.ojts.util.DateUtil;

/**
 * @author cs
 *
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
public class DateRangeUtils {

    protected static DataInputRegistry[] convertToSorted(Object[] objs) {
		Logger log = Logger.getLogger(DateRangeUtils.class);

		if(null == objs)
		    return new DataInputRegistry[0];
		
		DataInputRegistry[] dir = new DataInputRegistry[objs.length];
        int i = 0;
        boolean first = true;
        HashMap markmap = new HashMap();
        while( i < objs.length ) {
            Date min = null;
            try {
                min = DateUtil.parseDate("3000-12-31");
            } catch (ParseException e) {
                log.error("ParseException", e);
            }
            dir[i] =  null;
            // add the data input registry objects in sorted start date order
            int markj = -1;
			for (int j = 0; j < objs.length; j++) {
			    DataInputRegistry temp = (DataInputRegistry) objs[j];
			    if(temp.getStartDate().before(min) && (null == markmap.get(new Integer(j)))) {
			        min    = temp.getStartDate();
			        dir[i] = temp;
			        markj  = j;
			    }
            }
            markmap.put(new Integer(markj), new Integer(markj));
            first = false;
            i++;
        }
        return dir;
    }
    
    public static DataInputRegistry[] getRegisteredDataInputRanges(Observer observer, DataSourceType dstype, MarketPlace marketplace, Subject subject) {
		Logger log = Logger.getLogger(DateRangeUtils.class);

		String observer_and_type = DataInputCacheUtils.buildObserverAndTypeString(observer, dstype);
		String market            = marketplace.getName();
		String isin              = subject.getName();

		Object[] oresult = null;
		try {
    		String query  = "SELECT dir FROM " + DataInputRegistry.class.getName() + " dir WHERE dir.observer = $1 and dir.subject = $2 and dir.market = $3";
			Object[] query_arguments = {observer_and_type, isin, market};
            oresult =  DBAccess.getObjectsFromDatabase(query, query_arguments, false);
        } catch (NoDBResultsException e) {
            log.error("NoDBResultsException", e);
        } catch (PersistenceException e) {
            // was already logged
            log.error("PersistenceException", e);
        }
        
        if(null == oresult || oresult.length == 0)
            return new DataInputRegistry[0];
    
        return convertToSorted(oresult);
    }
    
    public static DataInputRegistry[] getRegisteredDataInputRanges(Observer observer, DataSourceType dstype, MarketPlace marketplace, Subject subject, Date start, Date end) {
		assert null != observer && null != dstype;
		Logger log = Logger.getLogger(DateRangeUtils.class);

		String observer_and_type = DataInputCacheUtils.buildObserverAndTypeString(observer, dstype);
		String market            = marketplace.getName();
		String isin              = subject.getName();

		Object[] oresult = null;
		try {
    		String query  = "SELECT dir FROM " + DataInputRegistry.class.getName() + " dir WHERE dir.observer = $1 and dir.subject = $2 and dir.market = $3 and dir.endDate > $4 and dir.startDate < $5";
			Object[] query_arguments = {observer_and_type, isin, market, start, end};
            oresult =  DBAccess.getObjectsFromDatabase(query, query_arguments, false);
        } catch (NoDBResultsException e) {
            log.error("NoDBResultsException", e);
        } catch (PersistenceException e) {
            // was already logged
            log.error("PersistenceException", e);
        }
        
        if(null == oresult || oresult.length == 0)
            return new DataInputRegistry[0];
    
        return convertToSorted(oresult);
    }
    
    protected static Date[][] convertDIRToDateRanges(DataInputRegistry[] dir) {
        Date[][] result = new Date[dir.length][2];
        
        for(int i = 0; i < dir.length; i++) {
            Date[] interval = result[i];
            interval[0] = dir[i].getStartDate();
            interval[1] = dir[i].getEndDate();
        }
        
        return result;        
    }
        
    public static Date[][] getRegisteredDateRanges(Observer observer, DataSourceType dstype, MarketPlace marketplace, Subject subject, boolean unify) {
		Logger log = Logger.getLogger(DateRangeUtils.class);
		
		DataInputRegistry[] dir = getRegisteredDataInputRanges(observer, dstype, marketplace, subject);
        
		Date[][] intervals = convertDIRToDateRanges(dir);
        if(!unify) {
            return intervals;
        }
        
        return mergeIntervals(intervals);
    }
    
    protected static Date[][] mergeIntervals(Date[][] intervals) {
        LinkedList ll = new LinkedList();
        for(int i = 0; i < intervals.length; i++) {
            Date[] interval = intervals[i];
            ll.add(interval);
        }
        
        int i = 0;
        while(i < ll.size()) {
            Date[] di = (Date[]) ll.get(i);
            boolean remove = false;
            for(int j = i + 1; j < ll.size(); j++) {
                Date[] dj = (Date[]) ll.get(j);
                
                Date[] combination = combineDateInterval(di[0], di[1], dj[0], dj[1]);
                if(null == combination)
                    break;// i can do that, because the intervals are sorted according to the start date
                di[0] = combination[0];
                di[1] = combination[1];
                ll.remove(j);
                remove = true;
                break;
            }
            if(false == remove) i++;
        }
        
        Date[][] result = new Date[ll.size()][2];
        for(i = 0; i < result.length; i++) {
            result[i] = (Date[]) ll.get(i);
        }
        
        return result;
    }
    
    public static boolean isDateInIntervals(Date[][] intervals, Date test) {
        for(int i = 0; i < intervals.length; i++) {
            Date[] interval = intervals[i];
            if(test.before(interval[0]) || test.after(interval[1]))
                continue; // not in this interval, test others
            return true;
        }
        return false; // no interval found
    }
    
    public static Date[] combineDateInterval(Date s1, Date e1, Date s2, Date e2) {
        Date start = null;
        if(s1.before(s2)) {
            if(e1.before(s2)) // no combination possible
                return null;
            start = s1;
        } else {// s2.before(s1)
            if(e2.before(s1)) // no combination possible
                return null;
            start = s2;            
        }
        Date end = e1;
        if(end.before(e2))
            end = e2;
        Date[] result = new Date[2];
        result[0] = start;
        result[1] = end;
        
        return result;
    }
        
    public static Date[][] findIntervalsStillToRead(Observer observer, DataSourceType dstype, MarketPlace marketplace, Subject subject, Date start, Date end) throws DBException {
        DataInputRegistry[] dir = getRegisteredDataInputRanges(observer, dstype, marketplace, subject, start, end);
        
		Date[][] intervals = mergeIntervals(convertDIRToDateRanges(dir));
        
        ArrayList li = new ArrayList();
        int i = 0;
        if(intervals.length > 0) {
            Date end1   = DateUtil.truncate(start);
            Date start2 = DateUtil.dateOneDayBefore(DateUtil.truncate(intervals[0][0]));
            if(!start2.before(end1)) {
                Date[] interval = {end1, start2};
                li.add(interval);
            }
            for(i = 0; i < intervals.length - 1; i++) {
                end1   = DateUtil.dateOneDayAfter(DateUtil.truncate(intervals[i][1]));
                start2 = DateUtil.dateOneDayBefore(DateUtil.truncate(intervals[i + 1][0]));
                if(!start2.before(end1)) {
                    Date[] interval = {end1, start2};
                    li.add(interval);                        
                }
            }
            end1   = DateUtil.dateOneDayAfter(DateUtil.truncate(intervals[i][1]));
            start2 = DateUtil.truncate(end);
            if(!start2.before(end1)) {
                Date[] interval = {end1, start2};
                li.add(interval);                        
            }                
        } else {
            Date end1   = DateUtil.truncate(start);
            Date start2 = DateUtil.truncate(end);
            if(end1.before(start2)) {
                Date[] interval = {end1, start2};
                li.add(interval);                        
            }                
        }
        Date[][] intervals_still_to_read = new Date[li.size()][];
        for(i = 0; i < intervals_still_to_read.length; i++) {
            intervals_still_to_read[i] = (Date[]) li.get(i);
        }		
        return intervals_still_to_read;
    }
    
    public static boolean isDateInIntervals(Date date, Date[][] intervals) {
        // assume that the intervals are sorted nonoverlapping intervals
        if(null == intervals || 0 == intervals.length)
            return false;
        if(date.before(intervals[0][0]))
            return false;
        for(int i = 0; i < intervals.length; i++) {
            if(!date.before(intervals[i][0]) && !date.after(intervals[i][1])) 
                return true;
            if(!date.after(intervals[i][1]))
                return false;
        }        
        return false;
    }
    
}
