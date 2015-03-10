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
 * Created on 24.04.2005
 *
 */
package net.sf.ojts.math.types.vpd;

import java.util.Date;

import net.sf.ojts.util.DateUtil;

/**
 * @author cs
 *
 */
public class ValuesPerDate {
    
    protected ValuesPerDateContainer container_ = null;
    protected Date                   date_      = null;
    protected double[]               values_    = new double[0];

    public ValuesPerDate(ValuesPerDateContainer container, Date date, double[] values) {
        super();
        container_ = container;
        date_      = date;
        values_    = values;
    }

    public ValuesPerDate(Date date, double[] values) {
        super();
        date_      = date;
        values_    = values;
    }
    
    public ValuesPerDateContainer getContainer() {
        return container_;
    }
    public void setContainer(ValuesPerDateContainer container) {
        container_ = container;
    }
    public Date getDate() {
        return date_;
    }
    public void setDate(Date date) {
        date_ = date;
    }
    public double[] getValues() {
        return values_;
    }
    public void setValues(double[] values) {
        if(null == values)
            values = new double[0];
        values_ = values;
    }
    
    
    
    public String toString() {
        String result = "" + DateUtil.formatAccurateDate(date_) + ":[";
        if(values_.length > 0) {
            result += values_[0] + " " + container_.getUnitForColumn(0).getName();
            for(int i = 1; i < values_.length; i++) {
                result += ";" + values_[i] + " " + container_.getUnitForColumn(i).getName();
            }
        }
        result += "]";
        return  result;
    }
}
