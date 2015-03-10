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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;

import net.sf.ojts.datainput.DataInputHandler;
import net.sf.ojts.datainput.Result;
import net.sf.ojts.datainput.ResultItem;
import net.sf.ojts.datainput.exceptions.CannotHandleSubjectException;
import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.datainput.impl.DataInputCacheUtils;
import net.sf.ojts.datainput.impl.DateRangeUtils;
import net.sf.ojts.functionality.exceptions.OJTSFunctionalityException;
import net.sf.ojts.jdo.DataInputRegistry;
import net.sf.ojts.jdo.DataItem;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.DataSourceType;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.Subject;
import net.sf.ojts.util.ConfigurationHandler;
import net.sf.ojts.util.DateUtil;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;

/**
 * @author cs
 *
 */
public class DataInput {

    public static DataInputHandler getDataInputHandlerForObserverString(String observer) throws OJTSFunctionalityException {
        DataSource  data_source         = DBAccessConvenience.getDataSource(observer);
        return getDataInputHandlerForObserverString(data_source);
    }
    
    public static DataInputHandler getDataInputHandlerForObserverString(DataSource data_source) throws OJTSFunctionalityException {
        // find the data source
        if(null == data_source)
            throw new OJTSFunctionalityException("The given DataSource is null!");
        String handler_class_name = data_source.getHandlerClassName();
        if(null == handler_class_name)
            throw new OJTSFunctionalityException("The given DataSource has a null handler class name configured!");
        Class cls = null;
        try {
            cls = Class.forName(handler_class_name);
        } catch (ClassNotFoundException e) {
            throw new OJTSFunctionalityException("The given DataSource has a non existing handler class name configured: " + handler_class_name);
        }
        Class[] constructor_arg_types = {DataSource.class};
        Constructor constructor = null;
        try {
            constructor = cls.getConstructor(constructor_arg_types);
        } catch (Exception e) {
            throw new OJTSFunctionalityException("The handler class : " + handler_class_name + " has no constructor for (DataSource ds)?");
        }
        
        Object[] args = {data_source};
        DataInputHandler handler = null;
        try {
            handler = (DataInputHandler) constructor.newInstance(args);
        } catch (Exception e) {
            throw new OJTSFunctionalityException("Exception was thrown while tryping to construct a handler of type : " + handler_class_name, e);
        }
        return handler;
    }

    
    public static Result fetchData(DataSource observer_and_type, Date sdate, Date edate, Subject subject, MarketPlace marketplace) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
        if(null == edate)
            edate = DateUtil.truncate(DateUtil.getToday());

        // get the data from the handler
    	DataInputHandler handler = getDataInputHandlerForObserverString(observer_and_type);
    	Result           result  = handler.read(sdate, edate, subject, marketplace);
    	
    	// this is to solve some problems with the data input registry
    	// even if the result does not contain any items i want that this read is registered in the registry
    	result.setStart(sdate);
    	result.setEnd(edate);
    	result.setSubject(subject);
    	
