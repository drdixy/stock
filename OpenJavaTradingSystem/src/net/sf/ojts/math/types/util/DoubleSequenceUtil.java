/**
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
 * Created on 06.05.2005
 *
 */
package net.sf.ojts.math.types.util;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;

import net.sf.ojts.jdo.Unit;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.util.DateUtil;

/**
 * @author cs
 *
 */
public class DoubleSequenceUtil {

	public static String paddWith(int totallength, String padchar, String value) {
		int padnum = totallength - value.length();
		for(int i = 0; i < padnum ; i++) {
			value = " " + value;
		}
		return value;
	}
	
    public static String prettyPrint(DoubleSequence sequence, boolean twopass) {
        String result = "";

        int size  = sequence.size();
        int depth = sequence.depth();

		NumberFormat   standard = NumberFormat.getInstance();
		standard.setMaximumFractionDigits(2);
		standard.setMinimumFractionDigits(2);
        NumberFormat[] formats = new NumberFormat[depth];
        for(int j = 0; j < depth; j++) {
            Unit unit = sequence.getUnitForColumn(j);
            if(null == unit || null == unit.getPropertyTypeLink()) {
                formats[j] = standard;
                continue;
            }
            if(unit.getPropertyTypeLink().getName().equals("PRICE")) {
                NumberFormat cf =  NumberFormat.getCurrencyInstance();
                cf.setCurrency(Currency.getInstance(unit.getName()));
                formats[j] = cf;
                continue;
            }
            if(unit.getPropertyTypeLink().getName().equals("VOLUME")) {
                formats[j] = NumberFormat.getIntegerInstance();
                continue;
            }
            if(unit.getPropertyTypeLink().getName().equals("PERCENTAGE")) {
                formats[j] = NumberFormat.getPercentInstance();
				formats[j].setMaximumFractionDigits(2);
				formats[j].setMinimumFractionDigits(2);
                continue;
            }
                
            formats[j] = NumberFormat.getInstance();
        }

		int[] maxsize = new int[depth];
		if(twopass) {
	        for(int i = 0; i < size; i++) {
	            for(int j = 0; j < depth; j++) {
	                double value = sequence.get(i, j);
	                result = formats[j].format(value);
					maxsize[j] = Math.max(maxsize[j], result.length());
	            }
	        }			
		}
		
        Date date = null;
        result = "(\n";
		if(twopass) {
	        for(int i = 0; i < size; i++) {
	            date = (Date) sequence.getLabel(i);
	            result += "(" + DateUtil.formatDate(date) + " ";
	            for(int j = 0; j < depth; j++) {
	                double value = sequence.get(i, j);
	                if(0 == j)
	                    result += paddWith(maxsize[j], " ", formats[j].format(value));
	                else
	                    result += " " + paddWith(maxsize[j], " ", formats[j].format(value));
	            }
	            result += ")\n";
	        }			
		} else {
	        for(int i = 0; i < size; i++) {
	            date = (Date) sequence.getLabel(i);
	            result += "(" + DateUtil.formatDate(date) + " ";
	            for(int j = 0; j < depth; j++) {
	                double value = sequence.get(i, j);
	                if(0 == j)
	                    result += formats[j].format(value);
	                else
	                    result += " " + formats[j].format(value);
	            }
	            result += ")\n";
	        }
		}
        result += ")\n";			
        
        return result;        
    }
    
}
