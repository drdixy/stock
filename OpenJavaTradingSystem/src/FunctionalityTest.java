/*
 * Created on 19.11.2004 by cs
 *
 */

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;

import org.jfree.chart.JFreeChart;

import net.sf.ojts.datainput.Result;
import net.sf.ojts.datainput.impl.DataInputCacheUtils;
import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.functionality.DBConfiguration;
import net.sf.ojts.functionality.DataAnalysisAndVisualisation;
import net.sf.ojts.functionality.DataInput;
import net.sf.ojts.functionality.Functionality;
import net.sf.ojts.functionality.FNPortfolio;
import net.sf.ojts.functionality.SISCInterface;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.portfolio.Position;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Portfolio;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.math.signal.Denoise;
import net.sf.ojts.math.statistics.AggregateFunctions;
import net.sf.ojts.math.types.DoubleSequence;
import net.sf.ojts.math.types.DoubleSequenceResult;
import net.sf.ojts.math.types.sequencecombinations.Reorder;
import net.sf.ojts.math.types.sequencecombinations.ReorderResult;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer;
import net.sf.ojts.math.types.vpd.ValuesPerDateContainer.VPDDoubleSequence;
import net.sf.ojts.pjdo.ValueAndUnit;
import net.sf.ojts.util.DateUtil;
import net.sf.ojts.util.context.CurrencyConversionContext;


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
public class FunctionalityTest {

    public static void main(String[] args) throws Exception {
        Functionality.init();
        
        //test1();
        //test2();
        //test4();
        test10();
    }
    
    public static void test0() throws Exception {
        if(!DBConfiguration.readXMLConfiguration("testread.xml")) {
            System.exit(1);
        }
        
        for(int i = 0; i < 3; i++) {
            MarketPlace mt = DBAccessConvenience.getMarketPlace("XETRA");        
            //mt.setDescription("test"); // checked that this has no effect on the stored data as it should!
            System.out.println(mt.getName());            
        }        
        //Functionality.schemeEvaluate("(load \"./sisc/functionality.scm\")");

        //test1();
        //test10();        
    }
    
    public static void test1() throws Exception {        
        //Functionality.fetchData("yahoo-csv", "2000-01-01", null, "DE0007500001", "XETRA");
        SISCInterface.fetchData("yahoo-csv", "2004-06-01", "2004-08-23", "DE0007500001", "XETRA");
        //Functionality.fetchData("yahoo-csv", "2003-06-01", "2003-08-23", "DE0007500001", "XETRA");
        //Functionality.fetchData("yahoo-csv", "2003-01-01", null        , "DE0007500001", "XETRA");
        // Functionality.schemeEvaluate("(begin (require-library 'sisc/libs/srfi) (load \"./sisc/functionality.scm\"))");        

        SISCInterface.fetchData("onvista-htmlshare", "2004-06-01", "2004-08-23", "EE0000001063", "UNDEFINED");
        SISCInterface.fetchData("onvista-htmlfund" , "2004-06-01", "2004-08-23", "LU0130728842", "UNDEFINED");
    }

    public static void test2() throws Exception {
        //Result result = DataInputCacheUtils.readFromCache("onvista-htmlshare-UNDEFINED-EE0000001063-2004-03-31-to-2005-03-31-readdate-2005-04-01_163538.xml");
        Result result = DataInputCacheUtils.readFromCache("out.xml");
		DataInput.commitReadDataToDB(result, true, false);

		//Functionality.importCache();
    }

