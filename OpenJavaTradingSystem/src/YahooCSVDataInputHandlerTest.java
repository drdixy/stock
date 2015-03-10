
import net.sf.ojts.datainput.Result;
import net.sf.ojts.datainput.ResultItem;
import net.sf.ojts.datainput.impl.YahooCSVDataInputHandler;
import net.sf.ojts.jdo.DataItem;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.util.ConfigurationHandler;
import net.sf.ojts.util.DateUtil;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.JDO;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;

/*
 * Created on 25.09.2004 by CS
 *
 */

/**
 * @author CS
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
public class YahooCSVDataInputHandlerTest {

	public static void main(String[] args) {
		ConfigurationHandler.initLog4j("conf/log4j.properties", true);
		ConfigurationHandler.setRb("conf/jts.properties");

		String dbname = "jts";
		JDO    jdo    = new JDO();
		jdo.setDatabaseName( dbname );
		jdo.setConfiguration( "castor/database.xml" );
		jdo.setClassLoader( CastorTest.class.getClassLoader() );

		try {
			ConfigurationHandler.setJDO(jdo, dbname);
			ConfigurationHandler.openDatabase();
			Database db = ConfigurationHandler.getDatabase();
			
			Observer    yahoo              = null;
			DataSource  yahoo_price_volume = null;
			SecurityPaper      thyssen_krupp      = null;
			MarketPlace xetra              = null;
			
			OQLQuery     oql       = null;
			QueryResults results   = null;
			Result       container = null;
			String       query     = null; 
			try {
				/*
				yahoo = (Observer) db.load(Observer.class, new Integer(5));
				query = "SELECT ob FROM " + Observer.class.getName() + " ob WHERE ob.id = $1";
				System.out.println("query: " + query);
				oql = db.getOQLQuery(query);
				oql.bind(5);
				results = oql.execute();
				if(results.hasMore()) {
					yahoo = (Observer) results.next();
				}

				query = "SELECT ds FROM " + DataSource.class.getName() + " ds WHERE ds.observerLink = $1";
				System.out.println("query: " + query);
				oql = db.getOQLQuery(query);
				oql.bind(yahoo);
				results = oql.execute();
				
				*/

				query = "SELECT ds FROM " + DataSource.class.getName() + " ds WHERE ds.observerLink.name = $1 and ds.type.name = $2";
				System.out.println("query: " + query);
				oql = db.getOQLQuery(query);
				oql.bind("yahoo");
				oql.bind("csv");
				results = oql.execute();

				
				if(results.hasMore()) {
					yahoo_price_volume = (DataSource) results.next();
				}
				if(results.hasMore()) {
					System.err.println("more than one result");
					System.exit(1);
				}

				query = "SELECT eq FROM " + SecurityPaper.class.getName() + " eq WHERE eq.name = $1";
				System.out.println("query: " + query);
				oql = db.getOQLQuery(query);
				oql.bind("DE0007500001");
				results = oql.execute();
				if(results.hasMore()) {
					thyssen_krupp = (SecurityPaper) results.next();
				}

				query = "SELECT mt FROM " + MarketPlace.class.getName() + " mt WHERE mt.name = $1";
				System.out.println("query: " + query);
				oql = db.getOQLQuery(query);
				oql.bind("XETRA");
				results = oql.execute();
				if(results.hasMore()) {
					xetra = (MarketPlace) results.next();
				}
				
			} finally {
				if(null != oql)
					oql.close();
			}
			if(null == yahoo_price_volume) {
				System.err.println("no DataSource found");
				System.exit(1);
			}
			
			
			YahooCSVDataInputHandler handler = new YahooCSVDataInputHandler(yahoo_price_volume);//
			Result result = null;
			try {
				result = handler.read(DateUtil.parseDate("2004-06-01"), DateUtil.getToday(), thyssen_krupp, xetra);
			} catch (Exception e) {
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

			ConfigurationHandler.commitDatabase();			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConfigurationHandler.closeDatabase();			
		}

	
	}
}
