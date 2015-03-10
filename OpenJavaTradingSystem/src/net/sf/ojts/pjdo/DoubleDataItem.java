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
 * Created on 28.11.2004 by cs
 *
 */


package net.sf.ojts.pjdo;

import java.util.Comparator;
import java.util.Date;

/**
 * @author cs
 *
 */
public class DoubleDataItem extends ValueAndUnit {

    public static class DateOrderComparator implements Comparator {
        public int     compare(Object o1, Object o2) {
            if(null != o1 && null != o2 && o1 instanceof DoubleDataItem && o2 instanceof DoubleDataItem) {
                DoubleDataItem di1 = (DoubleDataItem) o1;
                DoubleDataItem di2 = (DoubleDataItem) o2;
                return di1.date.compareTo(di2.date);
            } else {
                throw new ClassCastException("Could not cast o1 or o2 to type DoubleDataItem");
            }
        }
        
        public boolean equals(Object obj) {
            return false;
        }
    }
    
	protected Date   date;

    public DoubleDataItem() {
        super();
        this.date  = null;
    }
	
    public DoubleDataItem(Date date) {
        super();
        this.date  = date;
    }
	
    public DoubleDataItem(Date date, double value, String unit) {
        super(value, unit);
        this.date = date;
    }
    
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
}
