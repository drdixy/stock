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
package net.sf.ojts.math.types.vpd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.ojts.jdo.Unit;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.DoubleSequenceResult;
import net.sf.ojts.math.types.util.CompareAndMeasure;
import net.sf.ojts.math.types.util.DoubleSequenceUtil;
import net.sf.ojts.math.types.util.Interpolate;
import net.sf.ojts.math.types.util.InterpolationStrategy;
import net.sf.ojts.math.types.util.LinearInterpolationStrategy;


/**
 * @author cs
 *
 */
public class ValuesPerDateContainer {

    public static class DateOrderComparator implements Comparator {
        public int     compare(Object o1, Object o2) {
            if(null != o1 && null != o2 && o1 instanceof ValuesPerDate && o2 instanceof ValuesPerDate) {
                ValuesPerDate di1 = (ValuesPerDate) o1;
                ValuesPerDate di2 = (ValuesPerDate) o2;
                return di1.getDate().compareTo(di2.getDate());
            } else {
                throw new ClassCastException("Could not cast o1 or o2 to type ValuesPerDate");
            }
        }
        
        public boolean equals(Object obj) {
            return false;
        }
    }

    public static class DateAsKeyComparator implements Comparator {
        public int     compare(Object o1, Object o2) {
            if(null != o1 && null != o2 && o1 instanceof ValuesPerDate && o2 instanceof Date) {
                ValuesPerDate di1 = (ValuesPerDate) o1;
                Date di2 = (Date) o2;
                return di1.getDate().compareTo(di2);
            } else {
                throw new ClassCastException("Could not cast o1 or o2 to type ValuesPerDate");
            }
        }
        
        public boolean equals(Object obj) {
            return false;
        }
    }
    
    public static class DateCompareAndMeasure implements CompareAndMeasure {
                
        public double difference(Object o1, Object o2) {
            assert null != o1 && null != o2 && o1 instanceof Date && o2 instanceof Date;// throw new ClassCastException("Could not cast o1 or o2 to type Date")
            Date d1 = (Date) o1;
            Date d2 = (Date) o2;
            return d1.getTime() - d2.getTime();
        }
        public int compare(Object o1, Object o2) {
            assert null != o1 && null != o2 && o1 instanceof Date && o2 instanceof Date;// throw new ClassCastException("Could not cast o1 or o2 to type Date")
            Date d1 = (Date) o1;
            Date d2 = (Date) o2;
            return d1.compareTo(d2);
        }
    }
    
    public class VPDDoubleSequence implements DoubleSequence, DoubleSequenceResult {
        
        protected int   index_i_           = -1;
        protected int   index_j_           =  0;
        
        protected int[] index_j_selection_ = new int[0];
        protected int[] index_r_selection_ = new int[0];

        public VPDDoubleSequence(int selection_size, int start_offset) {
            assert selection_size + start_offset <= values_size_;
            int[] index_selection = new int[selection_size];
            for(int j = 0; j < index_selection.length; j++) {
                index_selection[j] = start_offset + j;
            }
            index_r_selection_ = index_selection; 
            index_j_selection_ = index_selection; 
        }
        
        public VPDDoubleSequence(int[] index_selection, boolean isResult) {
            if(isResult)
                index_r_selection_ = index_selection; 
            else
                index_j_selection_ = index_selection; 
        }

        public VPDDoubleSequence(int[] index_j_selection, int[] index_r_selection) {
            index_j_selection_ = index_j_selection; 
            index_r_selection_ = index_r_selection; 
        }
        
        public int[] getIndexJSelection() {
            return index_j_selection_;
        }
        
        public int[] getIndexRSelection() {
            return index_r_selection_;
        }
        
        public Unit getUnitForColumn(int cindex) {
            return columnunits_[index_j_selection_[cindex]];
        }
        public void setUnitForColumn(int cindex, Unit unit) {
            columnunits_[index_j_selection_[cindex]] = unit;
        }
        
