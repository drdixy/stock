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

import java.util.Date;

import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.jdo.subject.SecurityPaper;
import net.sf.ojts.jdo.subject.Subject;

/**
 * @author CS
 *
 * @table DataItem
 * @xml   DataItem
 * @key-generator MAX 
 */
public class DataItem {

	/**
	 * @primary-key
	 */
	protected int                             id;
	/**
	 * @sql-index
	 * @sql-unique IDX_DataItem_IDT
	 */
	protected Date                            time;
	/**
	 * @sql-index
	 * @sql-unique IDX_DataItem_IDT
	 */
	protected Subject                         subject;
	/**
	 * @sql-index
	 * @sql-unique IDX_DataItem_IDT
	 */
	protected ObserverDataSourceConfiguration source;
	
	public DataItem() {
		super();
	}

	public DataItem(Date time, Subject subject, ObserverDataSourceConfiguration source) {
		super();
		this.time     = time;
		this.subject  = subject;
		this.source   = source;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Property getProperty() {
		return source.getProperty();
	}

	public ObserverDataSourceConfiguration getSource() {
		return source;
	}
	public void setSource(ObserverDataSourceConfiguration source) {
		this.source = source;
	}
	public Subject getSubject() {
		return subject;
	}
	public void setSubject(Subject subject) {
		this.subject = subject;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public Unit getUnit() {
	    Unit unit = source.getUnit();
	    subject = DBAccessConvenience.downcastSubject(subject);
	    if(null == unit && (subject instanceof SecurityPaper))
	        unit = ((SecurityPaper) subject).getDefaultCurrency();
		return unit;
	}
}
