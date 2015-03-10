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

import net.sf.ojts.math.types.DoubleSequence;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.OHLCDataset;

/**
 * @author cs
 *
 */
public class OHLCDatasetImpl implements OHLCDataset {

    protected DatasetGroup         group_        = null;
    protected DoubleSequence       input_        = null;
    protected String[]             series_names_ = new String[0];
    
    public OHLCDatasetImpl(DoubleSequence input, String[] series_names) {
        group_        = new DatasetGroup();
        input_        = input;
        series_names_ = series_names;
    }
    
    /*
    public OHLCDataItem getOHLCDataItem(int index) {
        return new OHLCDataItemImpl(input_, output_, index);
    }
    */
    
    public Number getHigh(int series, int index) {
        return new Double(getHighValue(series, index));
    }
    public double getHighValue(int series, int index) {
        return input_.get(index, series * 5 + 1);
    }
    public Number getLow(int series, int index) {
        return new Double(getLowValue(series, index));
    }
    public double getLowValue(int series, int index) {
        return input_.get(index, series * 5 + 2);
    }
    public Number getOpen(int series, int index) {
        return new Double(getOpenValue(series, index));
    }
    public double getOpenValue(int series, int index) {
        return input_.get(index, series * 5 + 0);
    }
    public Number getClose(int series, int index) {
        return new Double(getCloseValue(series, index));
    }
    public double getCloseValue(int series, int index) {
        return input_.get(index, series * 5 + 3);
    }
    public Number getVolume(int series, int index) {
        return new Double(getVolumeValue(series, index));
    }
    public double getVolumeValue(int series, int index) {
        return input_.get(index, series * 5 + 4);
    }
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }
    public int getItemCount(int arg0) {
        return input_.size();
    }
    public Number getX(int series, int index) {
        return new Double(getXValue(series, index));
    }
    public double getXValue(int series, int index) {
        return ((Date) input_.getLabel(index)).getTime();
    }
    public Number getY(int series, int index) {
        return getClose(series, index);
    }
    public double getYValue(int series, int index) {
        return getCloseValue(series, index);
    }
    public int getSeriesCount() {
        return series_names_.length;
    }
    public String getSeriesName(int series) {
        return series_names_[series];
    }
    public void addChangeListener(DatasetChangeListener arg0) {
    }
    public void removeChangeListener(DatasetChangeListener arg0) {
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
