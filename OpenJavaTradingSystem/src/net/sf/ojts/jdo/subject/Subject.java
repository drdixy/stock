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
 * Created on 07.08.2004 by CS
 *
 */
package net.sf.ojts.jdo.subject;

/**
 * @author CS
 *
 * @table Subject
 * @xml   Subject
 * @key-generator MAXOrKeep 
 */

public class Subject {

	/*
	 * not really necessary to specify 
	 * @field-type  java.lang.Integer
	 * 
	 * @field-name  id
	 * 
	 * @sql-name    id
	 * @sql-type    integer
	 * 
	 * @xml-name    id
	 */
	
	/**
	 * @primary-key
	 */
	private int    id = -1;
	
	/**
	 * @sql-unique
	 */
	private String name;
	private String description;
	private String urlSources;	
	
	private String typeId;
	
    public Subject() {
        super();
        typeId = this.getClass().getName();
    }
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrlSources() {
		return urlSources;
	}
	public void setUrlSources(String urlsources) {
		this.urlSources = urlsources;
	}	
    public String getTypeId() {
        return typeId;
    }
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }
}