        public int getIndexForLabel(Object label) {
            int result = -1;
            
            DateAsKeyComparator compare = new DateAsKeyComparator();
            result = Collections.binarySearch(content_, label, compare);
            
            return result;
        }
        
        public double current(int depth) {
            if(index_i_ < 0 || index_i_ >= size())
                throw new ArrayIndexOutOfBoundsException("index_i_ '" + index_i_ + "' not in range: [" + 0 + "," + size() + "]");
            if(depth < 0 || depth >= depth())
                throw new ArrayIndexOutOfBoundsException("depth '" + depth + "' not in range: [" + 0 + "," + depth() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(index_i_);
            return vpd.getValues()[index_j_selection_[depth]];
        }
        public double[] current() {
            if(index_i_ < 0 || index_i_ >= size())
                throw new ArrayIndexOutOfBoundsException("index_i_ '" + index_i_ + "' not in range: [" + 0 + "," + size() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(index_i_);
            double[] result = new double[depth()];
            double[] base   = vpd.getValues(); 
            for (int i = 0; i < result.length; i++) {
                result[i] = base[index_j_selection_[i]];
            }
            return result;
        }
        public double[] get(int index) {
            if(index < 0 || index >= size())
                throw new ArrayIndexOutOfBoundsException("index '" + index + "' not in range: [" + 0 + "," + size() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(index);
            double[] result = new double[depth()];
            double[] base   = vpd.getValues(); 
            for (int i = 0; i < result.length; i++) {
                result[i] = base[index_j_selection_[i]];
            }
            return result;
        }
        public int getIndex() {
            return index_i_;
        }
        public int dec() {
            index_i_--;
            if(index_i_ < -1) index_i_ = -1;
            return index_i_;
        }
        public int inc() {
            index_i_++;
            if(index_i_ > size())
                index_i_ = size();
            return index_i_;
        }
        public boolean hasNext() {
            return index_i_ < size() - 1;
        }
        public double[] next() {
            inc();
            return current();
        }
        public double next(int depth) {
            inc();
            return current(depth);
        }
        public void rewind() {
            index_i_ = -1;
        }
        public int depth() {
            return index_j_selection_.length;
        }
        public double get(int index, int depth) {
            if(index < 0 || index >= size())
                throw new ArrayIndexOutOfBoundsException("index '" + index + "' not in range: [" + 0 + "," + size() + "]");
            if(depth < 0 || depth >= depth())
                throw new ArrayIndexOutOfBoundsException("depth '" + depth + "' not in range: [" + 0 + "," + depth() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(index);
            return vpd.getValues()[index_j_selection_[depth]];
        }
        public Object getLabel() {
            if(index_i_ < 0 || index_i_ > size())
                throw new ArrayIndexOutOfBoundsException("index_i_ '" + index_i_ + "' not in range: [" + 0 + "," + size() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(index_i_);
            return vpd.getDate();
        }
        public Object getLabel(int index) {
            if(index < 0 || index > size())
                throw new ArrayIndexOutOfBoundsException("index '" + index + "' not in range: [" + 0 + "," + size() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(index);
            return vpd.getDate();
        }
        public int size() {
            return content_.size();
        }
        public double[][] toMatrix() {
            int size  = size();
            int depth = depth();
            double[][] result = new double[size][depth];
            for(int i = 0; i < size; i++)
                result[i] = get(i);            
            return result;
        }
        
        public void   setResult(int result_index, double result) {
            setResult(index_i_, result_index, result);
        }
        public double getResult(int result_index) {
            return getResult(index_i_, result_index);
        }        
        
        public void   setResult(int input_index, int result_index, double result) {
            if(input_index < 0 || input_index >= size())
                throw new ArrayIndexOutOfBoundsException("input_index '" + input_index + "' not in range: [" + 0 + "," + size() + "]");
            if(result_index < 0 || result_index >= resultDepth())
                throw new ArrayIndexOutOfBoundsException("result_index '" + result_index + "' not in range: [" + 0 + "," + resultDepth() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(input_index);
            vpd.getValues()[index_r_selection_[result_index]] = result;
        }
        public double getResult(int input_index, int result_index) {
            if(input_index < 0 || input_index >= size())
                throw new ArrayIndexOutOfBoundsException("input_index '" + input_index + "' not in range: [" + 0 + "," + size() + "]");
            if(result_index < 0 || result_index >= resultDepth())
                throw new ArrayIndexOutOfBoundsException("result_index '" + result_index + "' not in range: [" + 0 + "," + resultDepth() + "]");
            ValuesPerDate vpd = (ValuesPerDate) content_.get(result_index);
            return vpd.getValues()[index_r_selection_[result_index]];
        }

        public int resultDepth() {
            return index_r_selection_.length;
        }

        public String toString() {
            return DoubleSequenceUtil.prettyPrint(this, true);
        }
    }
    
    
    protected Comparator comparator_    = new DateOrderComparator();
    protected Comparator dakcomparator_ = new DateAsKeyComparator();

    protected int        values_size_   = -1;
    protected ArrayList  content_       = new ArrayList();
    protected Date       first_date_    = null;
    protected Date       last_date_     = null;
    protected Unit[]     columnunits_   = new Unit[0];
    
    public ValuesPerDateContainer(int values_size, Unit[] columnunits) {
        super();
        values_size_ = values_size;
        columnunits_ = columnunits;
        if(columnunits_.length != values_size_)
            throw new ArrayIndexOutOfBoundsException("columnunits_.length: " + columnunits_.length + " != values_size_: " + values_size_);
    }
    
    public void addItem(ValuesPerDate item) {
        assert null != item && item.getValues().length == values_size_;
        // Search for the non-existent item
        int index = Collections.binarySearch(content_, item, comparator_);
        
        if (index < 0) {
            // Add the non-existent item to the list
            content_.add(-index - 1, item);
        } else {
            // we have already an entry for that date, overwrite it
            // ?? not sure if this is the right action to perform ??
            content_.set(index, item);
        }
        Date date = item.getDate();
        if(null == first_date_ || date.before(first_date_))
            first_date_ = date;
        if(null == last_date_ || date.after(last_date_))
            last_date_ = date;
    }

    public void addItem(Date date, double[] values) {
        assert null != values && values.length == values_size_;
        ValuesPerDate item = new ValuesPerDate(this, date, values);
        addItem(item);
    }

    public ValuesPerDate addItem(Date date) {
        double[] values = new double[values_size_];
        ValuesPerDate item = new ValuesPerDate(this, date, values);
        addItem(item);
        return item;
    }
    
    public void addItems(ValuesPerDate[] items) {
        for(int i = 0; i < items.length; i++) {
            addItem(items[i]);
        }
    }
    
    public ValuesPerDate getItem(int index) {
        return (ValuesPerDate) content_.get(index);
    }

    public ValuesPerDate getItem(Date date) {
        int index = Collections.binarySearch(content_, date, dakcomparator_);
        if(index < 0) return null;
        return (ValuesPerDate) content_.get(index);
    }
    
    public SortedSet getDateKeys() {
        TreeSet set = new TreeSet();
        
        int len = content_.size();
        for(int i = 0; i < len; i++) {
            ValuesPerDate vpd = (ValuesPerDate) content_.get(i);
            set.add(vpd.getDate());
        }
        
        return set;
    }

    public Date getFirstDate() {
        return first_date_;
    }
    public Date getLastDate() {
        return last_date_;
    }
    public int getValuesSize() {
        return values_size_;
    }
    public ValuesPerDate[] getContent() {
        ValuesPerDate[] result = new ValuesPerDate[content_.size()];
        return (ValuesPerDate[]) content_.toArray(result);
    }
    
    public Unit getUnitForColumn(int cindex) {
        return columnunits_[cindex];
    }

    public void setUnitForColumn(int cindex, Unit unit) {
        columnunits_[cindex] = unit;
    }
    
    public int size() {
        return content_.size();
    }
    
    public static ValuesPerDateContainer merge(ValuesPerDateContainer[] containers, int extra_values_size) {		
        InterpolationStrategy  strategy = new LinearInterpolationStrategy();    
		return merge(containers, strategy, extra_values_size);
    }
    public static ValuesPerDateContainer merge(ValuesPerDateContainer[] containers, InterpolationStrategy strategy, int extra_values_size) {
		InterpolationStrategy[] strategies = new InterpolationStrategy[containers.length];
		for (int i = 0; i < strategies.length; i++) {
			strategies[i] = strategy;
		}
		return merge(containers, strategies, extra_values_size);
    }
    public static ValuesPerDateContainer merge(ValuesPerDateContainer[] containers, InterpolationStrategy strategies[], int extra_values_size) {
		assert containers.length == strategies.length;
        if(null == containers || containers.length == 0)
            return null;
        TreeSet dates = new TreeSet();
        int values_size = extra_values_size;
        for(int i = 0; i < containers.length; i++) {
            values_size += containers[i].getValuesSize();
            dates.addAll(containers[i].getDateKeys());
        }
        
        ValuesPerDateContainer result = new ValuesPerDateContainer(values_size, new Unit[values_size]);
        
        int size = dates.size();
        Iterator iter = dates.iterator();
        while(iter.hasNext()) {
            double[] values = new double[values_size];
            Date date = (Date) iter.next();
            result.content_.add(new ValuesPerDate(result, date, values));
        }
        
        CompareAndMeasure compare = new DateCompareAndMeasure();
        int index_offset = 0;
        for(int i = 0; i < containers.length; i++) {
            ValuesPerDateContainer container = containers[i];
	        InterpolationStrategy  is = strategies[i];
            int vs = container.getValuesSize();
            
            for(int j = 0; j < vs; j++) {
                result.setUnitForColumn(index_offset + j, container.getUnitForColumn(j));
            }
            
            
            if(result.size() == container.size()) {
                // no interpolation necessary, because result is supposed to be a superset of container and therefore
                // if their sizes match they have the same number of elements.

                for(int l = 0; l < size; l++) {
                    ValuesPerDate vpd_result = (ValuesPerDate) result.content_.get(l);
                    double[] result_values = vpd_result.getValues(); 
                    ValuesPerDate vpd_input  = (ValuesPerDate) container.content_.get(l);
                    double[] input_values = vpd_input.getValues(); 
                    for(int m = 0; m < vs; m++) {
                        result_values[index_offset + m] = input_values[m]; 
                    }                    
                }                
            } else if (container.size() == 0){
                // all values are empty and therefore should be initialized with 0
                for(int j = 0; j < vs; j++) {
                    result.setUnitForColumn(index_offset + j, new Unit());
                }

                for(int l = 0; l < size; l++) {
                    ValuesPerDate vpd_result = (ValuesPerDate) result.content_.get(l);
                    double[] result_values = vpd_result.getValues(); 
                    for(int m = 0; m < vs; m++) {
                        result_values[index_offset + m] = 0;
                    }                    
                }                
            } else {
                DoubleSequence    ds_input_to_interpolate = container.new VPDDoubleSequence(vs, 0);
                VPDDoubleSequence drs_interplation_result = result.new VPDDoubleSequence(vs, index_offset);
                Interpolate.interpolate(drs_interplation_result, ds_input_to_interpolate, drs_interplation_result, compare, is);
            }
            index_offset += vs;
        }
        for(int i = 0; i < extra_values_size; i++) {
            result.setUnitForColumn(index_offset + i, new Unit());            
        }
        
        return result;
    }
    
    public String toString() {
        VPDDoubleSequence sequence = this.new VPDDoubleSequence(getValuesSize(),0); 		
        return DoubleSequenceUtil.prettyPrint(sequence, true);
    }
    
}