    public static void test3() throws Exception {
        
        //Functionality.displayChart(Functionality.createCandlestickChartForEquity("yahoo-csv", "2003-01-01", null        , "DE0007500001", "XETRA"));
        Position position = null;
        Date when = DateUtil.parseDate("2004-05-20");
        SecurityPaper sp = DBAccessConvenience.getSecurityPaper("DE0007500001");
        MarketPlace   mt = DBAccessConvenience.getMarketPlace("XETRA");
        DataSource    ds = DBAccessConvenience.getDataSource("yahoo-csv");
        position = FNPortfolio.createPosition(ds, when, sp, mt, 29, 13.39, 10.0, DBAccessConvenience.getUnit("EUR"));
        //Position[] positions = Portfolio.getPositionsForSecurityPaper(DBAccessConvenience.getSecurityPaper("DE0007500001"), true);
        position = FNPortfolio.fillMissingFieldsInPosition(ds, position, DBAccessConvenience.getMarketPlace("XETRA"));
        		
        Unit eur = DBAccessConvenience.getUnit("EUR");
        DataSource  cur_ds = DBAccessConvenience.getDataSource("onvista-htmlcurrency");
        MarketPlace cur_mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        CurrencyConversionContext context = new CurrencyConversionContext(eur, cur_ds, cur_mt);        
		
        NumberFormat cf = NumberFormat.getCurrencyInstance();
		NumberFormat pf = NumberFormat.getPercentInstance();
		pf.setMaximumFractionDigits(2);
        cf.setCurrency(Currency.getInstance(position.getClosePriceCurrency().getName()));
        System.out.println(DateUtil.formatDate(position.getClose()) + " isin: " + position.getSubject().getName() + " value: " + cf.format(position.getClosePrice()));
        net.sf.ojts.pjdo.DoubleDataItem  absolute = FNPortfolio.evaluatePositionAbsolutePerPiece(position, context);
        System.out.println(DateUtil.formatDate(absolute.getDate()) + " absolute: " + cf.format(absolute.getValue()));
        net.sf.ojts.pjdo.DoubleDataItem  relative = FNPortfolio.evaluatePositionRelative(position, context);
        System.out.println(DateUtil.formatDate(relative.getDate()) + " relative: " + pf.format(relative.getValue()));
        net.sf.ojts.pjdo.DoubleDataItem  relative_yearly = FNPortfolio.evaluatePositionRelativeYearly(position, context);
        System.out.println(DateUtil.formatDate(relative.getDate()) + " relative yearly: " + pf.format(relative_yearly.getValue()));
        net.sf.ojts.pjdo.DoubleDataItem  absolute_yearly = FNPortfolio.evaluatePositionAbsoluteYearly(position, context);
        System.out.println(DateUtil.formatDate(relative.getDate()) + " absolute yearly: " + cf.format(absolute_yearly.getValue()));
        
        ValueAndUnit ra = FNPortfolio.evaluatePerformanceRelativeYearly(ds, DBAccessConvenience.getSecurityPaper("DE0008001009"), DBAccessConvenience.getMarketPlace("XETRA"), DateUtil.parseDate("2004-06-23"), DateUtil.parseDate("2004-12-17"), context);
        System.out.println("relative yearly: " + pf.format(ra.getValue()));
        ValueAndUnit aa = FNPortfolio.evaluatePerformanceAbsoluteYearly(ds, DBAccessConvenience.getSecurityPaper("DE0008001009"), DBAccessConvenience.getMarketPlace("XETRA"), DateUtil.parseDate("2004-06-23"), DateUtil.parseDate("2004-12-17"), context);
        System.out.println("absolute yearly: " + cf.format(aa.getValue()));
    }

    public static void test4() throws Exception {
        SISCInterface.fetchData("onvista-htmlcurrency", "2004-06-01", "2004-08-23", "EUR/USD", "UNDEFINED");
        //SISCInterface.fetchCurrencyData("onvista-htmlshare", "2004-01-01", null, "EUR", "USD");
    }
    
    public static void test5() throws Exception {
        /*
        DataSource ds = DBAccessConvenience.getDataSource("onvista-htmlfund");
        Date start = DateUtil.parseDate("2004-01-01");
        Date end   = DateUtil.getToday();
        SecurityPaper sp = DBAccessConvenience.getSecurityPaper("LU0029865408");
        MarketPlace   mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        net.sf.ojts.pjdo.DoubleDataItem[] data = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);
        
	    XYSeries original = new XYSeries("original", true, false);
	    for(int i = 0; i < data.length; i++) {
	        original.add(i, data[i].getValue(), false);
	    }
        
        Unit eur = DBAccessConvenience.getUnit("EUR");
        ds = DBAccessConvenience.getDataSource("onvista-htmlcurrency");
        mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        CurrencyConversionContext context = new CurrencyConversionContext(eur, ds, mt);        
        data = context.convert(data, true);
        
	    XYSeries converted = new XYSeries("converted", true, false);
	    for(int i = 0; i < data.length; i++) {
	        converted.add(i, data[i].getValue(), false);
	    }
	    
	    DefaultTableXYDataset xyNoisyDataset = new DefaultTableXYDataset();
	    xyNoisyDataset.addSeries(original);
	    DefaultTableXYDataset xyDenoiseDataset = new DefaultTableXYDataset();
	    xyDenoiseDataset.addSeries(converted);        
	    
	    // JFreeChart chart = createCandlestickChart(dataitem);
	    JFreeChart chart = HaarWaveletTest.createXYChart(xyNoisyDataset, xyDenoiseDataset);	    
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
        */
    }

