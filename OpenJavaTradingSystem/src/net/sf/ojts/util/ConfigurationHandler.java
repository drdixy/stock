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
 * Created on 31.07.2004 by cs
 *
 */
package net.sf.ojts.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.DatabaseNotFoundException;
import org.exolab.castor.jdo.JDO;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.TransactionAbortedException;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.exolab.castor.jdo.engine.DatabaseImpl;
import org.exolab.castor.jdo.engine.DatabaseRegistry;
import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.mapping.MappingResolver;
import org.exolab.castor.mapping.loader.ClassDescriptorImpl;

/**
 * @author cs
 *
 */
public class ConfigurationHandler {
	
	public static final String HTTP_PROXY_HOST = "http_proxy_host";
	public static final String HTTP_PROXY_PORT = "http_proxy_port";
	public static final String HTTP_PROXY_USER = "http_proxy_user";
	public static final String HTTP_PROXY_PASS = "http_proxy_pass";

	public static final String JDBC_DRIVER   = "jdbc_driver";
	public static final String JDBC_LOCATION = "jdbc_location";
	public static final String JDBC_USERNAME = "jdbc_username";
	public static final String JDBC_PASSWORD = "jdbc_password";
	
	public static String CASTOR_MAPPING       = "/castor/mapping.xml";
	public static String DATA_INPUT_CACHE_DIR = "/data/cache";
	
	public static String getCastorMapping() {
        String home = FileUtils.getExecutablePathLocation();
	    return home + CASTOR_MAPPING;
	}
	
	public static String getDataInputCacheDir() {
        String home = FileUtils.getExecutablePathLocation();
	    return home + DATA_INPUT_CACHE_DIR;
	}
	
	public static class ConfigurationException extends Exception {	
		private static final long serialVersionUID = 4050206319120038199L;
		public ConfigurationException() {super();}
		public ConfigurationException(String arg0) {super(arg0);}		
		public ConfigurationException(String arg0, Throwable arg1) {super(arg0, arg1);}
		public ConfigurationException(Throwable arg0) {super(arg0);}
	}
	
	//	public static void initLog4j(String configfilename, boolean watch, String logfile) throws Exception {
	public static void initLog4j(String configfilename, boolean watch) {
		System.out.println("initLog4j: configfilename: " + configfilename);
		//System.out.println("initLog4j: logfile       : " + logfile);
		if(configfilename == null || configfilename.length() == 0 || !(new File(configfilename)).isFile()){
				System.err.println("ERROR: Cannot read the configuration file. " + configfilename);
				System.exit(1);
		}

		if(watch)
			PropertyConfigurator.configureAndWatch(configfilename);
		else
			PropertyConfigurator.configure(configfilename);
		
		/*
		String pattern =  "Milliseconds since program start: %r %n";
        pattern += "Classname of caller: %C %n";
        pattern += "Date in ISO8601 format: %d{ISO8601} %n";
        pattern += "Location of log event: %l %n";
        pattern += "Message: %m %n %n";
		
		String pattern = "[%X{RemoteAddress}][%-5p][%d{ISO8601}][%l] : %m %n";
        PatternLayout layout = new PatternLayout(pattern);		
		
		//SimpleLayout             layout   = new SimpleLayout();
		DailyRollingFileAppender appender = new DailyRollingFileAppender(layout, logfile, "'.'yyyy-MM-dd"); 
		
		Logger log = Logger.getRootLogger();
		log.removeAllAppenders();
		log.addAppender(appender);
        */
		
		System.out.println("Log4j correctly set-up.");
	}

	protected static PropertyResourceBundle rb_ = null;


	/**
	 * @return Returns the rb_.
	 */
	public static synchronized PropertyResourceBundle getRb() {
		return rb_;
	}

