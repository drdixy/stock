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
 * Created on 18.04.2005
 *
 */
package net.sf.ojts.jdo.subject;

import net.sf.ojts.jdo.Unit;

/**
 * @author cs
 *
 * @table CurrencyPair
 * @xml   CurrencyPair
 */
public class CurrencyPair extends SecurityPaper {

	/**
	 * @sql-name Unit_from
	 * @sql-index
	 * @sql-unique IDX_CurrencyPair_IDT
	 */
    protected Unit from = null;
	/**
	 * @sql-name Unit_to
	 * @sql-index
	 * @sql-unique IDX_CurrencyPair_IDT
	 */
    protected Unit to   = null;
    
    public CurrencyPair() {
        super();
    }
    
    public CurrencyPair(Unit from, Unit to) {
        super();
        this.from = from;
        this.to   = to;
        setName(buildName());
    }
    
    public Unit getFrom() {
        return from;
    }
    public void setFrom(Unit from) {
        this.from = from;
        if(null == getFrom() || null == getTo())
            return;
        setName(buildName());
    }
    public Unit getTo() {
        return to;
    }
    public void setTo(Unit to) {
        this.to = to;
        if(null == getFrom() || null == getTo())
            return;
        setName(buildName());
    }
    
    protected String buildName() {
        if(null == getFrom() || null == getTo())
            return "";
        return   getFrom().getName() + "/" + getTo().getName();        
    }
    
	public String getName() {
        if(null == getFrom() || null == getTo())
            return super.getName();
        String n = buildName();
        if(n.equals(super.getName()))
            return super.getName();
        setName(n);
		return n;
	}
    
}
