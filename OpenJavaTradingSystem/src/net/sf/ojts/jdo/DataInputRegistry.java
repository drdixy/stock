/*
 * Created on 19.11.2004 by cs
 *
 */
package net.sf.ojts.jdo;

import java.util.Date;

/**
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

/**
 * @author cs
 *
 * @table DataInputRegistry
 * @xml   DataInputRegistry
 * @key-generator MAX 
 */
public class DataInputRegistry {

	/**
	 * @primary-key
	 */	
	protected int              id;
	
	protected String           observer;
	protected Date             startDate;
	protected Date             endDate;
	protected String           subject;
	protected String           market;
    	
	protected Date             whenDate;
	
    public DataInputRegistry() {
        super();
    }
    
    public DataInputRegistry(String observer, Date start, Date end, String subject, String market, Date when) {
        this.observer  = observer;
        this.startDate = start;
        this.endDate   = end;
        this.subject   = subject;
        this.market    = market;
        this.whenDate  = when;
    }    
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getMarket() {
        return market;
    }
    public void setMarket(String market) {
        this.market = market;
    }
    public String getObserver() {
        return observer;
    }
    public void setObserver(String observer) {
        this.observer = observer;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }    

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getWhenDate() {
        return whenDate;
    }
    public void setWhenDate(Date whenDate) {
        this.whenDate = whenDate;
    }
}
