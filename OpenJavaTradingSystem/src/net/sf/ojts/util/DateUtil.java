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
 * Created on 31.07.2004 by cs
 *
 */
package net.sf.ojts.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author cs
 *
 */
public class DateUtil {
    protected static TimeZone                   TZ          = TimeZone.getTimeZone("GMT");
	//protected static TimeZone                   TZ          = TimeZone.getDefault();
	protected static String                     DATE_FORMAT = "yyyy-MM-dd";// "yyyy-MM-dd HH:mm:ss";
	protected static java.text.SimpleDateFormat SDF         = new java.text.SimpleDateFormat(DATE_FORMAT);
	protected static Calendar                   CAL         = Calendar.getInstance(TZ);

	protected static String                     ACCURATE_DATE_FORMAT = "yyyy-MM-dd_HHmmss";
	protected static java.text.SimpleDateFormat ADF                  = new java.text.SimpleDateFormat(ACCURATE_DATE_FORMAT);
	
	static {
		/*
		 * on some JDK, the default TimeZone is wrong
		 * we must set the TimeZone manually!!!
		 *     sdf.setTimeZone(TimeZone.getTimeZone("CEST"));
		 */
		SDF.setTimeZone(TZ);       
		ADF.setTimeZone(TZ);
	}
		
	public static String formatDate(Date date) {
		if(null == date)
			return "";
		return SDF.format(date);
	}

	public static String formatAccurateDate(Date date) {
		if(null == date)
			return "";
		return ADF.format(date);
	}
	
	public static Date parseDate(String date) throws ParseException {
		return SDF.parse(date);
	}
	
	public static Date parseAccurateDate(String date) throws ParseException {
		return ADF.parse(date);
	}
	
	public static Date dateOneDayBefore(Date date) {
		CAL.setTime(date);
		CAL.add(Calendar.DATE, -1);
		return CAL.getTime();		
	}

	public static Date dateOneDayAfter(Date date) {
		CAL.setTime(date);
		CAL.add(Calendar.DATE, 1);
		return CAL.getTime();		
	}
	
	public static Date getNow() {
		Calendar cal = Calendar.getInstance(TZ);
		Date   today = cal.getTime();
		return today;
	}
	
	public static int getDayOfWeek(Date date) {
	    CAL.setTime(date);
	    return CAL.get(Calendar.DAY_OF_WEEK);
	}

	public static Date getToday() {
	    return truncate(getNow());
	}

	public static Date getTomorrow() {
	    return truncate(dateOneDayAfter(getNow()));
	}
	
	public static Date truncate(Date date) {
		CAL.setTime(date);
		CAL.set(Calendar.AM_PM       , Calendar.AM);
		CAL.set(Calendar.HOUR       ,0);
		CAL.set(Calendar.MINUTE     ,0);
		CAL.set(Calendar.SECOND     ,0);
		CAL.set(Calendar.MILLISECOND,0);
		return CAL.getTime();		
	}
	
	public static Date add(Date date, int years, int months, int days, int hours, int minutes, int seconds, int millis) {
		CAL.setTime(date);
		CAL.add(Calendar.YEAR        , years);
		CAL.add(Calendar.MONTH       , months);
		CAL.add(Calendar.DAY_OF_MONTH, days);		
		CAL.add(Calendar.HOUR        , hours);
		CAL.add(Calendar.MINUTE      , minutes);
		CAL.add(Calendar.SECOND      , seconds);
		CAL.add(Calendar.MILLISECOND , millis);
		return CAL.getTime();		
	}
	
	public static double getNumberOfDaysBetween(Date sdate, Date edate) {
        long from = sdate.getTime();
        long to   = edate.getTime();
        double difference = to - from;        
        double number_of_days = difference / (1000.0 * 60 * 60 * 24);
        return number_of_days;
	}
	
    public static Date predictLastAvailableDate(int lag_in_days, Date end_date) {
        end_date = DateUtil.truncate(end_date);
        Date today = DateUtil.getToday();
        Date lag_date = DateUtil.add(today, 0, 0, -1 * lag_in_days, 0, 0, 0, 0);
        if(DateUtil.getDayOfWeek(lag_date) == Calendar.SATURDAY) {
            lag_date = DateUtil.add(lag_date, 0, 0, -1, 0, 0, 0, 0); // the friday before the week-end
        } else if (DateUtil.getDayOfWeek(lag_date) == Calendar.SUNDAY) {
            lag_date = DateUtil.add(lag_date, 0, 0, -2, 0, 0, 0, 0); // the friday before the week-end            
        } else {
            // do nothing
        }
        
        if(end_date.after(lag_date))
            return lag_date;
        else
            return end_date;
    }

}
