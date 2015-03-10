/*
 * Created on 18.09.2004 by CS
 *
 */
package net.sf.ojts.datainput.xmlimport;

import net.sf.ojts.util.ConfigurationHandler;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.xml.XMLFieldDescriptor;


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
public class IDResolver implements org.exolab.castor.xml.IDResolver {

	/* (non-Javadoc)
	 * @see org.exolab.castor.xml.IDResolver#resolve(java.lang.String)
	 */
	public Object resolve(String arg0, XMLFieldDescriptor descriptor, Object parent) {
		Logger log = Logger.getLogger(this.getClass());
		log.debug("IDResolver.resolve(idref: " + arg0 + ", cls: " + descriptor.getFieldType() + ")");
		Database db = ConfigurationHandler.getDatabase();
		Object result = null;
		try {
			Integer id = new Integer(Integer.parseInt(arg0));
			result = db.load(descriptor.getFieldType(), id);
		} catch (Exception e) {
			log.error("Could not locate object of type: " + descriptor.getFieldType() + " with id: " + arg0, e);
		}
		return result;
	}
}
