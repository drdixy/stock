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

import java.lang.reflect.Method;

import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.exceptions.NoDBResultsException;
import net.sf.ojts.functionality.exceptions.TooManyDBResultsException;
import net.sf.ojts.util.ConfigurationHandler;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;

/**
 * @author cs
 *
 */
public class DBAccess {

    public static Object getObjectFromDatabase(String query, Object[] query_arguments) throws TooManyDBResultsException, PersistenceException {
    	Logger log = Logger.getLogger(Functionality.class);
        Object result = null;
        try {
            result = DBAccess.getObjectFromDatabase(query, query_arguments, false);
        } catch (NoDBResultsException e) {
            log.error("NoDBResultsException exception was thrown, which should not be possible at all!!", e);
        }
        return result;
    }

    public static Object getObjectFromDatabase(String query, Object[] query_arguments, boolean no_results_is_exception) throws TooManyDBResultsException, PersistenceException, NoDBResultsException {
    	Logger log = Logger.getLogger(Functionality.class);
    
    	Object       result      = null;
    	Database     db          = null;
    	OQLQuery     oql         = null;
    	QueryResults results     = null;
    	int          size        = -1;
    	try {
            ConfigurationHandler.openDatabase();
            db = ConfigurationHandler.getDatabase();
            
            oql = db.getOQLQuery(query);
            for(int i = 0; i < query_arguments.length; i++) {
                oql.bind(query_arguments[i]);                
            }
    		results = oql.execute(true);
    		size = results.size();
    		if(0 == size) {
    		    if(no_results_is_exception)
    		        throw new NoDBResultsException("The query: " + query + " with arguments: " + query_arguments + " returned no results: " + size);
    		    else
    		        return null;
    		}
    		if(1 < size)
    		    throw new TooManyDBResultsException("The query: " + query + " with arguments: " + query_arguments + " returned too many results: " + size);
    		if (results.hasMore()) {
    		    result = results.next();
    		} else {
    		    throw new NoDBResultsException("The query: " + query + " with arguments: " + query_arguments + " returned no results, but size was > 0s: " + size);			    
    		}
        } catch (PersistenceException e) {
            log.error("A  PersistenceException occured while processing the query: " + query + " with arguments: " + query_arguments, e);
            throw e;
    	} finally {
    		// this is a get and should not change the database
    		ConfigurationHandler.rollbackDatabase();			
    		if(null != oql)
    			oql.close();
    		ConfigurationHandler.closeDatabase();			
    	}
        
        return result;
    }

    public static Object[] getObjectsFromDatabase(String query, Object[] query_arguments) throws PersistenceException {
    	Logger log = Logger.getLogger(Functionality.class);
        Object[] result = null;// null indicates error in contrast to an array with 0 length.
        try {
            result = DBAccess.getObjectsFromDatabase(query, query_arguments, false);
        } catch (NoDBResultsException e) {
            log.error("NoDBResultsException exception was thrown, which should not be possible at all!!");
        }
        return result;
    }

    public static Object[] getObjectsFromDatabase(String query, Object[] query_arguments, boolean no_results_is_exception) throws PersistenceException, NoDBResultsException {
    	Logger log = Logger.getLogger(Functionality.class);
    
    	Object[]     result      = new Object[0];
    	Database     db          = null;
    	OQLQuery     oql         = null;
    	QueryResults results     = null;
    	int          size        = -1;
    	try {
            ConfigurationHandler.openDatabase();
            db = ConfigurationHandler.getDatabase();
            
            oql = db.getOQLQuery(query);
            for(int i = 0; i < query_arguments.length; i++) {
                oql.bind(query_arguments[i]);                
            }
    		results = oql.execute(true);
    		size = results.size();
    		if(0 == size && no_results_is_exception)
    		    throw new NoDBResultsException("The query: " + query + " with arguments: " + query_arguments + " returned no results: " + size);
    		
    		result = new Object[size];
    		int i = 0;
    		while(results.hasMore()) {
    		    result[i] = results.next();
    		    i++;
    		}
    		// this is a get and should not change the database
    		ConfigurationHandler.rollbackDatabase();			
        } catch (PersistenceException e) {
            log.error("A  PersistenceException occured while processing the query: " + query + " with arguments: " + query_arguments, e);
            throw e;
    	} finally {
    		if(null != oql)
    			oql.close();
    		ConfigurationHandler.closeDatabase();			
    	}
        
        return result;
    }

