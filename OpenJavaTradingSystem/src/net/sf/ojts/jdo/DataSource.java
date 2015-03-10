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

import net.sf.ojts.jdo.subject.Observer;

/**
 * @author CS
 *
 * @table DataSource
 * @xml   DataSource
 * @xkey-generator MAX 
 */
public class DataSource {

	/**
	 * @primary-key
	 */
	protected int             id;
	/**
	 * @sql-index
	 */
	protected DataSourceType  type;
	protected String          url;
	protected String          description;
	/**
	 * @sql-name Observer_id
	 * @sql-index
	 */
	protected Observer        observerLink;
	protected String          handlerClassName;
	protected int             dataLagInDays;
	
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
	public DataSourceType getType() {
		return type;
	}
	public void setType(DataSourceType type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Observer getObserverLink() {
		return observerLink;
	}
	public void setObserverLink(Observer subjectLink) {
		this.observerLink = subjectLink;
	}
	public String getHandlerClassName() {
		return handlerClassName;
	}
	public void setHandlerClassName(String handlerClassName) {
		this.handlerClassName = handlerClassName;
	}
	public String getObserverTypeString() {
	    return "" + getObserverLink().getName() + "-" + getType().getName();
	}	
    public int getDataLagInDays() {
        return dataLagInDays;
    }
    public void setDataLagInDays(int dataLagInDays) {
        this.dataLagInDays = dataLagInDays;
    }
}
