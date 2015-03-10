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
 * Created on 10.04.2005
 *
 */
package net.sf.ojts.functionality;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Date;

import net.sf.ojts.datainput.exceptions.CannotHandleSubjectException;
import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.exceptions.OJTSFunctionalityException;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.DoubleDataItem;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.vpd.OHLCDataItem;
import net.sf.ojts.math.types.vpd.OHLCDatasetImpl;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer;
import net.sf.ojts.math.types.vpd.XYDatasetImpl;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer.VPDDoubleSequence;
import net.sf.ojts.util.DateUtil;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.PersistenceException;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;

/**
 * @author cs
 *
 */
public class DataAnalysisAndVisualisation {

    static class ChartWindowAdapter extends WindowAdapter {
        ChartFrame frame = null;        
        public ChartWindowAdapter(ChartFrame frame) {this.frame = frame;}        
    	public void windowClosing(WindowEvent e) {frame.dispose();}
    }

    public static final int OPENING_DAY_PRICE  = 1;//"OPENING-DAY-PRICE"
    public static final int MAX_DAY_PRICE      = 2;//"MAX-DAY-PRICE";
    public static final int MIN_DAY_PRICE      = 3;//"MIN-DAY-PRICE";
    public static final int CLOSING_DAY_PRICE  = 4;//"CLOSING-DAY-PRICE";
    public static final int TRADING_DAY_VOLUME = 5;//"TRADING-DAY-VOLUME";        

