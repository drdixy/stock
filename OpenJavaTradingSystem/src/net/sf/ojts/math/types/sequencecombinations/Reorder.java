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
public class Reorder implements DoubleSequence {
    
    DoubleSequence base_  = null;
    int[]          order_ = new int[0];
    int            depth_ = -1;

    public Reorder(DoubleSequence base, int[] order) {
        assert base.depth() >= order.length;
        base_  = base;
        order_ = order;
        depth_ = order_.length;
    }
    
    protected int convert(int oldindex) {
        assert oldindex < depth_;
        return order_[oldindex];
    }
    
    public double[] current() {
        double[] result = new double[depth_];
        for(int i = 0; i < depth_; i++) {
            result[i] = current(i);
        }
        return result;
    }
    public double current(int depth) {
        depth = convert(depth);
        return base_.current(depth);
    }
    public int dec() {
        return base_.dec();
    }
    public int depth() {
        return depth_;
    }
    public double[] get(int index) {
        double[] result = new double[depth_];
        for(int i = 0; i < depth_; i++) {
            result[i] = get(index, i);
        }
        return result;
    }
    public double get(int index, int depth) {
        depth = convert(depth);
        return base_.get(index, depth);
    }
    public int getIndex() {
        return base_.getIndex();
    }
    public int getIndexForLabel(Object label) {
        return base_.getIndexForLabel(label);
    }
    public Object getLabel() {
        return base_.getLabel();
    }
    public Object getLabel(int index) {
        return base_.getLabel(index);
    }
    public Unit getUnitForColumn(int cindex) {
        cindex = convert(cindex);
        return base_.getUnitForColumn(cindex);
    }
    public boolean hasNext() {
        return base_.hasNext();
    }
    public int inc() {
        return base_.inc();
    }
    public double[] next() {
        base_.next();
        return current();
    }
    public void rewind() {
        base_.rewind();
    }
    public int size() {
        return base_.size();
    }
    public double[][] toMatrix() {
        double[][] result = new double[base_.size()][];
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
