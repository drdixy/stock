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
 * Created on 01.05.2005
 *
 */
package net.sf.ojts.math.types.util;

/**
 * @author cs
 *
 */
public class LinearInterpolationStrategy implements InterpolationStrategy {
	
	public static final int LAST_AVAILABLE_VALUE = 0;
	public static final int TO_ZERO              = 1;
	
	protected int type_                    = LAST_AVAILABLE_VALUE;
	
	public LinearInterpolationStrategy() {
		type_ = LAST_AVAILABLE_VALUE;
	}
	public LinearInterpolationStrategy(int type) {
		type_ = type;
	}

    public double[] before(CompareAndMeasure compare, Object sslabel, Object datalabel, double[] value) {
        double[] result = new double[value.length];
        for(int i = 0; i < result.length; i++)
            result[i] = 0;
        return result;
    }
    public double[] middle(CompareAndMeasure compare, Object sslabel, Object datalabel0, Object datalabel1, double[] value0, double[] value1) {
        double[] result = new double[value0.length];
        double delta_1_0 = compare.difference(datalabel1, datalabel0);
        double delta_s_0 = compare.difference(sslabel, datalabel0);
        double q = delta_s_0 / delta_1_0; 
        
        for(int i = 0; i < result.length; i++)
            result[i] = value0[i] + q * (value1[i] - value0[i]);
        return result;
    }
    public double[] after (CompareAndMeasure compare, Object sslabel, Object datalabel, double[] value) {
        double[] result = new double[value.length];
		if(LAST_AVAILABLE_VALUE == type_) {
	        for(int i = 0; i < result.length; i++)
	            result[i] = value[i];						
		} else if (TO_ZERO == type_) {
	        for(int i = 0; i < result.length; i++)
	            result[i] = 0;			
		} else {
	        for(int i = 0; i < result.length; i++)
	            result[i] = value[i];
		}
        return result;
    }
    

}
