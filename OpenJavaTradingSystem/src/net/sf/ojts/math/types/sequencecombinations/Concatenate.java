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
package net.sf.ojts.math.types.sequencecombinations;

import net.sf.ojts.jdo.Unit;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.util.DoubleSequenceUtil;

/**
 * @author cs
 *
 */
public class Concatenate implements DoubleSequence {
    
    protected DoubleSequence[] sequences_ = new DoubleSequence[0];
    protected int size  = -1;
    protected int depth = -1;

    public Concatenate(DoubleSequence[] sequences) {
        assert null != sequences;
        sequences_ = sequences;
        depth = 0;
        size  = 0;
        for(int i = 0; i < sequences_.length; i++) {
            if(0 == i)
                size = sequences_[i].size();
            depth += sequences_[i].depth();
            assert size == sequences_[i].size();
        }
    }
    
    protected int[] convertDepthToSequenceAndDepth(int depth) {
        int i = 0;
        for(; i < sequences_.length; i++) {
            depth -= sequences_[i].depth();
            if(depth < 0)
                break;
        }
        assert depth < 0;//otherwise something went wrong!
        depth += sequences_[i].depth();
        return new int[]{i,depth};
    }
    
    public double[] current() {
        double[] result = new double[depth];
        int k = 0;
        for(int i = 0; i < sequences_.length; i++) {
            double[] current = sequences_[i].current();
            for(int j = 0; j < current.length; j++) {
                result[k] = current[j];
                k++;
            }
        }        
        return result;
    }
    public double current(int depth) {
        int[] seq_and_depth = convertDepthToSequenceAndDepth(depth);
        int seq = seq_and_depth[0];
        depth = seq_and_depth[1];
        return sequences_[seq].current(depth);
    }
    public int dec() {
        int result = -1;
        for(int i = 0; i < sequences_.length; i++) {
            int dec = sequences_[i].dec();
            if(0 == i)
                result = dec;
            assert result == dec;
        }
        return result;
    }
    public int depth() {
        return depth;
    }
    public double get(int index, int depth) {
        int[] seq_and_depth = convertDepthToSequenceAndDepth(depth);
        int seq = seq_and_depth[0];
        depth = seq_and_depth[1];
        return sequences_[seq].get(index, depth);
    }
    public double[] get(int index) {
        double[] result = new double[depth];
        int k = 0;
        for(int i = 0; i < sequences_.length; i++) {
            double[] current = sequences_[i].get(index);
            for(int j = 0; j < current.length; j++) {
                result[k] = current[j];
                k++;
            }
        }        
        return result;
    }
    public int getIndex() {
        int result = -1;
        for(int i = 0; i < sequences_.length; i++) {
            int idx = sequences_[i].getIndex();
            if(0 == i)
                result = idx;
            assert result == idx;
        }
        return result;
    }
    public int getIndexForLabel(Object label) {
        int result = -1;
        for(int i = 0; i < sequences_.length; i++) {
            int idx = sequences_[i].getIndexForLabel(label);
            if(0 == i)
                result = idx;
            assert result == idx;
        }
        return result;
    }
    public Object getLabel() {
        Object result = null;
        for(int i = 0; i < sequences_.length; i++) {
            Object label = sequences_[i].getLabel();
            if(0 == i)
                result = label;
            assert result.equals(label);
        }
        return result;
    }
    public Object getLabel(int index) {
        Object result = null;
        for(int i = 0; i < sequences_.length; i++) {
            Object label = sequences_[i].getLabel(index);
            if(0 == i)
                result = label;
            assert result.equals(label);
        }
        return result;
    }
    public Unit getUnitForColumn(int cindex) {
        int[] seq_and_depth = convertDepthToSequenceAndDepth(depth);
        int seq = seq_and_depth[0];
        cindex = seq_and_depth[1];
        return sequences_[seq].getUnitForColumn(cindex);
    }
    public boolean hasNext() {
        boolean result = false;
        for(int i = 0; i < sequences_.length; i++) {
            boolean hn = sequences_[i].hasNext();
            if(0 == i)
                result = hn;
            assert result == hn;
        }
        return result;
    }
    public int inc() {
        int result = -1;
        for(int i = 0; i < sequences_.length; i++) {
            int inc = sequences_[i].inc();
            if(0 == i)
                result = inc;
            assert result == inc;
        }
        return result;
    }
    public double[] next() {
        double[] result = new double[depth];
        int k = 0;
        for(int i = 0; i < sequences_.length; i++) {
            double[] current = sequences_[i].next();
            for(int j = 0; j < current.length; j++) {
                result[k] = current[j];
                k++;
            }
        }        
        return result;
    }
    public void rewind() {
        for(int i = 0; i < sequences_.length; i++) {
            sequences_[i].rewind();
        }        
    }
    public int size() {
        return size;
    }
    public double[][] toMatrix() {
        double[][] result = new double[size][];
        rewind();
        int i = 0;
        while(hasNext()) {
            result[i] = next(); i++;
        }
        rewind();
        return result;
    }

    public String toString() {
        return DoubleSequenceUtil.prettyPrint(this, true);
    }
}
