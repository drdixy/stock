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
 * Created on 02.04.2005 cs
 *
 */
package net.sf.ojts.jdo.readercache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * @author cs
 *
 * @table RCCacheFile
 * @xml   RCCacheFile
 * @key-generator MAX 
 */
public class RCCacheFile {

	/**
	 * @primary-key
	 */
	protected int    id;
	
	protected String subjectName        = "";
	protected String observerName       = "";
	protected String dataSourceTypeName = "";
	protected String marketPlaceName    = "";
	protected Date   from               = null;
	protected Date   to                 = null;
	protected Date   readdate           = null;
	
	//protected RCDataItemContainer[] rCContainer = new RCDataItemContainer[0];
	/**
	 * @sql-name   RCDataItemContainer_id
	 * @field-type RCDataItemContainer
	 * @sql-dirty check
	 */
	protected Collection rCContainer    = new ArrayList();
	
    public RCCacheFile() {
        super();
    }
    
    public RCCacheFile(String subjectName, String observerName, String dataSourceTypeName, String marketPlaceName, Date from, Date to, Date readdate, RCDataItemContainer[] rccontainer) {
        super();
        this.subjectName        = subjectName;
        this.observerName       = observerName;
        this.dataSourceTypeName = dataSourceTypeName;
        this.marketPlaceName    = marketPlaceName;
        this.from               = from;
        this.to                 = to;
        this.readdate           = readdate;
        this.rCContainer        = Arrays.asList(rccontainer);
    }
    
    public String getDataSourceTypeName() {
        return dataSourceTypeName;
    }
    public void setDataSourceTypeName(String dataSourceTypeName) {
        this.dataSourceTypeName = dataSourceTypeName;
    }
    public Date getFrom() {
        return from;
    }
    public void setFrom(Date from) {
        this.from = from;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getMarketPlaceName() {
        return marketPlaceName;
    }
    public void setMarketPlaceName(String marketPlaceName) {
        this.marketPlaceName = marketPlaceName;
    }
    public String getObserverName() {
        return observerName;
    }
    public void setObserverName(String observerName) {
        this.observerName = observerName;
    }
    public RCDataItemContainer[] getRCContainerCorrectType() {
        if(null == rCContainer)
            return new RCDataItemContainer[0];
        RCDataItemContainer[] result = new RCDataItemContainer[rCContainer.size()];
        Iterator iter = rCContainer.iterator();
        int i = 0;
        while(iter.hasNext()) {
            result[i] = (RCDataItemContainer) iter.next(); i++;
        }
        return result;
    }
    public void setRCContainerCorrectType(RCDataItemContainer[] rccontainer) {
        this.rCContainer = Arrays.asList(rccontainer);
    }
    public Collection getRCContainer() {
        return rCContainer;
    }
    public void setRCContainer(Collection rccontainer) {
        this.rCContainer = rccontainer;
    }
    public Date getReaddate() {
        return readdate;
    }
    public void setReaddate(Date readdate) {
        this.readdate = readdate;
    }
    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    public Date getTo() {
        return to;
    }
    public void setTo(Date to) {
        this.to = to;
    }
}
