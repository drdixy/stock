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
 * Created on 26.04.2005
 *
 */
package net.sf.ojts.math.types.util;

import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.DoubleSequenceResult;

/**
 * @author cs
 *
 */
public class Interpolate {

    public static void interpolate(DoubleSequence superset, DoubleSequence data, DoubleSequenceResult result, CompareAndMeasure compare, InterpolationStrategy strategy) {
        if(null == superset || null == data || null == result || null == compare || data.size() == 0 || superset.size() == 0 || superset.size() <= data.size())
            return;
        superset.rewind(); data.rewind();
        before(superset, data, result, compare, strategy);
        middle(superset, data, result, compare, strategy);
        after(superset, data, result, compare, strategy);
        
    }
    
    protected static void before(DoubleSequence superset, DoubleSequence data, DoubleSequenceResult result, CompareAndMeasure compare, InterpolationStrategy strategy) {
        data.inc();// go to first element of the sequence
        double[] value = data.current();
        Object datalabel = data.getLabel();
        while(superset.hasNext()) {
            superset.inc();
            Object sslabel   = superset.getLabel();
            int cmp = compare.compare(sslabel, datalabel); 
            if(0 == cmp)
                break;
            if(cmp > 0)
                throw new RuntimeException("superset is not a proper superset of data!!");
            double[] interpolated = strategy.before(compare, sslabel, datalabel, value);
            int i = superset.getIndex();
            for(int j = 0; j < interpolated.length; j++) {
                result.setResult(i, j, interpolated[j]);                
            }
        }
    }

    protected static void middle(DoubleSequence superset, DoubleSequence data, DoubleSequenceResult result, CompareAndMeasure compare, InterpolationStrategy strategy) {
        double[] value0     = null;
        Object   datalabel0 = null;
        double[] value1     = data.current();
        Object   datalabel1 = data.getLabel();
        int depth = data.depth();
        int i = superset.getIndex();
        do {
            Object sslabel   = superset.getLabel();
            int cmp = compare.compare(sslabel, datalabel1); 
            if(0 == cmp) {// no interpolatin needed
                value0     = value1;
                datalabel0 = datalabel1;
                for(int j = 0; j < depth; j++) {
                    result.setResult(i, j, value0[j]);
                }
                if(!data.hasNext())
                    break;
                data.inc();
                value1     = data.current();
                datalabel1 = data.getLabel();
            } else if(cmp < 0) {
                double[] interpolated = strategy.middle(compare, sslabel, datalabel0, datalabel1, value0, value1);
                for(int j = 0; j < interpolated.length; j++) {
                    result.setResult(superset.getIndex(), j, interpolated[j]);
                }                
            } else {
                // should never happen!!
                throw new RuntimeException("sslabel can never be greater than datalabel!!");
            }
            if(!superset.hasNext())
                throw new RuntimeException("suberset is not a superset of data!!");
            i = superset.inc();
        } while(true);        
    }
    
    protected static void after(DoubleSequence superset, DoubleSequence data, DoubleSequenceResult result, CompareAndMeasure compare, InterpolationStrategy strategy) {
        double[] value = data.current();
        Object datalabel = data.getLabel();
        while(superset.hasNext()) {
            superset.inc();
            Object sslabel   = superset.getLabel();
            double[] interpolated = strategy.after(compare, sslabel, datalabel, value);
            int i = superset.getIndex();
            for(int j = 0; j < interpolated.length; j++) {
                result.setResult(i, j, interpolated[j]);                
            }
        }        
    }
}
