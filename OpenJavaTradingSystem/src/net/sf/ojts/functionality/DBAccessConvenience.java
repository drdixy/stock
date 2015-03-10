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

import org.apache.log4j.Logger;

import net.sf.ojts.datainput.exceptions.DBException;
import net.sf.ojts.functionality.exceptions.OJTSFunctionalityException;
import net.sf.ojts.jdo.Alias;
import net.sf.ojts.jdo.AliasType;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.DataSourceType;
import net.sf.ojts.jdo.ObserverDataSourceConfiguration;
import net.sf.ojts.jdo.Property;
import net.sf.ojts.jdo.PropertyType;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.CurrencyPair;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Observer;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.jdo.subject.Subject;

/**
 * @author cs
 *
 */
public class DBAccessConvenience {

    public static String[] splitObserverString(String observer_and_type) throws OJTSFunctionalityException {
        String[] parts = observer_and_type.split("-");
        if(null == parts || 2 != parts.length)
            throw new OJTSFunctionalityException("The observer String does not have the right format XXXXX-YYY: " + observer_and_type);
        return parts;        
    }

    public static DataSource getDataSource(String observer_and_type) {
    	Logger log = Logger.getLogger(DBAccessConvenience.class);
		DataSource result = null;
		try {
	        String[] parts = splitObserverString(observer_and_type);
	        result =  DBAccessConvenience.getDataSource(parts[0], parts[1]);			
		} catch(Exception ex) {
			// ignore
			log.debug("Exception was thrown while trying to get data source: " + observer_and_type, ex);
		}
		return result;
    }

    public static DataSource getDataSource(String observer, String data_source_type) {
    	Logger log = Logger.getLogger(DBAccessConvenience.class);
    	DataSource  data_source     = null;
		try {
			String      query           = "SELECT ds FROM " + DataSource.class.getName() + " ds WHERE ds.observerLink.name = $1 and ds.type.name = $2";
			Object[]    query_arguments = {observer, data_source_type};            
			data_source                 = (DataSource) DBAccess.getObjectFromDatabase(query, query_arguments);
		} catch(Exception e) {
			// ignore
			log.debug("Exception was thrown while trying to get data source: " + observer + "-" +data_source_type, e);
		}
        return data_source;
    }