    public static void test7() throws Exception {
        DataSource ds = DBAccessConvenience.getDataSource("yahoo-csv");
        Date start = DateUtil.parseDate("2004-01-02");
        Date end   = DateUtil.parseDate("2004-01-30");
        SecurityPaper sp = DBAccessConvenience.getSecurityPaper("DE0007500001");
        MarketPlace   mt = DBAccessConvenience.getMarketPlace("XETRA");
        ValuesPerDateContainer data_opening = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.OPENING_DAY_PRICE);
        ValuesPerDateContainer data_min     = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.MIN_DAY_PRICE);
        ValuesPerDateContainer data_max     = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.MAX_DAY_PRICE);
        ValuesPerDateContainer data_closing = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);
        ValuesPerDateContainer data_volume  = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.TRADING_DAY_VOLUME);

        ds = DBAccessConvenience.getDataSource("onvista-htmlshare");
        start = DateUtil.parseDate("2004-01-15");
        end   = DateUtil.parseDate("2004-02-15");
        sp = DBAccessConvenience.getSecurityPaper("EE0000001063");
        mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        ValuesPerDateContainer hp_data_closing = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);        

        ds = DBAccessConvenience.getDataSource("onvista-htmlfund");
        start = DateUtil.parseDate("2004-01-15");
        end   = DateUtil.parseDate("2004-02-15");
        sp = DBAccessConvenience.getSecurityPaper("LU0029865408");
        mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        ValuesPerDateContainer fu_data_closing = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE);        
        
        ValuesPerDateContainer[] containers = {data_opening, data_min, data_max, data_closing, data_volume, hp_data_closing, fu_data_closing};
        ValuesPerDateContainer data = ValuesPerDateContainer.merge(containers, 1); 
        System.out.println(data);

        Unit eur = DBAccessConvenience.getUnit("EUR");
        ds = DBAccessConvenience.getDataSource("onvista-htmlcurrency");
        mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        CurrencyConversionContext context = new CurrencyConversionContext(eur, ds, mt);        
        int[] usd_currency_column = {6};
        // modify in place:
        VPDDoubleSequence currency_conversion_sequence = data.new VPDDoubleSequence(usd_currency_column, usd_currency_column);
        context.convert(currency_conversion_sequence, currency_conversion_sequence);
        data.setUnitForColumn(usd_currency_column[0], context.getTargetCurrency());
        System.out.println(data);        
        
        int[] indices_input  = {3, 5, 6};
        int[] indices_output = {7};
        VPDDoubleSequence sum_sequence = data.new VPDDoubleSequence(indices_input, indices_output);
        DoubleSequence       sum_input  = sum_sequence;
        DoubleSequenceResult sum_output = sum_sequence;
        AggregateFunctions.sum(sum_input, sum_output, 0);
        data.setUnitForColumn(indices_output[0], context.getTargetCurrency());
        System.out.println(data);
        
    }

    public static void test8() throws Exception {        
        DataSource ds = DBAccessConvenience.getDataSource("yahoo-csv");
        Date start = DateUtil.parseDate("2004-01-02");
        Date end   = DateUtil.parseDate("2004-01-30");
        SecurityPaper sp = DBAccessConvenience.getSecurityPaper("DE0007500001");
        MarketPlace   mt = DBAccessConvenience.getMarketPlace("XETRA");
        
        DataAnalysisAndVisualisation.displayChart(DataAnalysisAndVisualisation.createCandlestickChartForSecurityPaper(ds, start, end, sp, mt));
    }

    public static void test9() throws Exception {        
        DataSource ds = DBAccessConvenience.getDataSource("yahoo-csv");
        Date start = DateUtil.parseDate("2004-01-02");
        Date end   = DateUtil.parseDate("2004-10-27");
        //Date end   = DateUtil.parseDate("2004-01-30");
        SecurityPaper sp = DBAccessConvenience.getSecurityPaper("DE0007500001");
        MarketPlace   mt = DBAccessConvenience.getMarketPlace("XETRA");
        ValuesPerDateContainer data_closing = DataAnalysisAndVisualisation.getDataForSecurityPaper(ds, start, end, sp, mt, DataAnalysisAndVisualisation.CLOSING_DAY_PRICE, 6);
        
        VPDDoubleSequence sequence = data_closing.new VPDDoubleSequence(6,0); 
        DoubleSequence       input       = sequence;
        DoubleSequenceResult result      = sequence;
        
        AggregateFunctions.relative(input, 0, result, 1);
        AggregateFunctions.logrelative(1.05, input, 0, result, 2);
        AggregateFunctions.continoussum(input, 2, result, 3);
        Denoise.wavelet(input, 0, result, 4, 0.5);
		AggregateFunctions.movingAverage(new Reorder(input, new int[]{0}), new ReorderResult(result, new int[]{5}), 20);
        
        System.out.println(input);

        {
            String[] sequence_names = {"DE0007500001"};            
            JFreeChart chart = DataAnalysisAndVisualisation.createXYChart("ChartName", input, sequence_names, false, true); 
            DataAnalysisAndVisualisation.displayChart(chart);            
        }
        
        Reorder chartsequence = new Reorder(input, new int[]{2,3});

        System.out.println(chartsequence);
        
        {
            String[] sequence_names = {"log", "sumlog"};            
            JFreeChart chart = DataAnalysisAndVisualisation.createXYChart("ChartName", chartsequence, sequence_names, false, false);             
            DataAnalysisAndVisualisation.displayChart(chart);
        }
        
        chartsequence = new Reorder(input, new int[]{0,4});

        System.out.println(chartsequence);
        
        {
            String[] sequence_names = {"noisy", "denoised"};            
            JFreeChart chart = DataAnalysisAndVisualisation.createXYChart("ChartName", chartsequence, sequence_names, false, false);             
            DataAnalysisAndVisualisation.displayChart(chart);
        }
        
        chartsequence = new Reorder(input, new int[]{0,5});

        System.out.println(chartsequence);
        
        {
            String[] sequence_names = {"original", "moving average"};            
            JFreeChart chart = DataAnalysisAndVisualisation.createXYChart("ChartName", chartsequence, sequence_names, false, false);             
            DataAnalysisAndVisualisation.displayChart(chart);
        }
        
    }

    public static void test10() throws Exception {
		Portfolio portfolio = new Portfolio();
		portfolio.setName("TestPortfolio");
		Unit eur = DBAccessConvenience.getUnit("EUR");
		Unit usd = DBAccessConvenience.getUnit("USD");
		portfolio.setDefaultCurrency(eur);

		Position position = null;
        DataSource ds = DBAccessConvenience.getDataSource("yahoo-csv");
        Date when = DateUtil.parseDate("2004-05-20");
        SecurityPaper sp = DBAccessConvenience.getSecurityPaper("DE0007500001");
        MarketPlace   mt = DBAccessConvenience.getMarketPlace("XETRA");
        position = FNPortfolio.createPosition(ds, when, sp, mt, 29, 13.39, 10.0, eur);
        Date sell = DateUtil.parseDate("2004-12-20");
		FNPortfolio.sellPosition(position, sell, mt, 16.05, 10.0, eur);
		
		portfolio.addPosition(position);
		
        ds = DBAccessConvenience.getDataSource("onvista-htmlshare");
        when = DateUtil.parseDate("2004-10-01");
        sp = DBAccessConvenience.getSecurityPaper("EE0000001063");
        mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        position = FNPortfolio.createPosition(ds, when, sp, mt, 50, 7.30, 10.5, eur);
		
		portfolio.addPosition(position);

        ds = DBAccessConvenience.getDataSource("onvista-htmlfund");
        when = DateUtil.parseDate("2004-09-01");
        sp = DBAccessConvenience.getSecurityPaper("LU0029865408");
        mt = DBAccessConvenience.getMarketPlace("UNDEFINED");
        position = FNPortfolio.createPosition(ds, when, sp, mt, 40, 21.71, 10.5, usd);
		
		portfolio.addPosition(position);
		
		//DBAccess.createObjectInDatabase(portfolio);
		
		ValuesPerDateContainer vpd = FNPortfolio.getClosingPricesForPortfolio(portfolio, 2);
		System.out.println(vpd);
		
        VPDDoubleSequence input  = vpd.new VPDDoubleSequence(5,portfolio.getPositions().size() + 1);
        AggregateFunctions.continoussum(input, 2, input, 3);
		AggregateFunctions.movingAverage(new Reorder(input, new int[]{2}), new ReorderResult(input, new int[]{4}), 20);
        {
            String[] sequence_names = {"Portfolio Absolute"};            
            JFreeChart chart = DataAnalysisAndVisualisation.createXYChart("ChartName", input, sequence_names, true, true); 
            DataAnalysisAndVisualisation.displayChart(chart);            
        }
        {
            String[] sequence_names = {"log", "sumlog"};            
            JFreeChart chart = DataAnalysisAndVisualisation.createXYChart("ChartName", new Reorder(input, new int[]{2,3}), sequence_names, false, false);             
            DataAnalysisAndVisualisation.displayChart(chart);
        }
        {
            String[] sequence_names = {"log", "moving average"};            
            JFreeChart chart = DataAnalysisAndVisualisation.createXYChart("ChartName", new Reorder(input, new int[]{2,4}), sequence_names, false, false);             
            DataAnalysisAndVisualisation.displayChart(chart);
        }
	}
}
