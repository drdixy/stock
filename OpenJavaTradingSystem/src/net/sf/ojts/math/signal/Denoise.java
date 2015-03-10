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
package net.sf.ojts.math.signal;

import JSci.maths.wavelet.FWTCoef;
import JSci.maths.wavelet.Signal;
import JSci.maths.wavelet.daubechies2.Daubechies2;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.DoubleSequenceResult;

/**
 * @author cs
 *
 */
public class Denoise {

    public static void wavelet(DoubleSequence input, int incolumn, DoubleSequenceResult result, int outcolumn, double denoiselevel) {
        assert result != null && input != null && input.size() == result.size() && input.depth() > incolumn && result.resultDepth() > outcolumn;

        int size = input.size();
        
		// also build the noisy closing day values which have to be a power of 2
        Daubechies2 filter = new Daubechies2();
        //MultiSplineHaar filter = new MultiSplineHaar();
		int log2 = (int) Math.ceil(Math.log(size) / Math.log(2));
		int len  = (int) Math.pow(2, log2) + filter.getFilterType();
		int offset = (len - size) / 2;
		
		double[] noisy = new double[len];
		for(int i = 0; i < offset; i++) {
		    noisy[i] = input.get(0,incolumn);
		}
		for(int i = 0; i < size; i++) {
		    noisy[offset + i] = input.get(i,incolumn);
		}
		for(int i = offset + size; i < len; i++) {
		    noisy[i] = input.get(size - 1,incolumn);
		}        
        Signal signal = new Signal( noisy );
        
        signal.setFilter( filter );
        int level = Math.min( log2 - 4, 20 );
        //int level = 1;
        FWTCoef signalCoeffs = signal.fwt( level ); // level is the number of iterations
        /*
        double[][] coefs = new double[7 + 1][];
        {
            double[] tmp1 = {0}; 
            double[] tmp2 = {5}; 
            coefs[0] = tmp1;
            coefs[coefs.length - 1] = tmp2;            
        }
        for(i = coefs.length - 2; i > 0; i--) {
            coefs[i] = new double[(int)Math.pow(2,coefs.length - i - 1)];
            for(int j = 0; j < coefs[i].length; j++) {
                coefs[i][j] = 0;
            }
        }
        */
        
        //FWTCoef signalCoeffs = new FWTCoef(coefs);
        //signalCoeffs.denoise( 0.1 );
        signalCoeffs.denoise( denoiselevel );
        double[] rebuild = signalCoeffs.rebuildSignal( filter ).evaluate( 0 );// 0 is the number of iterations

		for(int i = 0; i < size; i++) {
		    result.setResult(i, outcolumn, rebuild[offset + i]);
		}
        
        Unit unit = input.getUnitForColumn(incolumn);
        result.setUnitForColumn(outcolumn, unit);
    }
    
}
