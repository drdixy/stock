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
 * Created on 17.04.2005
 *
 */
package net.sf.ojts.jdo.conversion;

import net.sf.ojts.jdo.Unit;

/**
 * @author cs
 *
 * @table TypeConversion
 * @xml   TypeConversion
 * @xkey-generator MAX 
 */
public class TypeConversion {

	/**
	 * @primary-key
	 */
	protected int          id;	
	/**
	 * @sql-name Unit_from
	 * @sql-index
	 * @sql-unique IDX_TypeConversion_IDT
	 */
	protected Unit         from      = null;
	/**
	 * @sql-name Unit_to
	 * @sql-index
	 * @sql-unique IDX_TypeConversion_IDT
	 */
	protected Unit         to        = null;
	
	private   String       typeId    = "";
	
    public TypeConversion() {
        super();
        typeId = this.getClass().getName();
    }
    
    public TypeConversion(Unit from, Unit to) {
        this();
        this.from      = from;
        this.to        = to;
    }
        
    public Unit getFrom() {
        return from;
    }
    public void setFrom(Unit from) {
        this.from = from;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Unit getTo() {
        return to;
    }
    public void setTo(Unit to) {
        this.to = to;
    }    
    public String getTypeId() {
        return typeId;
    }
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }
}