    public static PropertyType createPropertyType(int id, String name, String description) {
        PropertyType result = new PropertyType();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static PropertyType getPropertyType(int id) {
        return (PropertyType) DBAccess.getObjectById(PropertyType.class, id);
    }

    public static PropertyType getPropertyType(String name) {
        return (PropertyType) DBAccess.getObjectByName(PropertyType.class, name);
    }

    public static Property createProperty(int id, String name, String description, PropertyType type) {
        Property result = new Property();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        result.setType(type);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static Property getProperty(int id) {
        return (Property) DBAccess.getObjectById(Property.class, id);
    }

    public static Property getProperty(String name) {
        return (Property) DBAccess.getObjectByName(Property.class, name);
    }

    public static Unit createUnit(int id, String name, PropertyType type_link) {
        Unit result = new Unit();
        result.setId(id);
        result.setName(name);
        result.setPropertyTypeLink(type_link);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static Unit getUnit(int id) {
        return (Unit) DBAccess.getObjectById(Unit.class, id);
    }

    public static Unit getUnit(String name) {
        return (Unit) DBAccess.getObjectByName(Unit.class, name);
    }

    public static Subject createSubject(int id, String name, String description, String urlsources) {
        Subject result = new Subject();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        result.setUrlSources(urlsources);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static Subject getSubject(int id) {
        return (Subject) DBAccess.getObjectById(Subject.class, id);
    }

    public static Subject getSubject(String name) {
        return (Subject) DBAccess.getObjectByName(Subject.class, name);
    }

    public static SecurityPaper createSecurityPaper(int id, String name, String description, String urlsources) {
        SecurityPaper result = new SecurityPaper();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        result.setUrlSources(urlsources);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static Subject downcastSubject(Subject input) {
        if(input.getTypeId().equals(input.getClass().getName()))
            return input;// type already ok
        String query = "SELECT obj FROM " + input.getTypeId() + " obj WHERE obj.id = $1";
        Object[] args = {new Integer(input.getId())};
        Object result = null;
        try {
            result = DBAccess.getObjectFromDatabase(query, args);
        } catch(Exception ex) {
            // ignore
        }
        return (Subject) result;        
    }

    public static SecurityPaper getSecurityPaper(int id) {
        return (SecurityPaper) DBAccess.getObjectById(SecurityPaper.class, id);
    }

    public static SecurityPaper getSecurityPaper(String name) {
        return (SecurityPaper) DBAccess.getObjectByName(SecurityPaper.class, name);
    }

    public static MarketPlace createMarketPlace(int id, String name, String description, String urlsources) {
        MarketPlace result = new MarketPlace();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        result.setUrlSources(urlsources);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static MarketPlace getMarketPlace(int id) {
        return (MarketPlace) DBAccess.getObjectById(MarketPlace.class, id);
    }

    public static MarketPlace getMarketPlace(String name) {
        return (MarketPlace) DBAccess.getObjectByName(MarketPlace.class, name);
    }

    public static Observer createObserver(int id, String name, String description, String urlsources) {
        Observer result = new Observer();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        result.setUrlSources(urlsources);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static Observer getObserver(int id) {
        return (Observer) DBAccess.getObjectById(Observer.class, id);
    }

    public static Observer getObserver(String name) {
        return (Observer) DBAccess.getObjectByName(Observer.class, name);
    }

    public static AliasType createAliasType(int id, String name, String description, Observer observer_link, String urlsource) {
        AliasType result = new AliasType();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        result.setObserverLink(observer_link);
        result.setUrlSource(urlsource);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static AliasType getAliasType(int id) {
        return (AliasType) DBAccess.getObjectById(AliasType.class, id);
    }

    public static AliasType getAliasType(String name) {
        return (AliasType) DBAccess.getObjectByName(AliasType.class, name);
    }

    public static Alias createAlias(int id, String alias, MarketPlace market, Subject subject, AliasType type) {
        Alias result = new Alias();
        result.setId(id);
        result.setAlias(alias);
        result.setMarket(market);
        result.setSubject(subject);
        result.setType(type);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static Alias getAlias(int id) {
        return (Alias) DBAccess.getObjectById(Alias.class, id);
    }

    public static Alias getAlias(String name) {
        return (Alias) DBAccess.getObjectByName(Alias.class, name, "alias");
    }

    public static DataSourceType createDataSourceType(int id, String name, String description) {
        DataSourceType result = new DataSourceType();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static DataSourceType getDataSourceType(int id) {
        return (DataSourceType) DBAccess.getObjectById(DataSourceType.class, id);
    }

    public static DataSourceType getDataSourceType(String name) {
        return (DataSourceType) DBAccess.getObjectByName(DataSourceType.class, name);
    }

    public static DataSource createDataSource(int id, String description, String url, DataSourceType type, Observer observer_link, String handler_class_name) {
        DataSource result = new DataSource();
        result.setId(id);
        result.setDescription(description);
        result.setUrl(url);
        result.setType(type);
        result.setObserverLink(observer_link);
        result.setHandlerClassName(handler_class_name);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static DataSource getDataSource(int id) {
        return (DataSource) DBAccess.getObjectById(DataSource.class, id);
    }

    public static ObserverDataSourceConfiguration createObserverDataSourceConfiguration(int id, DataSource observer_data_source, MarketPlace observed_at, Property property, Unit unit, String colu) {
        ObserverDataSourceConfiguration result = new ObserverDataSourceConfiguration();
        result.setId(id);
        result.setObserverDataSource(observer_data_source);
        result.setObservedAt(observed_at);
        result.setProperty(property);
        result.setUnit(unit);
        result.setColu(colu);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static ObserverDataSourceConfiguration getObserverDataSourceConfiguration(int id) {
        return (ObserverDataSourceConfiguration) DBAccess.getObjectById(ObserverDataSourceConfiguration.class, id);
    }

    public static ObserverDataSourceConfiguration[] getObserverDataSourceConfiguration(DataSource data_source, MarketPlace market) throws DBException {
    	String query = "SELECT config FROM " + ObserverDataSourceConfiguration.class.getName() + " config WHERE config.observedAt = $1 and config.observerDataSource = $2";
    	ObserverDataSourceConfiguration[] info = null;
    	{
    		Object[] query_arguments = {market, data_source};
    		Object[] objs = null;
    		try {
                objs = DBAccess.getObjectsFromDatabase(query, query_arguments, true);
            } catch (Exception e) {
                throw new DBException(e);
            }
    		
    		info = new ObserverDataSourceConfiguration[objs.length];
    		for (int i = 0; i < objs.length; i++) {
                info[i] = (ObserverDataSourceConfiguration) objs[i];                
            }
    	}
    	return info;
    }

    public static CurrencyPair createCurrencyPair(int id, String description, String urlsources, Unit from, Unit to) {
        CurrencyPair result = new CurrencyPair();
        result.setId(id);
        result.setDescription(description);
        result.setUrlSources(urlsources);
        result.setFrom(from);
        result.setTo(to);
        if(DBAccess.createObjectInDatabase(result))
            return result;
        else
            return null;
    }

    public static CurrencyPair getCurrencyPair(int id) {
        return (CurrencyPair) DBAccess.getObjectById(CurrencyPair.class, id);
    }

    public static CurrencyPair getCurrencyPair(String name) {
        return (CurrencyPair) DBAccess.getObjectByName(CurrencyPair.class, name);
    }

    public static CurrencyPair getCurrencyPair(Unit from, Unit to) {
    	Logger log = Logger.getLogger(DBAccessConvenience.class);

    	String   query = "SELECT obj FROM " + CurrencyPair.class.getName() + " obj WHERE obj.from = $1 and obj.to = $2";
        Object[] args = {from, to};
        CurrencyPair result = null;
        try {
            result = (CurrencyPair) DBAccess.getObjectFromDatabase(query, args);
        } catch (Exception e) {
            log.error("getCurrencyPair(Unit,Unit): Exception was thrown!", e);
        }
        return result;
    }
    
    public static CurrencyPair getCurrencyPairCreateIfNotExists(Unit from, Unit to) {
    	Logger log = Logger.getLogger(DBAccessConvenience.class);

        CurrencyPair result = null;
    	{
        	String   query = "SELECT obj FROM " + CurrencyPair.class.getName() + " obj WHERE obj.from = $1 and obj.to = $2";
            Object[] args = {from, to};
            try {
                result = (CurrencyPair) DBAccess.getObjectFromDatabase(query, args);
            } catch (Exception e) {}
            if(null != result)
                return result;    	    
    	}
        
    	int id = -1;
    	{
        	String   query = "SELECT max(obj.id) FROM " + CurrencyPair.class.getName() + " obj";
            Object[] args = {};
            Integer maximum = null;
            try {
                maximum = (Integer) DBAccess.getObjectFromDatabase(query, args, true);
            } catch (Exception e) {
            }
            if(null == maximum)
                maximum = new Integer(0);
            id = maximum.intValue() + 1;
    	}
    	
    	result = createCurrencyPair(id, "", "", from, to);        
        return result;
    }
}
