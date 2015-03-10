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
 * Created on 19.08.2004 by CS
 *
 */
package net.sf.ojts.jdo;

/**
 * @author CS
 *
 * for currencies use as name the ISO 4217 currency codes!!
 * http://en.wikipedia.org/wiki/ISO_4217
 *
 * @table Unit
 * @xml   Unit
 * @xkey-generator MAX 
 */
public class Unit {

	/**
	 * @primary-key
	 */
	protected int          id   = -1;
	protected String       name = "N/A";
	protected PropertyType propertyTypeLink = null; // element with the same propertyTypeLink can be converted to oneanother
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PropertyType getPropertyTypeLink() {
		return propertyTypeLink;
	}
	public void setPropertyTypeLink(PropertyType propertyTypeLink) {
		this.propertyTypeLink = propertyTypeLink;
	}
}