	/**
	 * @param rb_ The rb_ to set.
	 */
	public static synchronized void setRb(String config_properties_filename) {
		File config_properties_file = new File(config_properties_filename);
		
		try {
			FileInputStream fis = new FileInputStream(config_properties_file);			
			rb_ = new PropertyResourceBundle(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("PropertyResourceBundle successfully reloaded.");
	}
	
	public static synchronized String getString(String key) {
		return rb_.getString(key);
	}

	public static synchronized String getStringNonNull(String key) throws ConfigurationException {
		Logger log = Logger.getLogger(ConfigurationHandler.class);

		String value = rb_.getString(key);
		if(null == value) {
			String message = "Expected to get a non null string!";
			log.error(message);
			throw new ConfigurationException(message);			
		}
		return value;
	}
	
	public static synchronized int getInt(String key) throws ConfigurationException {
		Logger log = Logger.getLogger(ConfigurationHandler.class);
		
		int    rvalue = -1;
		String value  = rb_.getString(key);
		try {
			rvalue = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			String message = "Could not parse supposed int value: " + value;
			log.info(message, e);
			throw new ConfigurationException(message, e);
		}
		return rvalue;
	}
	
	protected static JDO             jdo_      = null;
	protected static Database        db_       = null;
	protected static MappingResolver resolver_ = null;
	
	public static synchronized void setJDO(JDO jdo, String dbname) throws DatabaseNotFoundException, PersistenceException {
		jdo_      = jdo;
		db_       = jdo.getDatabase();
		resolver_ = DatabaseRegistry.getDatabaseRegistry(dbname).getMappingResolver();
	}
	
	public static synchronized void openDatabase() throws PersistenceException {
		//db_ =  jdo_.getDatabase();
	    if(null == db_)
	        db_       = jdo_.getDatabase();
		db_.begin();
		if(false) {
			try {
				// XXX here i am just trying around what has to be done to make the database respect transactions!!
				//DatabaseRegistry.getDatabaseRegistry(db_.getDatabaseName()).getDataSource().getConnection().setAutoCommit(false);
				DatabaseImpl dbimpl = (DatabaseImpl) db_;
				Connection conn = (Connection) dbimpl.getConnection();
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
	}

	public static synchronized void commitDatabase() {
		Logger log = Logger.getLogger(ConfigurationHandler.class.getClass());
		try {
			db_.commit();
		} catch (TransactionNotInProgressException e) {
			log.error("TransactionNotInProgressException exception caught while trying to commit!", e);
		} catch (TransactionAbortedException e) {
			log.error("TransactionAbortedException exception caught while trying to commit!", e);
		}
	}
	
	public static synchronized void rollbackDatabase() {
		Logger log = Logger.getLogger(ConfigurationHandler.class.getClass());
		try {
			db_.rollback();
		} catch (TransactionNotInProgressException e) {
			log.error("TransactionNotInProgressException exception caught while trying to rollback!", e);
		}
	}
	
	public static synchronized void closeDatabase() {
		Logger log = Logger.getLogger(ConfigurationHandler.class.getClass());
		try {
		    if(null != db_)
		        db_.close();
		} catch (TransactionNotInProgressException e) {
			log.error("TransactionNotInProgressException exception caught while trying to close the db!", e);
		} catch (TransactionAbortedException e) {
			log.error("TransactionAbortedException exception caught while trying to close the db!", e);
		} catch (PersistenceException e) {
			log.error("PersistenceException exception caught while trying to close the db!", e);
		} finally {
		    db_ = null;		    
		}
	}
	
	public static synchronized Database getDatabase() {
		return db_;
	}
	
	public static ClassDescriptor getJDOClassDescriptor(Class type) {
		return resolver_.getDescriptor(type);
	}

	public static FieldDescriptor[] getJDOIdentities(Class type) {
		ClassDescriptor classDescriptor = getJDOClassDescriptor(type);
		if (classDescriptor != null) {
			// cast necessary, cause getIdentities() doesn't exist in interface:
			return ((ClassDescriptorImpl) classDescriptor).getIdentities();
		}
		return null;
	}	
	
	public static Object getJDOIdentity(Object obj) {
		FieldDescriptor[] identities = getJDOIdentities(obj.getClass());
		if ((identities != null) && (identities.length > 0)) {
			Object[] values = new Object[identities.length];
			for (int i = 0; i < identities.length; i++) {
				values[i] = identities[i].getHandler().getValue(obj);
				if (values[i] instanceof BigDecimal) {
					if (identities[i].getFieldType() == Integer.class)
						values[i] = new Integer(((BigDecimal) values[i]).intValue());
					if (identities[i].getFieldType() == Long.class)
						values[i] = new Long(((BigDecimal) values[i]).longValue());
				}
			}
			if (identities.length == 1)
				return values[0];
			else
				return values;
		}
		return null;
	}
	
}
