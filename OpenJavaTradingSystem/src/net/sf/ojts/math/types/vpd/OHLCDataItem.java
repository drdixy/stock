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
 * Created on 19.11.2004 by cs
 *
 */

package net.sf.ojts.math.types.vpd;

import java.util.Date;

/**
 * @author cs
 *
 */
public interface OHLCDataItem {

    public Date getDate();

    public double getOpen();
    public void setOpen(double open);
    public double getHigh();
    public void setHigh(double high);
    public double getLow();
    public void setLow(double low);
    public double getClose();
    public void setClose(double close);
    
    public double getVolume();
    public void setVolume(double volume);
    
    public String getUnit();
    public void setUnit(String unit);
}
