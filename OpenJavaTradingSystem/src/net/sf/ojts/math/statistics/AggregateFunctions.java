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
package net.sf.ojts.math.statistics;

import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.DoubleSequenceResult;

/**
 * @author cs
 *
 */
public class AggregateFunctions {

    public static void min(DoubleSequence input, DoubleSequenceResult result) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > 0 && result.resultDepth() > 0;
        int size = input.size();
        int depth = input.depth();
        double min   = 0.0;
        double value = 0.0;
        for(int i = 0; i < size; i++) {
            min = input.get(i, 0);
            for(int j = 1; j < depth; j++) {
                value = input.get(i, j);
                if(value < min)
                    min = value;
            }
            result.setResult(i, 0, min);
        }
    }

    public static void max(DoubleSequence input, DoubleSequenceResult result) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > 0 && result.resultDepth() > 0;
        int size = input.size();
        int depth = input.depth();
        double max   = 0.0;
        double value = 0.0;
        for(int i = 0; i < size; i++) {
            max = input.get(i, 0);
            for(int j = 1; j < depth; j++) {
                value = input.get(i, j);
                if(value > max)
                    max = value;
            }
            result.setResult(i, 0, max);
        }
    }

    public static void minmax(DoubleSequence input, DoubleSequenceResult result) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > 0 && result.resultDepth() > 1;
        int size = input.size();
        int depth = input.depth();
        double min   = 0.0;
        double max   = 0.0;
        double value = 0.0;
        for(int i = 0; i < size; i++) {
            min = input.get(i, 0);
            max = input.get(i, 0);
            for(int j = 1; j < depth; j++) {
                value = input.get(i, j);
                if(value < min)
                    min = value;
                if(value > max)
                    max = value;
            }
            result.setResult(i, 0, min);
            result.setResult(i, 1, max);
        }
    }

    public static void sum(DoubleSequence input, DoubleSequenceResult result, int outcolumn) {
        assert result != null && input != null && input.size() == result.size() && result.resultDepth() > outcolumn;
        int size = input.size();
        int depth = input.depth();
        double sum = 0.0;
        for(int i = 0; i < size; i++) {
            sum = 0.0;
            for(int j = 0; j < depth; j++) {
                sum += input.get(i,j);
            }
            result.setResult(i, outcolumn, sum);
        }
    }

    public static void relative(DoubleSequence input, int incolumn, DoubleSequenceResult result, int outcolumn) {
		relative(input, incolumn, null, -1, result, outcolumn);
    }	
	
    public static void relative(DoubleSequence input, int incolumn, DoubleSequence buysell, int buysellcolumn, DoubleSequenceResult result, int outcolumn) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > incolumn && result.resultDepth() > outcolumn;
        int size = input.size();
        if(size < 2)
            return;
        double previous = input.get(0,incolumn);
        result.setResult(0, outcolumn, 1.0);
        for(int i = 1; i < size; i++) {
			double bought = 0.0;
			if(null != buysell)
				bought = buysell.get(i, buysellcolumn);
            double value0 = input.get(i,incolumn);
			double value  = value0 - bought;
            result.setResult(i, outcolumn, value / previous);
            previous = value0;
        }
        Unit unit = DBAccessConvenience.getUnit("factor");
        result.setUnitForColumn(outcolumn, unit);
    }
    
    public static void log(double logbase, DoubleSequence input, int incolumn, DoubleSequenceResult result, int outcolumn) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > incolumn && result.resultDepth() > outcolumn;
        int size = input.size();
        for(int i = 0; i < size; i++) {
            double value = input.get(i,incolumn);
            result.setResult(i, outcolumn, Math.log(value) / Math.log(logbase));
        }
        Unit unit = DBAccessConvenience.getUnit("logf");
        result.setUnitForColumn(outcolumn, unit);
    }
    
    public static void logrelative(double logbase, DoubleSequence input, int incolumn, DoubleSequenceResult result, int outcolumn) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > incolumn && result.resultDepth() > outcolumn;
        int size = input.size();
        if(size < 2)
            return;
        double previous = input.get(0,incolumn);
        for(int i = 1; i < size; i++) {
            double value = input.get(i,incolumn);
            result.setResult(i, outcolumn, Math.log(value / previous) / Math.log(logbase));
            previous = value;
        }
        Unit unit = DBAccessConvenience.getUnit("conversion-factor");
        result.setUnitForColumn(outcolumn, unit);
    }


    public static void continoussum(DoubleSequence input, int incolumn, DoubleSequenceResult result, int outcolumn) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > incolumn && result.resultDepth() > outcolumn;
        int size = input.size();
        double sum = 0.0;
        for(int i = 0; i < size; i++) {
            sum += input.get(i,incolumn);
            result.setResult(i, outcolumn, sum);
        }
        Unit unit = input.getUnitForColumn(incolumn);
        result.setUnitForColumn(outcolumn, unit);
    }    
    
    public static void mean(DoubleSequence input, DoubleSequenceResult result) {
        assert result != null && input != null && input.size() == result.size() && result.resultDepth() > 0;
        int size = input.size();
        int depth = input.depth();
        double sum = 0.0;
        for(int i = 0; i < size; i++) {
            sum = 0.0;
            for(int j = 0; j < depth; j++) {
                sum += input.get(i,j);
            }
            result.setResult(i, 0, sum / depth);
        }
    }    

    // http://www.answers.com/topic/bias-statistics
    // http://www.answers.com/topic/variance-1
    /*
     * algorithm which avoids large numbers while summing up:
     * 
     * double avg = 0;
     * double var = 0;
     * long n = data.length; // number of elements
     * for i = 1 to n
     *   avg = (avg*i + data[i]) / (i + 1);
     *   var = (var * (i - 1) + (data[i] - avg)*(data[i] - avg)) / i;
     * end for
     */
    public static void varianceUnBiased(DoubleSequence input, DoubleSequenceResult result) {
        assert result != null && input != null && input.size() == result.size() && result.resultDepth() > 0;
        int size  = input.size();
        int depth = input.depth();
        double mean     = 0.0;
    	double variance = 0.0;
    	double value    = 0.0;
        for(int i = 0; i < size; i++) {
            mean     = 0.0;
        	variance = 0.0;
            for(int j = 0; j < depth; j++) {
                value        = input.get(i, j);
                mean         = (mean * (j + 1) + value) / (j + 2);
                variance     = (variance * i + (value - mean) * (value - mean)) / (j + 1); 
            }
            result.setResult(i, 0, mean);        
            
            if(result.resultDepth() < 2) {continue;}// no calculation of variance wanted        
            result.setResult(i, 1, variance);
            
            if(result.resultDepth() < 3) {continue;}// no calculation of sigma wanted        
    		double sigma    = Math.sqrt(variance);
            result.setResult(i, 2, sigma);
        }
    }    
    
    
    public static void movingAverage(DoubleSequence input, DoubleSequenceResult result, int span) {
        assert result != null && input != null && input.size() == result.size() && result.resultDepth() > 0;
        int size = input.size();
        if(null == input || size < span || span < 2) {return;}// no calculation of moving average possible
        boolean want_variance = result.resultDepth() > 1;
        boolean want_sigma    = result.resultDepth() > 2;
        double sum    = 0.0;
        double square = 0.0;
        for(int i = 0; i < size; i++) {
            double value = input.get(i,0); 
            sum    += value;
            square += value * value; 
            if(i >= span) {
                double old_value = input.get(i - span, 0); 
                sum    -= old_value;
                square -= old_value * old_value;
            }
            if(i >= (span - 1)) {
                double mean = sum / span;
                result.setResult(i, 0, mean);
                if(want_variance) {// moving variance
                    double variance = (square - sum * sum / span)/(span - 1);
                    result.setResult(i, 1, variance);
                    if(want_sigma) {// moving standard deviation
                		double sigma    = Math.sqrt(variance);
                        result.setResult(i, 2, sigma);
                    }
                }
            } else {
				double sp = Math.min(span, i + 1);
                double mean = sum / sp;
                result.setResult(i, 0, mean);
                if(want_variance) {// moving variance
					sp = Math.max(sp, 2);
                    double variance = (square - sum * sum / sp)/(sp - 1);
                    result.setResult(i, 1, variance);
                    if(want_sigma) {// moving standard deviation
                		double sigma    = Math.sqrt(variance);
                        result.setResult(i, 2, sigma);
                    }
                }				
            }
        }
    }

    public static void constmultiply(DoubleSequence input, int incolumn, DoubleSequenceResult result, int outcolumn, double constant) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > incolumn && result.resultDepth() > outcolumn;
        int size = input.size();
        int depth = input.depth();
        for(int i = 0; i < size; i++) {
            result.setResult(i, incolumn, input.get(i,outcolumn) * constant);
        }
    }
}
