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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.ojts.datainput.xmlimport.IDResolver;
import net.sf.ojts.util.ConfigurationHandler;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author cs
 *
 */
public class DBConfiguration {

    public static boolean readXMLConfiguration(String filename) {
    	Logger log = Logger.getLogger(Functionality.class);
    	
    	// create a mapping for the xml file
    	Mapping mapping = new Mapping();
    	Unmarshaller unmarshaller = new Unmarshaller();
        try {
            String mapping_path = ConfigurationHandler.getCastorMapping();
            File mapping_file_path = new File(mapping_path);
            mapping.loadMapping(mapping_file_path.getAbsolutePath());
            unmarshaller.setMapping(mapping);
            unmarshaller.setValidation(false);
        } catch (IOException e) {
            log.error("While loading the mapping file an IOException occured.", e);
            return false;
        } catch (MappingException e) {
            log.error("While loading the mapping file a MappingException occured.", e);
            return false;
        }
    	
        // set a custom id resolver to resolve objects that are already in the database
    	IDResolver idresolver = new IDResolver();
    	unmarshaller.setIDResolver(idresolver);

    	// we need to parse the xml document and work on every element one by one, because
    	// the castor xml reading only works on whole elements and not on fragments by default.
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setValidating(false);
    	factory.setNamespaceAware(false);
    	DocumentBuilder builder = null;
    	try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("While creating a document builder factory a ParserConfigurationException occured.", e);
            return false;
        }
    
        // parse the document
    	File file = new File(filename);
        Document document = null;
    	try {
    	    document = builder.parse( file );
        } catch (SAXException e) {
            log.error("While parsing the file: " + filename + " a SAXException occured.", e);
            return false;
        } catch (IOException e) {
            log.error("While parsing the file: " + filename + " an IOException occured.", e);
            return false;
        }
    	Element root = document.getDocumentElement();
    	NodeList nl = root.getChildNodes();
    	int len = nl.getLength();
    	Object xmlobject = null;

    	Database db = null;
    	try {
            ConfigurationHandler.openDatabase();
            db = ConfigurationHandler.getDatabase();
    
            // iterate through all xml nodes, check if the element is already in the database and if not
            // create the element in the database
            for(int i = 0; i < len; i++) {
            	Node node = nl.item(i);
            	if(node.getNodeType() != Node.ELEMENT_NODE)
            		continue;
            	log.debug("node[" + i + "]: " + node.getNodeName());
            	xmlobject = unmarshaller.unmarshal(node);
            	log.debug("xmlobject: " + xmlobject.getClass() + " : " + xmlobject);
    
            	// find out if the object already exists in the database
            	boolean already_exists = false;
            	Object loaded = null;
            	try {
            		loaded = db.load(xmlobject.getClass(), ConfigurationHandler.getJDOIdentity(xmlobject));
            	} catch (ObjectNotFoundException e) {
            		// ignored
            	}
            	if(null != loaded)
            		already_exists = true;
    
            	if(already_exists) {
            	    log.debug("Object already exists.");
            		continue;
            	} else {
            	    log.debug("Creating object.");
            		try {
            			db.create(xmlobject);
            		} catch(Exception e) {
            			// here we will get already exists / collissions with unique, ... exceptions
                	    log.error("While trying to create an object an Exception occured.", e);
            		}
            	}
            }
            
            ConfigurationHandler.commitDatabase();
        } catch (Exception e) {
    	    log.error("Something went wrong while reading the xml configuration: " + filename, e);
    	    return false;
        } finally {
    		ConfigurationHandler.closeDatabase();
        }
        
        return true;
    }

}
