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
 * Created on 07.05.2005
 *
 */

package net.sf.ojts.math.types.sequencecombinations;

import net.sf.ojts.jdo.Unit;
import net.sf.ojts.math.types.DoubleSequenceResult;

/**
 * @author cs
 *
 */
public class ReorderResult implements DoubleSequenceResult {

	DoubleSequenceResult base_  = null;

    int[]                order_ = new int[0];
    int                  depth_ = -1;

    public ReorderResult(DoubleSequenceResult base, int[] order) {
        assert base.resultDepth() >= order.length;
        base_  = base;
        order_ = order;
        depth_ = order_.length;
    }
	
    protected int convert(int oldindex) {
        assert oldindex < depth_;
        return order_[oldindex];
    }
	
	public double getResult(int input_index, int result_index) {
		result_index = convert(result_index);
		return base_.getResult(input_index, result_index);
	}

	public int resultDepth() {
		return depth_;
	}

	public void setResult(int input_index, int result_index, double result) {
		result_index = convert(result_index);
		base_.setResult(input_index, result_index, result);
	}

	public void setUnitForColumn(int cindex, Unit unit) {
		cindex = convert(cindex);
		base_.setUnitForColumn(cindex, unit);
	}

	public int size() {
		return base_.size();
	}
		
}
