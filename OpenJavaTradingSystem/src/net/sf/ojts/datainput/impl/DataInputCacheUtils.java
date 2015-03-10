/*
 * Created on 21.11.2004 by cs
 *
 */
package net.sf.ojts.datainput.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.MarshalListener;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import net.sf.ojts.datainput.Result;
import net.sf.ojts.datainput.ResultItem;
import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.DBAccess;
import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.functionality.Functionality;
import net.sf.ojts.functionality.exceptions.OJTSFunctionalityException;
import net.sf.ojts.jdo.DataInputRegistry;
import net.sf.ojts.jdo.DataItem;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.DataSourceType;
import net.sf.ojts.jdo.DoubleDataItem;
import net.sf.ojts.jdo.ObserverDataSourceConfiguration;
import net.sf.ojts.jdo.readercache.RCCacheFile;
import net.sf.ojts.jdo.readercache.RCDataItem;
import net.sf.ojts.jdo.readercache.RCDataItemContainer;
import net.sf.ojts.jdo.readercache.RCDoubleDataItem;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.Subject;
import net.sf.ojts.util.ConfigurationHandler;
import net.sf.ojts.util.DateUtil;

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
public class DataInputCacheUtils {
    
    public static class EnumerateObjectsMarshalListener implements MarshalListener {
        
        protected int id = 0;
        
        public boolean preMarshal(Object object) {
    		Logger log = Logger.getLogger(this.getClass());
            // use reflection to set the object id :(
            try {
                Class[] argtypes = new Class[1];
                argtypes[0] = Integer.TYPE;
                String classname = object.getClass().getName();
                if(classname.startsWith("net.sf.ojts.jdo")) {
                    Method method = object.getClass().getMethod("setId", argtypes);
                    Object[] args = new Object[1];
                    args[0] = new Integer(id); id++;
                    Object id     = method.invoke(object, args);                    
                }
            } catch (Exception e) {
                log.error("Could not set id on object via reflection on type" + object.getClass().getName(), e);                
            }
            return true;
        }

        public void postMarshal(Object object) {
        }
        
    }

    public static String buildObserverAndTypeString(Observer observer, DataSourceType dstype) {
	    String observer_and_type = observer.getName() + "-" + dstype.getName();
	    return observer_and_type;
    }
    
    public static void writeReaderCacheToDisk(Result result) throws OJTSFunctionalityException {
		Logger log = Logger.getLogger(Functionality.class);

		String isin                            = result.getSubjectName();
        String market                          = result.getMarketPlaceName();
        ObserverDataSourceConfiguration[] info = result.getInfo();
        RCDataItemContainer[] rccontainer      = new RCDataItemContainer[info.length];
        for(int i = 0; i < info.length; i++) {
            String property = info[i].getProperty().getName();
            String unit     = "";
            rccontainer[i]  = new RCDataItemContainer(isin, market, property, unit);
        }
        ResultItem[] result_item = result.getResultItems(); 
        for(int i = 0; i < result_item.length; i++) {            
            DataItem[] data_item = result_item[i].getValues();
            Date date = result_item[i].getDate();
            
            for(int j = 0; j < data_item.length; j++) {
                if(data_item[j] instanceof DoubleDataItem) {
                    DoubleDataItem ddi = (DoubleDataItem) data_item[j];
                    rccontainer[j].addDataItem(new RCDoubleDataItem(date,ddi.getValue()));
                } else {
                    String msg = "writeReaderCacheToDisk(): cannot handle data_item of unknow type?";
                    log.error(msg);
                    throw new OJTSFunctionalityException(msg);
                }
            }
            if(0 == i) {
                for(int j = 0; j < data_item.length; j++) {
                    rccontainer[j].setUnit(data_item[j].getUnit().getName());
                }
            }
        }
        Observer       observer    = result.getObserver();
        DataSourceType dstype      = result.getDataSourceType();
        MarketPlace    marketplace = result.getMarketPlace();
        Subject        subject     = result.getSubject();
        
        try {
            DataInputCacheUtils.writeContentToCache(observer, dstype, marketplace, subject, result.getStart(), result.getEnd(), rccontainer);
        } catch (Exception e) {
            String msg = "writeReaderCacheToDisk(): could not write data to disk because of exception: " + e.getMessage();
            log.error(msg);
            throw new OJTSFunctionalityException(msg, e);
        }
    }
    
    public static void registerDataInputInDB(Result result) {
        Observer       observer    = result.getObserver();
        DataSourceType dstype      = result.getDataSourceType();
        MarketPlace    marketplace = result.getMarketPlace();
        Subject        subject     = result.getSubject();

        String observer_and_type = buildObserverAndTypeString(observer, dstype);

		DataInputRegistry direg = new DataInputRegistry(observer_and_type, result.getStart(), result.getEnd(), subject.getName(), marketplace.getName(), DateUtil.getToday());
		DBAccess.createObjectInDatabase(direg);        
    }
    
    protected static String escapeFileSystemCharacters(String input) {
        input = input.replaceAll(File.separator, "");
        return input;
    }
        
