
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
 * Created on 21.08.2004 by CS
 *
 */
package net.sf.ojts.jdo;

import net.sf.ojts.jdo.subject.MarketPlace;

/**
 * @author CS
 *
 * @table ObserverDataSourceConfiguration
 * @xml   ObserverDataSourceConfiguration
 * @xkey-generator MAX 
 */
public class ObserverDataSourceConfiguration {

	/**
	 * @primary-key 
	 */
	protected int         id;
	/**
	 * @sql-unique IDX_ODSC_IDT
	 */
	protected Property    property;
	/**
	 * @sql-name MarketPlace_id
	 * @sql-unique IDX_ODSC_IDT
	 * @sql-index
	 */
	protected MarketPlace observedAt;// marketplace
	/**
	 * @sql-unique IDX_ODSC_IDT
	 * @sql-index
	 */
	protected DataSource  observerDataSource;
	/**
	 * @sql-index
	 */
	protected Unit        unit = null;
	protected String      colu = null;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getColu() {
		return colu;
	}
	public void setColu(String colu) {
		this.colu = colu;
	}
	public MarketPlace getObservedAt() {
		return observedAt;
	}
	public void setObservedAt(MarketPlace observedAt) {
		this.observedAt = observedAt;
	}
	public DataSource getObserverDataSource() {
		return observerDataSource;
	}
	public void setObserverDataSource(DataSource observerDataSource) {
		this.observerDataSource = observerDataSource;
	}
	public Property getProperty() {
		return property;
	}
	public void setProperty(Property property) {
		this.property = property;
	}
	/*
	public Subject getSubject() {
		return subject;
	}
	public void setSubject(Subject subject) {
		this.subject = subject;
	}
	*/
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	
}
