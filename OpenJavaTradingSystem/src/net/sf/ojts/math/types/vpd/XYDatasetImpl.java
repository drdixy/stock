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
package net.sf.ojts.math.types.vpd;

import java.util.Date;

import net.sf.ojts.math.types.DoubleSequence;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;

/**
 * @author cs
 *
 */
public class XYDatasetImpl implements XYDataset {

    protected DatasetGroup         group_            = null;
    protected DoubleSequence       input_            = null;
    protected String[]             series_names_     = new String[0];
    protected boolean              data_axis_wanted_ = false;
    
    public XYDatasetImpl(DoubleSequence input, String[] series_names) {
        assert null != series_names && null != input && (input.depth()) >= series_names.length; 
        group_        = new DatasetGroup();
        input_        = input;
        series_names_ = series_names;
    }
    
    public XYDatasetImpl(DoubleSequence input, String[] series_names, boolean data_axis_wanted) {
        this(input, series_names);
        data_axis_wanted_ = data_axis_wanted;
    }
    
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }
    public int getItemCount(int series) {
        return input_.size();
    }
    public Number getX(int series, int index) {
        return new Double(getXValue(series, index));
    }
    public double getXValue(int series, int index) {
        Object xvalue = input_.getLabel(index);
        if(null == xvalue) return -1;
        if(xvalue instanceof Date) {
            if(data_axis_wanted_)
                return ((Date) xvalue).getTime();
            else
                return index;
        }
        if(xvalue instanceof Number)
            return ((Number) xvalue).doubleValue();
        return -1;
    }
    public Number getY(int series, int item) {
        return new Double(getXValue(series, item));
    }
    public double getYValue(int series, int item) {
        return input_.get(item, series);
    }
    public int getSeriesCount() {
        return series_names_.length;
    }
    public String getSeriesName(int series) {
        return series_names_[series];
    }
    public void addChangeListener(DatasetChangeListener listener) {
    }
    public void removeChangeListener(DatasetChangeListener listener) {
    }
    public DatasetGroup getGroup() {
        return group_;
    }
    public void setGroup(DatasetGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Null 'group' argument.");
        }
        group_ = group;
    }
    
    public String toString() {
        if(null != input_)
            return input_.toString();
        else
            return null;
    }
}