	public static void writeContentToCache(Observer observer, DataSourceType dstype, MarketPlace market, Subject subject, Date start, Date end, RCDataItemContainer[] rccontainer) throws FileNotFoundException, IOException, MappingException, MarshalException, ValidationException {
		//write content to a file in the cache director
	    String observer_and_type = buildObserverAndTypeString(observer, dstype);

	    Date today = DateUtil.getToday();
	    String fn       = observer_and_type + "-" + market.getName() + "-" + subject.getName() + "-" + DateUtil.formatDate(start) + "-to-" + DateUtil.formatDate(end) + "-readdate-" + DateUtil.formatAccurateDate(today) + ".xml";
		String filename = ConfigurationHandler.getDataInputCacheDir() + "/" + escapeFileSystemCharacters(fn);
		File file = new File(filename);
		Writer fwriter = new FileWriter(file);
		
		Mapping mapping = new Mapping();
		mapping.loadMapping(ConfigurationHandler.getCastorMapping());
		Marshaller marshaller = new Marshaller(fwriter);
		marshaller.setMapping(mapping);

		MarshalListener listener = new EnumerateObjectsMarshalListener();
		marshaller.setMarshalListener(listener);
		
		//marshaller.setMarshalAsDocument(true);
		RCCacheFile rccachefile = new RCCacheFile(subject.getName(), observer.getName(), dstype.getName(), market.getName(), start, end, today, rccontainer);
		
		marshaller.marshal(rccachefile);
		fwriter.close();
	}
	
	/*
	 * the cachefile needs to have a filename with a special format, because in the filename there is additional info encoded:
	 * onvista-htmlshare-UNDEFINED-EE0000001063-2004-03-31-to-2005-03-31-readdate-2005-04-01_163538.xml
	 * all elements of the name are separated by "-"
	 * the elements in order are
	 * - the observer name
	 * - the data source type identifier
	 * - the market place name
	 * - the isin of the security paper
	 * the other parts of the name are not required.
	 */
	public static Result readFromCache(String cachefile) throws FileNotFoundException, IOException, MappingException, MarshalException, ValidationException, DBException {
		String filename = ConfigurationHandler.getDataInputCacheDir() + "/" + cachefile;
		File file = new File(filename);
		Reader freader = new FileReader(file);
		
		Mapping mapping = new Mapping();
		mapping.loadMapping(ConfigurationHandler.getCastorMapping());
		Unmarshaller unmarshaller = new Unmarshaller();
		unmarshaller.setMapping(mapping);
		unmarshaller.setValidation(false);
		
		RCCacheFile rccachefile = (RCCacheFile) unmarshaller.unmarshal(freader); 
		RCDataItemContainer[] rccontainer = rccachefile.getRCContainerCorrectType();
		if(null == rccontainer || rccontainer.length == 0) {
            throw new ValidationException("In the cachefile: " + cachefile + " there are no rccontainer components!");		    
		}
		int size = -1;
		for (int i = 0; i < rccontainer.length; i++) {
            if(0 == i) {
                size = rccontainer[i].getDataItems().size();                
            } else {
                if(rccontainer[i].getDataItems().size() != size) {
                    throw new ValidationException("In the cachefile: " + cachefile + " are rccontainers with different data item sizes!");
                }                
            }
        }
		
		// from the start of the name of the file i get the observer name and observer type		
        DataSource  data_source = DBAccessConvenience.getDataSource(rccachefile.getObserverName() + "-" + rccachefile.getDataSourceTypeName());
        
        Subject subject = DBAccessConvenience.getSecurityPaper(rccachefile.getSubjectName());
        
        ObserverDataSourceConfiguration[] info = new ObserverDataSourceConfiguration[rccontainer.length];
	    for (int i = 0; i < info.length; i++) {
			String query = "SELECT config FROM " + ObserverDataSourceConfiguration.class.getName() + " config WHERE config.observedAt.name = $1 and config.observerDataSource = $2 and config.property.name = $3";
			{
				Object[] query_arguments = {rccontainer[i].getMarketPlaceName(), data_source, rccontainer[i].getPropertyName()};
				Object obj = null;
				try {
	                obj = DBAccess.getObjectFromDatabase(query, query_arguments, true);
	                info[i] = (ObserverDataSourceConfiguration) obj;
	            } catch (Exception e) {
	                throw new DBException(e);
	            }
			}
        }
	    
		Result container = new Result(info);
		ResultItem[] ritems = new ResultItem[size];
		HashMap datemap = new HashMap();
		for(int i = 0; i < rccontainer.length; i++) {
		    Collection dataitems = rccontainer[i].getDataItems();
		    Object[] dataitemsarray = dataitems.toArray();
		    for(int j = 0; j < size; j++) {
		        RCDataItem di = (RCDataItem) dataitemsarray[j];
		        Date date = di.getDate();
		        ResultItem ri = (ResultItem) datemap.get(date);
		        if(null == ri) {
				    ri = new ResultItem(container, date);
				    ri.setValues(new DataItem[info.length]);
				    ritems[j] = ri;
				    datemap.put(date, ri);
		        }
		        if(di instanceof RCDoubleDataItem) {
		            RCDoubleDataItem ddi = (RCDoubleDataItem) di;
		            ri.getValues()[i] = new DoubleDataItem(date, subject, info[i]);
		        } else {
	                throw new ValidationException("Got an RCDataItem of unknow type?");
		        }
		    }
		}		
		
		container.setResultItems(ritems);
		return container;	    
	}
	
}