    	// commit the read data to the database
    	DataInput.commitReadDataToDB(result, true, true);
    	return result;
    }

    /*
    public static CurrencyConversionDataInputHandler getCurrencyConversionDataInputHandlerForObserverString(String observer) throws PersistenceException, OJTSFunctionalityException{
        // find the data source
        if(null != observer && observer.startsWith("onvista")) {
            DataSource                         data_source = DBAccessConvenience.getDataSource(observer);
            CurrencyConversionDataInputHandler handler     = new OnVistaHTMLDataInputHandler(data_source);//
            return handler;
        } else {
            throw new OJTSFunctionalityException("The observer String is not known: " + observer);            
        }
    }

    public static CurrencyConversion[] fetchCurrencyData(String observer, Date sdate, Date edate, Unit from, Unit to) throws PersistenceException, OJTSFunctionalityException, DBException, IOException, CannotHandleSubjectException {
        if(null == edate)
            edate = DateUtil.truncate(DateUtil.getToday());

        // get the data from the handler
    	CurrencyConversionDataInputHandler handler = getCurrencyConversionDataInputHandlerForObserverString(observer);    	
    	CurrencyConversion[]               result  = handler.read(sdate, edate, from, to);
    	
    	// commit the read data to the database
    	//DataInput.commitReadCurrencyDataToDB(result, true);
    	return result;
    }

    public static void commitReadCurrencyDataToDB(CurrencyConversion[] result, boolean autocommit) throws PersistenceException {
    	Logger log = Logger.getLogger(DataInput.class);
    	if(null == result || result.length == 0) {
    	    log.info("commitReadCurrencyDataToDB: result was null therefore no commits are necessary.");	    		
    	    return;
    	}
    	Date real_begin   = DateUtil.getToday();
    	Date real_end     = null;
        try {
            real_end = DateUtil.parseDate("1900-01-01");
        } catch (ParseException e) {}
        Observer observer = null;
    	Unit     from     = null;
    	Unit     to       = null;
    	for(int i = 0; i < result.length; i++) {
    	    if(i == 0) {
    	        observer = result[i].getObserver();
    	        from     = result[i].getFrom();
    	        to       = result[i].getTo();
    	    } else {
    	        if(!result[i].getObserver().getName().equals(observer.getName()))
    	            throw new PersistenceException("commitReadCurrencyDataToDB(): CurrencyConversion[] data-set not consistent: observers mismatch!");
    	        if(!result[i].getFrom().getName().equals(from.getName()))
    	            throw new PersistenceException("commitReadCurrencyDataToDB(): CurrencyConversion[] data-set not consistent: from units mismatch!");
    	        if(!result[i].getTo().getName().equals(to.getName()))
    	            throw new PersistenceException("commitReadCurrencyDataToDB(): CurrencyConversion[] data-set not consistent: to units mismatch!");
    	    }
    	    if(result[i].getValidFrom().before(real_begin))
    	        real_begin = result[i].getValidFrom();
    	    if(result[i].getValidFrom().after(real_end))
    	        real_end   = result[i].getValidFrom();
    	}
		Object[] oresult = null;
		try {
    		String query  = "SELECT ccv FROM " + CurrencyConversion.class.getName() + " ccv WHERE ccv.observer = $1 and ccv.from = $2 and ccv.to = $3 and ccv.validFrom >= $4 and ccv.validFrom <= $5";
			Object[] query_arguments = {observer, from, to, real_begin, real_end};
            oresult =  DBAccess.getObjectsFromDatabase(query, query_arguments, false);
        } catch (NoDBResultsException e) {
            // will not be thrown, just in case:
            log.error("NoDBResultsException", e);
        } catch (PersistenceException e) {
            // was already logged
            log.error("PersistenceException", e);
        }
    	HashMap already_seen_dates = new HashMap();
    	for(int i = 0; i < oresult.length; i++) {
    	    CurrencyConversion entry = (CurrencyConversion) oresult[i];
    	    already_seen_dates.put(entry.getValidFrom(), entry);
    	}

		Database db = null;
		int commited_ritem_count = 0; 
        try {
            ConfigurationHandler.openDatabase();
            db = ConfigurationHandler.getDatabase();
		    
		    if(autocommit) {
				for(int i = 0; i < result.length; i++) {
					if(null != already_seen_dates.get(result[i].getValidFrom()))

					    continue;
					try {
                        db.create(result[i]);
        				ConfigurationHandler.commitDatabase();
					    commited_ritem_count++;
                    } catch (Exception e) {
                        // ignore and try to create next data item
                        ConfigurationHandler.rollbackDatabase();
                    } finally {
                        // the problem is that after the first exception the db or castor seems to be in a bad state?
        	    		ConfigurationHandler.closeDatabase();
        	            ConfigurationHandler.openDatabase();
        	            db = ConfigurationHandler.getDatabase();
                    }					        
				}			        
		    } else {
				try {
					for(int i = 0; i < result.length; i++) {
						if(null != already_seen_dates.get(result[i].getValidFrom()))
						    continue;
                        db.create(result[i]);
        				commited_ritem_count++;
					}			        			        
                    ConfigurationHandler.commitDatabase();
                } catch (Exception e) {
                    // ignore and try to create next data item
                    commited_ritem_count = 0; // error occured -> rollback -> no items committed
                    ConfigurationHandler.rollbackDatabase();
                    log.error("There were errors while trying to commit data to the database.", e);
                }					        

		    }
		} finally {
            ConfigurationHandler.rollbackDatabase();
    		ConfigurationHandler.closeDatabase();
		    log.info("While fetching data from observer: " + observer.getName() + " for currency conversion from : " + from.getName() + "  to: " + to.getName() + " between start: " + DateUtil.formatDate(real_begin) + " and end: " + DateUtil.formatDate(real_end) + " from a total of: " + result.length + " days there was data entered for: " + commited_ritem_count + " days.");    		
		}    	
    }
    */
    
    public static void commitReadDataToDB(Subject defaultsubject, Result[] result, boolean autocommit, boolean write_to_cache) throws PersistenceException {
    	Logger log = Logger.getLogger(DataInput.class);
    	if(null == result) {
    	    log.info("commitReadDataToDB: result was null therefore no commits are necessary.");	    		
    	    return;        
    	}
    	boolean errors = false;
    	PersistenceException last = null;
        for(int i = 0; i < result.length; i++) {
            try {
                DataInput.commitReadDataToDB(result[i], autocommit, write_to_cache);
            } catch (PersistenceException e) {
                log.error("A Persistence exception occured for index: " + i, e);
                errors = true;
                last = e;
            }
        }
        if(errors)
            throw new PersistenceException("There were one or several PersistenceException", last);
    }

    public static void commitReadDataToDB(Result result, boolean autocommit, boolean write_to_cache) throws PersistenceException {
    	Logger log = Logger.getLogger(DataInput.class);
    
    	if(null == result) {
    	    log.info("commitReadDataToDB: result was null therefore no commits are necessary.");	    		
    	    return;
    	}
    
    	HashMap already_seen_dates = new HashMap();
    	if(null != result) {
    		// create the returned results in the database
    		ResultItem[] ritems = result.getResultItems();
            if(0 == ritems.length) {
    		    log.info("Was fetching data for 0 days therefore no commits are necessary.");
    	        Date           sdate       = result.getStart();
    	        Date           edate       = result.getEnd();
    	        Subject        subject     = result.getSubject();
    		    if(null == subject || null == sdate || null == edate)
    		        return;
    	        Observer       observer    = result.getObserver();
    	        DataSourceType dstype      = result.getDataSourceType();
    	        MarketPlace    marketplace = result.getMarketPlace();

    			Date[][] intervals_still_to_read = new Date[0][];
    			try {
    			    //intervals_still_to_read = DateRangeUtils.findIntervalsStillToRead(observer, dstype, marketplace, subject, DateUtil.dateOneDayBefore(sdate), DateUtil.dateOneDayAfter(edate));
    			    Date start = edate;
    			    Date end   = DateUtil.getToday();
    			    
    		        DataInputRegistry[] dir = DateRangeUtils.getRegisteredDataInputRanges(observer, dstype, marketplace, subject, start, end);
    		        // if there are no registered ranges after the edate this is not a "hole" to be filled, but there is
    		        // simply no data available for that range.
    		        if(null == dir || dir.length == 0) return;
    		            
    			    intervals_still_to_read = DateRangeUtils.findIntervalsStillToRead(observer, dstype, marketplace, subject, sdate, edate);
    			    for(int i = 0; i  < intervals_still_to_read.length; i++) {
    			        result.setStart(intervals_still_to_read[i][0]);
    			        result.setEnd(intervals_still_to_read[i][1]);
    			        DataInputCacheUtils.registerDataInputInDB(result);
    			    }
    			} catch(Exception e) {   
    			    log.error("Exception occured while finding the intervals still to read!", e);
    			}
                return;
            }
    
    	    Date[][] intervals = DateRangeUtils.getRegisteredDateRanges(result.getObserver(), result.getDataSourceType(), result.getMarketPlace(), result.getSubject(), true);
    	    int total_ritem_count    = ritems.length;
    	    int commited_ritem_count = 0;
            
    		Database db = null;
            try {
                ConfigurationHandler.openDatabase();
                db = ConfigurationHandler.getDatabase();
    		    
    		    if(autocommit) {
    				for(int i = 0; i < ritems.length; i++) {
    				    boolean increase_committed = true;
    					if(DateRangeUtils.isDateInIntervals(intervals, ritems[i].getDate()))
    					    continue;
    					DataItem[] values = ritems[i].getValues();
    					for(int j = 0; j < values.length; j++) {
    						try {
                                db.create(values[j]);
                				ConfigurationHandler.commitDatabase();
                            } catch (Exception e) {
                                // ignore and try to create next data item
                                already_seen_dates.put(ritems[i].getDate(), "");
                                increase_committed = false;
                                
                                ConfigurationHandler.rollbackDatabase();
                            } finally {
                                // the problem is that after the first exception the db or castor seems to be in a bad state?
                	    		ConfigurationHandler.closeDatabase();
                	            ConfigurationHandler.openDatabase();
                	            db = ConfigurationHandler.getDatabase();
                            }					        
    					}
    					if(increase_committed)
    					    commited_ritem_count++;
    				}			        
    		    } else {
    				try {
    					for(int i = 0; i < ritems.length; i++) {
    						if(DateRangeUtils.isDateInIntervals(intervals, ritems[i].getDate()))
    						    continue;
    						DataItem[] values = ritems[i].getValues();
    						for(int j = 0; j < values.length; j++) {
                                db.create(values[j]);
    						}				        
            				commited_ritem_count++;
    					}			        			        
                        ConfigurationHandler.commitDatabase();
                    } catch (Exception e) {
                        // ignore and try to create next data item
                        already_seen_dates.put(ritems[0].getDate(), "");// just put one in here to signal the error
                        commited_ritem_count = 0; // error occured -> rollback -> no items committed
                        ConfigurationHandler.rollbackDatabase();
                        log.error("There were errors while trying to commit data to the database.", e);
                    }					        
    
    		    }
    		} finally {
                ConfigurationHandler.rollbackDatabase();
        		ConfigurationHandler.closeDatabase();
        			    		
        		String observer = result.getObserverName();
        		String market   = result.getMarketPlaceName();
        		String subject  = result.getSubjectName();
        		    		    
        		String start = DateUtil.formatDate(result.getStart());
        		String end   = DateUtil.formatDate(result.getEnd());
    
        		if(already_seen_dates.size() == 0 && commited_ritem_count > 0) {
        		    // only write to cache if there were no errors and there was data committed at all!
    				try {
    				    DataInputCacheUtils.registerDataInputInDB(result);
    				    if(write_to_cache)
    				        DataInputCacheUtils.writeReaderCacheToDisk(result);
                    } catch (Exception e) {
                        log.error("Error while trying to write to the cache for observer: " + observer + " market: " + market + "  subject: " + subject + " between start: " + start + " and end: " + end);
                    }	    		    
        		} else {
        		    if(already_seen_dates.size() != 0)
        		        log.info("you can ignore the fatal messages above.");	    		    
        		}
        		
    		    String not_committed = "0";
    		    String committed     = "" + ritems.length;
        		if(ritems.length != commited_ritem_count) {
        		    if(autocommit) {
        		        not_committed = "" + (ritems.length - commited_ritem_count);
        		        committed     = "" + commited_ritem_count;
    
        		    } else {
        		        not_committed = "ALL";
        		        committed     = "NONE";
        		    }
    
        		    String msg = "While fetching data for observer: " + observer + " market: " + market + "  subject: " + subject + " between start: " + start + " and end: " + end + " from a total of: " + ritems.length + " there were: " + not_committed + " ignored, because there was already data in the database or other errors occured";
        		    if(autocommit)
        		        log.info(msg);
        		    else
        		        log.error(msg);
        		} else {
        		    log.info("While fetching data for observer: " + observer + " market: " + market + "  subject: " + subject + " between start: " + start + " and end: " + end + " from a total of: " + ritems.length + " days there was data entered for: " + committed + " days.");
        		}
        		
    		}
    	}        
    }

    public static void importCache() {
    	Logger log = Logger.getLogger(DataInput.class);
    
    	/* XXX
        // iterate over all known DataInputHandlers and call their readCache methods
    	try {
            Result[] result = YahooCSVDataInputHandler.importCache();
            if(null == result || 0 == result.length) {
    		    log.info("importCache: data for the cached time periods is already in the database therefore no imports were done.");
    		    return;
            }
    		commitReadDataToDB(result, false, false);
        } catch (Exception e) {
            log.error("Error occured while calling the import cache method on the YahooCSVDataInputHandler.", e);
        }
        */
    }

}
