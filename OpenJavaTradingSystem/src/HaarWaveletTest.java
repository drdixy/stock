/*
 * Created on 02.11.2004 by cs
 *
 */

/**
 * @author cs
 *
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import net.sf.ojts.jdo.DoubleDataItem;
import net.sf.ojts.util.ConfigurationHandler;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.JDO;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import JSci.maths.wavelet.FWTCoef;
import JSci.maths.wavelet.Signal;
import JSci.maths.wavelet.daubechies2.Daubechies2;

public class HaarWaveletTest {

	public static void main(String[] args) {
		ConfigurationHandler.initLog4j("conf/log4j.properties", true);
		ConfigurationHandler.setRb("conf/jts.properties");

		String dbname = "jts";
		JDO    jdo    = new JDO();
		jdo.setDatabaseName( dbname );
		jdo.setConfiguration( "castor/database.xml" );
		jdo.setClassLoader( CastorTest.class.getClassLoader() );

		TreeMap      ohlcresults = new TreeMap();
		String       query       = null;
		Database     db          = null;
		OQLQuery     oql         = null;
		QueryResults results     = null;
		int          size        = -1;
		try {
    		ConfigurationHandler.setJDO(jdo, dbname);
			ConfigurationHandler.openDatabase();
			db = ConfigurationHandler.getDatabase();
			
			query = "SELECT ddi FROM "
				+ DoubleDataItem.class.getName()
				+ " ddi WHERE ddi.subject.name = $1 and ddi.source.observedAt.name = $2 and ddi.source.observerDataSource.observerLink.name = $3 ";
			oql = db.getOQLQuery(query);
			oql.bind("DE0007500001");
			oql.bind("XETRA");
			oql.bind("yahoo");
			results = oql.execute(true);
			size = results.size() / 5;//number of days
			while (results.hasMore()) {
			    DoubleDataItem ddi = (DoubleDataItem) results.next();
			    Date date = ddi.getTime();
			    double[] ohlc = (double[]) ohlcresults.get(date);
			    if(null == ohlc) {
			        ohlc = new double[5];
			        ohlcresults.put(date, ohlc);
			    }
			    if(ddi.getProperty().getName().equals("OPENING-DAY-PRICE")) {
			        ohlc[0] = ddi.getValue();
			    } else if(ddi.getProperty().getName().equals("MAX-DAY-PRICE")) {
			        ohlc[1] = ddi.getValue();
			    } else if(ddi.getProperty().getName().equals("MIN-DAY-PRICE")) {
			        ohlc[2] = ddi.getValue();
			    } else if(ddi.getProperty().getName().equals("CLOSING-DAY-PRICE")) {
			        ohlc[3] = ddi.getValue();
			    } else if(ddi.getProperty().getName().equals("TRADING-DAY-VOLUME")) {
			        ohlc[4] = ddi.getValue();
			    }
			}    
			if(ohlcresults.size() != size) {
			    throw new Exception("something went wront!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			ConfigurationHandler.closeDatabase();			
		}
		
	    OHLCDataItem[] dataitem = new OHLCDataItem[size];
	    int i = 0;
		for (Iterator iter = ohlcresults.keySet().iterator(); iter.hasNext();) {
		    Date     date = (Date) iter.next();
		    double[] data = (double[]) ohlcresults.get(date);
            dataitem[i] = new OHLCDataItem(date, data[0], data[1], data[2], data[3], data[4]);
            i++;
        }
		
		// also build the noisy closing day values which have to be a power of 2
        Daubechies2 filter = new Daubechies2();
        //MultiSplineHaar filter = new MultiSplineHaar();
		int log2 = (int) Math.ceil(Math.log(size) / Math.log(2));
		int len  = (int) Math.pow(2, log2) + filter.getFilterType();
		int offset = (len - size) / 2;
		double[] noisy = new double[len];
		for(i = 0; i < offset; i++) {
		    noisy[i] = dataitem[0].getClose().doubleValue();
		}
		for(i = 0; i < size; i++) {
		    noisy[offset + i] = dataitem[i].getClose().doubleValue();
		}
		for(i = offset + size; i < len; i++) {
		    noisy[i] = dataitem[size - 1].getClose().doubleValue();
		}
		/*
		{
		    double[] tmp = {-1,1};
			noisy = tmp; 
		}
		*/
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
        signalCoeffs.denoise( 0.1 );
        double[] rebuild = signalCoeffs.rebuildSignal( filter ).evaluate( 0 );// 0 is the number of iterations
	    XYSeries xynoisy = new XYSeries("noisy", true, false);
	    for(i = 0; i < noisy.length; i++) {
	        xynoisy.add(i, noisy[i], false);
	    }
	    XYSeries xydenoise = new XYSeries("denoise", true, false);
	    for(i = offset; i < offset + size; i++) {
	        xydenoise.add(i, rebuild[i], false);
	    }
	    DefaultTableXYDataset xyNoisyDataset = new DefaultTableXYDataset();
	    xyNoisyDataset.addSeries(xynoisy);
	    DefaultTableXYDataset xyDenoiseDataset = new DefaultTableXYDataset();
	    xyDenoiseDataset.addSeries(xydenoise);        

	    
	    // JFreeChart chart = createCandlestickChart(dataitem);
	    JFreeChart chart = createXYChart(xyNoisyDataset, xyDenoiseDataset);	    
		try {
			ChartFrame frame = new ChartFrame("New Chart", chart);

			frame.pack();
			frame.setLocation(200, 300);
			frame.setVisible(true);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
		} catch (Exception e) {
			System.out.println("Exception in createData(): " + e);
		}
	    
	}
	
	public static JFreeChart createCandlestickChart(OHLCDataItem[] dataitem) {
	    OHLCDataset xyDataset = new DefaultOHLCDataset("name", dataitem);
		DateAxis   xAxis = new DateAxis("day");
		NumberAxis yAxis = new NumberAxis("price");
		//yAxis.setRange(13,15);
		yAxis.setAutoRangeIncludesZero(false);
		TickUnits tick_units = new TickUnits();
		tick_units.add(new DateTickUnit(DateTickUnit.DAY,1));
		// xAxis.setStandardTickUnits(tick_units);
		
        //XYToolTipGenerator labelGenerator = StandardXYToolTipGenerator.getTimeSeriesInstance();
        HighLowItemLabelGenerator labelGenerator = new HighLowItemLabelGenerator();
		XYPlot plot = new XYPlot(xyDataset, xAxis, yAxis,
				new CandlestickRenderer(-1.0, true, labelGenerator));
	    JFreeChart chart = new JFreeChart("Title", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	    return chart;
	}
	
	public static JFreeChart createXYChart(XYDataset dataset1, XYDataset dataset2) {
        NumberAxis xAxis = new NumberAxis("xlabel");
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis("ylabel");
        yAxis.setAutoRangeIncludesZero(false);
        XYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES);
        XYPlot plot = new XYPlot(dataset1, xAxis, yAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setDataset(1, dataset2);
        XYItemRenderer renderer2 = new StandardXYItemRenderer();
        plot.setRenderer(1, renderer2);
        
        // overlay a moving average dataset
        final XYDataset maData = MovingAverage.createMovingAverage(
                dataset1,
            " (Moving Average)",
            5L,
            5L
        );
        plot.setDataset(2, maData);
        final XYItemRenderer renderer3 = new StandardXYItemRenderer();
        renderer3.setToolTipGenerator(
            new StandardXYToolTipGenerator(
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0,000.0")
            )
        );
        plot.setRenderer(2, renderer3);
        
        
        JFreeChart chart = new JFreeChart("title", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	    
	    return chart;
	}
}
