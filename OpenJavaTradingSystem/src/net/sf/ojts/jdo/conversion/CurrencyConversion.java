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


import net.sf.ojts.jdo.subject.CurrencyPair;

/**
 * @author cs
 *
 * @table CurConversion
 * @xml   CurrencyConversion
 */
public class CurrencyConversion extends TypeConversion {

    /**
	 * @sql-name CurPair_id
	 */
    protected CurrencyPair currencyPair = null;
    	
    public CurrencyConversion() {
        super();
    }

    public CurrencyConversion(CurrencyPair pair) {
        super(pair.getFrom(), pair.getTo());
        currencyPair = pair;
    }    
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }
    public void setCurrencyPair(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
    }
}