    public static boolean createObjectInDatabase(Object obj) {
        boolean success = false;
        try {
            DBAccess.createObjectInDatabaseWithException(obj);
            success = true;
        } catch (DBException e) {
            // ignore
        }
        return success;
    }

    public static void createObjectInDatabaseWithException(Object obj) throws DBException {
    	Logger log = Logger.getLogger(Functionality.class);
    
    	try {
            ConfigurationHandler.openDatabase();
            Database db = ConfigurationHandler.getDatabase();
            
            db.create(obj);
            ConfigurationHandler.commitDatabase();
        } catch (Exception e) {
            log.error("Problem while trying to create the object: " + obj.toString() + " in the database.", e);
            throw new DBException(e);
    	} finally {
    		ConfigurationHandler.closeDatabase();
    	}
    }

    public static boolean deleteObjectFromDatabase(Object obj) throws DBException {
    	Logger log = Logger.getLogger(Functionality.class);
    	boolean success = false;
    
    	Object       result      = null;
    	Database     db          = null;
    	OQLQuery     oql         = null;
    	QueryResults results     = null;
    	int          size        = -1;
    	
        String   query           = "SELECT obj FROM " + obj.getClass().getName() + " obj WHERE obj.id = $1";
        Object[] query_arguments = new Object[1];
    	try {
            ConfigurationHandler.openDatabase();
            db = ConfigurationHandler.getDatabase();
    
            Method method = obj.getClass().getMethod("getId", new Class[0]);
            Object id     = method.invoke(obj, new Object[0]);
            query_arguments[0] = id;
                        
            oql = db.getOQLQuery(query);
            for(int i = 0; i < query_arguments.length; i++) {
                oql.bind(query_arguments[i]);                
            }
    		results = oql.execute(true);
    		size = results.size();
    		if(0 == size) {			    
    	        throw new NoDBResultsException("While trying to delete object: the query: " + query + " with arguments: " + query_arguments + " returned no results: " + size);
    		}
    		if(1 < size)
    		    throw new TooManyDBResultsException("While trying to delete object: the query: " + query + " with arguments: " + query_arguments + " returned too many results: " + size);
    		if (results.hasMore()) {
    		    result = results.next();
                db.remove(result);
    		} else {
    		    throw new NoDBResultsException("While trying to delete object: the query: " + query + " with arguments: " + query_arguments + " returned no results, but size was > 0: " + size);			    
    		}
    		ConfigurationHandler.commitDatabase();			
    		success = true;
        } catch (Exception e) {
            log.error("An exception was thrown while trying to delete the object: " + obj.toString(), e);
    		ConfigurationHandler.rollbackDatabase();			
    	} finally {
    		// this is a get and should not change the database
    		if(null != oql)
    			oql.close();
    		ConfigurationHandler.closeDatabase();			
    	}
    	return success;
    }

    public static Object getObjectById(Class cls, int id) {
        String query = "SELECT obj FROM " + cls.getName() + " obj WHERE obj.id = $1";
        Object[] args = {new Integer(id)};
        Object result = null;
        try {
            result = getObjectFromDatabase(query, args);
        } catch(Exception ex) {
            // ignore
        }
        return result;
    }

    public static Object getObjectByName(Class cls, String name) {
        return DBAccess.getObjectByName(cls, name, "name");
    }

    public static Object getObjectByName(Class cls, String name, String property_name) {
        String query = "SELECT obj FROM " + cls.getName() + " obj WHERE obj." + property_name + " = $1";
        Object[] args = {name};
        Object result = null;
        try {
            result = getObjectFromDatabase(query, args);
        } catch(Exception ex) {
            // ignore
        }
        return result;
    }

}