    public static ValuesPerDateContainer getDataForSecurityPaper(DataSource observer_and_type, Date sdate0, Date edate0, SecurityPaper subject, MarketPlace market, int what) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
        return getDataForSecurityPaper(observer_and_type, sdate0, edate0, subject, market, what, 0);
    }
    
    public static ValuesPerDateContainer getDataForSecurityPaper(DataSource observer_and_type, Date sdate0, Date edate0, SecurityPaper subject, MarketPlace market, int what, int extra_columns_count) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
    	Logger log = Logger.getLogger(DataAnalysisAndVisualisation.class);
    	
    	ValuesPerDateContainer result = new ValuesPerDateContainer(1 + extra_columns_count, new Unit[1 + extra_columns_count]);
    	
        DataInput.fetchData(observer_and_type, sdate0, edate0, subject, market);
    	
    	String swhat = "";
    	switch(what) {
    		case OPENING_DAY_PRICE:
    		    swhat = "OPENING-DAY-PRICE";
    		    break;
    		case MAX_DAY_PRICE:
    		    swhat = "MAX-DAY-PRICE";
    		    break;
    		case MIN_DAY_PRICE:
    		    swhat = "MIN-DAY-PRICE";
    		    break;
    		case CLOSING_DAY_PRICE:
    		    swhat = "CLOSING-DAY-PRICE";
    		    break;
    		case TRADING_DAY_VOLUME:
    		    swhat = "TRADING-DAY-VOLUME";
    		    break;
    		default:
    		    swhat = "CLOSING-DAY-PRICE";
    	}
    
    	String       query       = null;
    	Object[]     results     = new Object[0];		
    	{
    		Date sdate = DateUtil.truncate(sdate0);
    	    Date edate = DateUtil.truncate(DateUtil.getToday());
    	    if(null != edate0)
    	        edate = DateUtil.truncate(edate0);
    		query = "SELECT ddi FROM "
    			+ DoubleDataItem.class.getName()
    			+ " ddi WHERE ddi.subject = $1 and ddi.source.observedAt = $2 and ddi.source.observerDataSource = $3 and ddi.time >= $4 and ddi.time <= $5 and ddi.source.property.name = $6";
    		Object[] query_arguments = {subject, market, observer_and_type, sdate, edate, swhat};
    		//Object[] query_arguments = {subject, market, observer, DateUtil.formatDate(sdate), DateUtil.formatDate(edate)};
    		results = DBAccess.getObjectsFromDatabase(query, query_arguments);
    	}
    	
    	int size = results.length;
    	net.sf.ojts.pjdo.DoubleDataItem[] dataitem = new net.sf.ojts.pjdo.DoubleDataItem[size];
    	for(int i = 0; i < results.length; i++) {
    	    DoubleDataItem ddi = (DoubleDataItem) results[i];
    	    Date date = ddi.getTime();
    	    if(0 == i)
    	        result.setUnitForColumn(0, ddi.getUnit());
    	    
    	    try {
    	        double[] item = new double[1 + extra_columns_count];
    	        item[0] = ddi.getValue();
    	        result.addItem(date, item);
    	    } catch(Exception e) {
    	        log.error("Unexpected exception??", e);
    	    }
    	}
    	
        return result;        
    }
    
    public static DoubleSequence getOHLCForSecurityPaper(DataSource observer_and_type, Date start, Date end, SecurityPaper subject, MarketPlace market) throws PersistenceException, OJTSFunctionalityException, DBException, CannotHandleSubjectException, IOException {
    	Logger log = Logger.getLogger(DataAnalysisAndVisualisation.class);
    	
        ValuesPerDateContainer data_opening = DataAnalysisAndVisualisation.getDataForSecurityPaper(observer_and_type, start, end, subject, market, DataAnalysisAndVisualisation.OPENING_DAY_PRICE);
        ValuesPerDateContainer data_min     = DataAnalysisAndVisualisation.getDataForSecurityPaper(observer_and_type, start, end, subject, market, DataAnalysisAndVisualisation.MIN_DAY_PRICE);
        ValuesPerDateContainer data_max     = DataAnalysisAndVisualisation.getDataForSecurityPaper(observer_and_type, start, end, subject, market, DataAnalysisAndVisualisation.MAX_DAY_PRICE);
        ValuesPerDateContainer data_closing = DataAnalysisAndVisualisation.getDataForSecurityPaper(observer_and_type, start, end, subject, market, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);
        ValuesPerDateContainer data_volume  = DataAnalysisAndVisualisation.getDataForSecurityPaper(observer_and_type, start, end, subject, market, DataAnalysisAndVisualisation.TRADING_DAY_VOLUME);

        ValuesPerDateContainer[] containers = {data_opening, data_max, data_min, data_closing, data_volume};
        ValuesPerDateContainer data = ValuesPerDateContainer.merge(containers, 0);
        
        int[] indices = {0,1,2,3,4};
        VPDDoubleSequence sequence = data.new VPDDoubleSequence(indices, indices);
        return sequence;
    }

    public static org.jfree.data.xy.OHLCDataItem[] convertToJFreeChartFormat(OHLCDataItem[] ojts_format) {
        if(null == ojts_format)
            return null;
        org.jfree.data.xy.OHLCDataItem[] result = new org.jfree.data.xy.OHLCDataItem[ojts_format.length];
        for(int i = 0; i < ojts_format.length; i++) {
            result[i] = new org.jfree.data.xy.OHLCDataItem(ojts_format[i].getDate(),ojts_format[i].getOpen(), ojts_format[i].getHigh(), ojts_format[i].getLow(), ojts_format[i].getClose(), ojts_format[i].getVolume());
        }
                
        return result;
    }
    
    public static JFreeChart createXYChart(String chartname, DoubleSequence input, String[] sequence_names, boolean autorange_includes_zero, boolean is_x_date_axis) {
        XYDatasetImpl dataset = new XYDatasetImpl(input, sequence_names, is_x_date_axis);

        ValueAxis xAxis = null;
        if(is_x_date_axis) {
            DateAxis axis = new DateAxis("Date");
            axis.setAutoTickUnitSelection(true);
            xAxis = axis;
        } else {
            NumberAxis axis = new NumberAxis("ItemCount");
            axis.setAutoTickUnitSelection(true);
            xAxis = axis;
        }
    	NumberAxis yAxis = new NumberAxis("Value");
    	yAxis.setAutoRangeIncludesZero(autorange_includes_zero);
    	StandardXYToolTipGenerator labelGenerator = new StandardXYToolTipGenerator();
    	XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new StandardXYItemRenderer(StandardXYItemRenderer.LINES, labelGenerator));
        
        JFreeChart chart = new JFreeChart(chartname, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        return chart;
    }

    public static JFreeChart createCandlestickChartForSecurityPaper(DataSource observer_and_type, Date start, Date end, SecurityPaper subject, MarketPlace market)  throws PersistenceException, OJTSFunctionalityException, DBException, CannotHandleSubjectException, IOException {
    	Logger log = Logger.getLogger(DataAnalysisAndVisualisation.class);
        DoubleSequence sequence = getOHLCForSecurityPaper(observer_and_type, start, end, subject, market);
        String seriesname = subject.getName();
        String[] seriesnames = {seriesname};
        OHLCDatasetImpl dataset = new OHLCDatasetImpl(sequence, seriesnames);
        DateAxis   xAxis = new DateAxis("day");
    	NumberAxis yAxis = new NumberAxis("price");
    	yAxis.setAutoRangeIncludesZero(false);
    	TickUnits tick_units = new TickUnits();
    	tick_units.add(new DateTickUnit(DateTickUnit.DAY,1));
        HighLowItemLabelGenerator labelGenerator = new HighLowItemLabelGenerator();
    	XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new CandlestickRenderer(-1.0, true, labelGenerator));
    	
    	if(null == end)
    	    end = DateUtil.truncate(DateUtil.getToday());
    	
        JFreeChart chart = new JFreeChart("prices for " + subject.getName() + " between " + DateUtil.formatDate(start) + " and " + DateUtil.formatDate(end), JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }

    public static void displayChart(JFreeChart chart) {
    	Logger log = Logger.getLogger(DataAnalysisAndVisualisation.class);
    	try {
    		ChartFrame frame = new ChartFrame("Chart", chart);
    		frame.pack();
    		frame.setLocation(200, 300);
    		frame.setVisible(true);
    		frame.addWindowListener(new ChartWindowAdapter(frame));
    	} catch (Exception e) {
    		log.error("Exception in displayChart!", e);
    	}        
    }

}
