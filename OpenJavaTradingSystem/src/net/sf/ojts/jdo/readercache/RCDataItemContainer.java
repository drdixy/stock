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

/*
 * Created on 31.03.2005 by cs
 *
 */


package net.sf.ojts.jdo.readercache;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author cs
 *
 * @table RCDataItemContainer
 * @xml   RCDataItemContainer
 * @key-generator MAX 
 */
public class RCDataItemContainer {

	/**
	 * @primary-key
	 */
	protected int       id;

	protected String    securityPaperISIN = "";
    protected String    marketPlaceName   = "";
    protected String    propertyName      = "";
    protected String    unit              = "";

	/**
	 * @sql-name   RCDataItem_id
	 * @field-type RCDataItem
	 * @sql-dirty check
	 */
    protected Collection dataItems         = new ArrayList();
    
    public RCDataItemContainer() {
        super();
    }
    
    /**
     * @param securityPaperISIN
     * @param marketPlaceName
     * @param propertyName
     * @param unit
     */
    public RCDataItemContainer(String securityPaperISIN, String marketPlaceName, String propertyName, String unit) {
        super();
        this.securityPaperISIN = securityPaperISIN;
        this.marketPlaceName = marketPlaceName;
        this.propertyName = propertyName;
        this.unit = unit;
    }
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
    
    public Collection getDataItems() {
        return dataItems;
    }
    public void setDataItems(ArrayList dataItems) {
        this.dataItems = dataItems;
    }    
    public void addDataItem(RCDataItem item) {
        dataItems.add(item);
    }
    public void removeDataItem(Object item) {
        dataItems.remove(item);
    }    
    public String getMarketPlaceName() {
        return marketPlaceName;
    }
    public void setMarketPlaceName(String marketPlaceName) {
        this.marketPlaceName = marketPlaceName;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    public String getSecurityPaperISIN() {
        return securityPaperISIN;
    }
    public void setSecurityPaperISIN(String securityPaperISIN) {
        this.securityPaperISIN = securityPaperISIN;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
