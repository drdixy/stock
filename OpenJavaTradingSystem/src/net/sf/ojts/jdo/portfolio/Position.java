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
 * Created on 18.12.2004 by cs
 *
 */
package net.sf.ojts.jdo.portfolio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.SecurityPaper;

/**
 * @author cs
 *
 * @table PPosition
 * @xml   Position
 * @key-generator MAXOrKeep 
 */
public class Position implements Cloneable {

	/**
	 * @primary-key
	 */
	protected int                             id = -1;

	/**
	 * @sql-index
	 */
	protected SecurityPaper                   subject;
	protected double                          quantity = -1;
	protected String                          comment;
	
	/**
	 * @sql-name MP_buy
	 */
	protected MarketPlace                     boughtAt;
	/**
	 * @sql-name openDate
	 */
	protected Date                            open;
	protected double                          openPrice   = -1;
	protected double                          openCharges =  0;
	/**
	 * @sql-name UT_buy
	 */
	protected Unit                            openPriceCurrency;
	
	/**
	 * @sql-name MP_sell
	 */
	protected MarketPlace                     soldAt;
	/**
	 * @sql-name closeDate
	 */
	protected Date                            close;
	protected double                          closePrice   = -1;
	protected double                          closeCharges =  0;
	/**
	 * @sql-name UT_sell
	 */
	protected Unit                            closePriceCurrency;

	/**
	 * @lazy
	 * @sql-name   Portfolio_id
	 * @field-type net.sf.ojts.jdo.subject.Portfolio
	 * @many-table PorPos
	 * @many-key   PPosition_id
	 * @sql-dirty check
	 */
	protected Collection portfolios = new ArrayList();
	
	protected DataSource preferredDataSource = null;

    public Position() {
		super();
    }
	    
    
    public MarketPlace getBoughtAt() {
        return boughtAt;
    }
    public void setBoughtAt(MarketPlace boughtAt) {
        this.boughtAt = boughtAt;
    }
    public Date getClose() {
        return close;
    }
    public void setClose(Date close) {
        this.close = close;
    }
    public double getClosePrice() {
        return closePrice;
    }
    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }
    public Unit getClosePriceCurrency() {
        return closePriceCurrency;
    }
    public void setClosePriceCurrency(Unit closePriceCurrency) {
        this.closePriceCurrency = closePriceCurrency;
    }
    
    public Collection getPortfolios() {
        return portfolios;
    }
    public void setPortfolios(Collection portfolios) {
        this.portfolios = portfolios;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Date getOpen() {
        return open;
    }
    public void setOpen(Date open) {
        this.open = open;
    }
    public double getOpenPrice() {
        return openPrice;
    }
    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }
    public Unit getOpenPriceCurrency() {
        return openPriceCurrency;
    }
    public void setOpenPriceCurrency(Unit openPriceCurrency) {
        this.openPriceCurrency = openPriceCurrency;
    }
    public double getQuantity() {
        return quantity;
    }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    public MarketPlace getSoldAt() {
        return soldAt;
    }
    public void setSoldAt(MarketPlace soldAt) {
        this.soldAt = soldAt;
    }
    public SecurityPaper getSubject() {
        return subject;
    }
    public void setSubject(SecurityPaper subject) {
        this.subject = subject;
    }    
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
	public double getCloseCharges() {
		return closeCharges;
	}

	public void setCloseCharges(double closeCharges) {
		this.closeCharges = closeCharges;
	}

	public double getOpenCharges() {
		return openCharges;
	}

	public void setOpenCharges(double openCharges) {
		this.openCharges = openCharges;
	}
	
	public Position copy() {
		Position result = new Position();
		result.setBoughtAt(getBoughtAt());
		result.setClose(getClose());
		result.setCloseCharges(getCloseCharges());
		result.setClosePrice(getClosePrice());
		result.setComment(getComment());
		result.setId(-1);// copy does not have a db identity so far
		result.setOpen(getOpen());
		result.setOpenCharges(getOpenCharges());
		result.setOpenPrice(getOpenPrice());
		result.setOpenPriceCurrency(getOpenPriceCurrency());
		result.setPortfolios(new ArrayList());// copy does not belong to any portfolio so far
		result.setQuantity(getQuantity());
		result.setSoldAt(getSoldAt());
		result.setSubject(getSubject());
		return result;
	}

	protected Object clone() throws CloneNotSupportedException {
		return copy();
	}
	public DataSource getPreferredDataSource() {
		return preferredDataSource;
	}
	public void setPreferredDataSource(DataSource preferredDataSource) {
		this.preferredDataSource = preferredDataSource;
	}
}
