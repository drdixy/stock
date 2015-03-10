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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.DataSourceType;
import net.sf.ojts.jdo.ObserverDataSourceConfiguration;
import net.sf.ojts.jdo.Alias;
import net.sf.ojts.jdo.AliasType;
import net.sf.ojts.jdo.Property;
import net.sf.ojts.jdo.PropertyType;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.jdo.subject.Subject;
import net.sf.ojts.util.ConfigurationHandler;

import org.exolab.castor.jdo.DatabaseNotFoundException;
import org.exolab.castor.jdo.JDO;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.TransactionAbortedException;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

/*
 * Created on 07.08.2004 by CS
 *
 */

/**
 * @author CS
 *
 */
public class CastorTest {

	public static void main(String[] args) {
		ConfigurationHandler.initLog4j("conf/log4j.properties", true);
		ConfigurationHandler.setRb("conf/jts.properties");

		String dbname = "jts";
		JDO    jdo    = new JDO();
		jdo.setDatabaseName( dbname );
		jdo.setConfiguration( "castor/database.xml" );
		jdo.setClassLoader( CastorTest.class.getClassLoader() );

		Subject subject = null;
		try {
			ConfigurationHandler.setJDO(jdo, dbname);

			//		 Obtain a new database
			ConfigurationHandler.openDatabase();
			Database db = ConfigurationHandler.getDatabase();
			//		 Begin a transaction
			
			SecurityPaper thyssen_krupp = new SecurityPaper();
			thyssen_krupp.setName("DE0007500001");//ISIN
			thyssen_krupp.setDescription("ThyssenKrupp AG Inhaber-Aktien O.N.");
			thyssen_krupp.setUrlSources("http://www.thyssenkrupp.de/");
			thyssen_krupp.setId(1);
			db.create(thyssen_krupp);
			subject = thyssen_krupp;
			
			MarketPlace xetra = new MarketPlace();
			xetra.setName("XETRA");
			xetra.setDescription("Fully automated trading system of the german stock exchange in Frankfurt.");
			xetra.setUrlSources("http://www.deutsche-boerse.com/");
			xetra.setId(2);
			db.create(xetra);
			
			Observer yahoo_finance = new Observer();
			yahoo_finance.setName("yahoo");
			yahoo_finance.setDescription("Finance information portal.");
			yahoo_finance.setUrlSources("http://finance.yahoo.com/ http://de.finance.yahoo.com/ http://fr.finance.yahoo.com/");
			yahoo_finance.setId(3);
			db.create(yahoo_finance);
			
			AliasType yahoo_alias = new AliasType();
			yahoo_alias.setName("yahoo aliases");
			yahoo_alias.setObserverLink(yahoo_finance);
			yahoo_alias.setDescription("Yahoo Symbols");
			yahoo_alias.setUrlSource("http://finance.yahoo.com/l");
			yahoo_alias.setId(1);
			db.create(yahoo_alias);
			
			Alias thyssen_krupp_yahoo_alias = new Alias();
			thyssen_krupp_yahoo_alias.setSubject(thyssen_krupp);
			thyssen_krupp_yahoo_alias.setType(yahoo_alias);
			thyssen_krupp_yahoo_alias.setMarket(xetra);
			thyssen_krupp_yahoo_alias.setAlias("tkag.de");
			thyssen_krupp_yahoo_alias.setId(1);
			db.create(thyssen_krupp_yahoo_alias);

			PropertyType datetime = new PropertyType();
			datetime.setName("DATETIME");
			datetime.setDescription("The datetime at which an observation was made.");
			datetime.setId(1);
			db.create(datetime);

			PropertyType price = new PropertyType();
			price.setName("PRICE");
			price.setDescription("The price of a given equity in a given currency");
			price.setId(2);
			db.create(price);

			PropertyType volume = new PropertyType();
			volume.setName("VOLUME");
			volume.setDescription("Integer number of any kind of transactions.");
			volume.setId(3);
			db.create(volume);
			
			Unit eur = new Unit();
			eur.setName("EUR");
			eur.setPropertyTypeLink(price);
			eur.setId(1);		
			db.create(eur);

			Unit usd = new Unit();
			usd.setName("USD");
			usd.setPropertyTypeLink(price);
			usd.setId(2);
			db.create(usd);

			Unit yahoo_date_time = new Unit();
			yahoo_date_time.setName("D-MMM-YY");
			yahoo_date_time.setPropertyTypeLink(datetime);
			yahoo_date_time.setId(3);
			db.create(yahoo_date_time);
			
			Property min_day_price = new Property();
			min_day_price.setName("MIN-DAY-PRICE");
			min_day_price.setType(price);
			min_day_price.setDescription("The minimum price of an equity for a given day in a given currency.");
			min_day_price.setId(1);
			db.create(min_day_price);

			Property max_day_price = new Property();
			max_day_price.setName("MAX-DAY-PRICE");
			max_day_price.setType(price);
			max_day_price.setDescription("The maximum price of an equity for a given day in a given currency.");
			max_day_price.setId(2);
			db.create(max_day_price);
			
			Property opening_day_price = new Property();
			opening_day_price.setName("OPENING-DAY-PRICE");
			opening_day_price.setType(price);
			opening_day_price.setDescription("The opening price of an equity for a given day in a given currency.");
			opening_day_price.setId(3);
			db.create(opening_day_price);

			Property closing_day_price = new Property();
			closing_day_price.setName("CLOSING-DAY-PRICE");
			closing_day_price.setType(price);
			closing_day_price.setDescription("The closing price of an equity for a given day in a given currency.");
			closing_day_price.setId(4);
			db.create(closing_day_price);

			Property trading_day_volume = new Property();
			trading_day_volume.setName("TRADING-DAY-VOLUME");
			trading_day_volume.setType(volume);
			trading_day_volume.setDescription("The traded volume for a given day.");
			trading_day_volume.setId(5);
			db.create(trading_day_volume);
			
			DataSourceType csv = new DataSourceType();
			csv.setName("csv");
			csv.setDescription("Comma Separated Values File");
			csv.setId(1);
			db.create(csv);

			DataSource yahoo_price_volume = new DataSource();
			yahoo_price_volume.setObserverLink(yahoo_finance);
			yahoo_price_volume.setType(csv);
			yahoo_price_volume.setUrl("http://de.table.finance.yahoo.com/table.csv?a=|SMS|&b=|SDS|&c=|SYYYYS|&d=|EME|&e=|EDE|&f=|EYYYYE|&s=|YahooSYM|&y=0&g=d&ignore=.csv");
			yahoo_price_volume.setDescription("Yahoo Historical Prices in CSV Format (Date, Opening, Max, Min, Closeing, Volume, Adj. Close*)");
			yahoo_price_volume.setId(1);
			db.create(yahoo_price_volume);
			
			ObserverDataSourceConfiguration thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo = new ObserverDataSourceConfiguration();
			thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo.setObservedAt(xetra);
			thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo.setObserverDataSource(yahoo_price_volume);
			thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo.setProperty(min_day_price);
			thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo.setUnit(eur);
			thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo.setColu("3");
			thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo.setId(1);
			db.create(thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo);
			
			/*
			 * we want for a given time range the data (certain properties / all properties obtainable of a given observer) 
			 * for a given equity at a given marketplace observed by a given observer
			 */
			
			//		 Do something
			//subject = (Subject) db.load(Subject.class, new Integer(1), Database.ReadOnly);


			// test YahooCSVDataInputHandler
			/*
			YahooCSVDataInputHandler handler = new YahooCSVDataInputHandler(yahoo_price_volume);//
			Result result = null;
			try {
				result = handler.read(DateUtil.parseDate("2004-06-01"), DateUtil.getToday(), thyssen_krupp, xetra);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CannotHandleSubjectException e) {
				e.printStackTrace();
			} catch (DBException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if(null != result) {
				// create the returned results in the database
				ResultItem[] ritems = result.getResultItems();
				for(int i = 0; i < ritems.length; i++) {
					DataItem[] values = ritems[i].getValues();
					for(int j = 0; j < values.length; j++) {
						db.create(values[j]);
					}
				}
			}
			*/
			
			//		 Commit the transaction, close database
			//ConfigurationHandler.commitDatabase();

	        File file = new File("test.xml");
			Writer fwriter = new FileWriter(file);
			
			Mapping mapping = new Mapping();
			mapping.loadMapping("castor/mapping.xml");
			Marshaller marshaller = new Marshaller(fwriter);
			marshaller.setMapping(mapping);
			//marshaller.setMarshalAsDocument(true);
			
			marshaller.marshal(datetime);
			marshaller.marshal(price);
			marshaller.marshal(volume);
			marshaller.marshal(min_day_price);
			marshaller.marshal(eur);
			marshaller.marshal(yahoo_price_volume);
			marshaller.marshal(thyssen_krupp_min_day_price_at_xetra_observed_by_yahoo);
			
		} catch (DatabaseNotFoundException e) {
			e.printStackTrace();
		} catch (TransactionNotInProgressException e) {
			e.printStackTrace();
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (MarshalException e1) {
			e1.printStackTrace();
		} catch (ValidationException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (MappingException e1) {
			e1.printStackTrace();
		} finally {
			ConfigurationHandler.closeDatabase();			
		}
	}
}
