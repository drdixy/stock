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
 * Created on 01.05.2005
 *
 */
package net.sf.ojts.math.types.vpd;

import java.util.Date;

import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.DoubleSequenceResult;


/**
 * @author cs
 *
 */
public class OHLCDataItemImpl implements OHLCDataItem {
    
    protected DoubleSequence       input_    = null;
    protected DoubleSequenceResult output_   = null;
    protected int                  rowindex_ = -1;
        
    
    public OHLCDataItemImpl(DoubleSequence input, DoubleSequenceResult output, int rowindex) {
        input_    = input;
        output_   = output;
        rowindex_ = rowindex;
    }

    public Date getDate() {        
        return (Date) input_.getLabel(rowindex_);
    }
    
    public double getOpen() {
        return input_.get(rowindex_, 0);
    }
    public void setOpen(double open) {
        output_.setResult(rowindex_, 0, open);
    }
    public double getHigh() {
        return input_.get(rowindex_, 1);
    }
    public void setHigh(double high) {
        output_.setResult(rowindex_, 1, high);
    }
    public double getLow() {
        return input_.get(rowindex_, 2);
    }
    public void setLow(double low) {
        output_.setResult(rowindex_, 2, low);
    }    
    public double getClose() {
        return input_.get(rowindex_, 3);
    }
    public void setClose(double close) {
        output_.setResult(rowindex_, 3, close);
    }
    public double getVolume() {
        return input_.get(rowindex_, 4);
    }
    public void setVolume(double volume) {
        output_.setResult(rowindex_, 4, volume);
    }
    public String getUnit() {
        return input_.getUnitForColumn(0).getName();
    }
    public void setUnit(String sunit) {
        Unit unit = DBAccessConvenience.getUnit(sunit);
        if(null == unit) return;
        output_.setUnitForColumn(0, unit);
        output_.setUnitForColumn(1, unit);
        output_.setUnitForColumn(2, unit);
        output_.setUnitForColumn(3, unit);
    }

}
