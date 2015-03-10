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
 * Created on 29.08.2004 by CS
 *
 */
package net.sf.ojts.datainput;

import java.util.Date;

import net.sf.ojts.jdo.DataItem;

/**
 * @author CS
 *
 */
public class ResultItem {
	protected Result     container = null;
	protected Date       date      = null;
	protected DataItem[] values    = null;
	
	public ResultItem() {
		super();
	}

	public ResultItem(Result container, Date date) {
		super();
		this.container = container;
		this.date      = date;
	}
	
	public Result getContainer() {
		return container;
	}
	public void setContainer(Result container) {
		this.container = container;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public DataItem[] getValues() {
		return values;
	}
	public void setValues(DataItem[] values) {
		this.values = values;
	}
}
