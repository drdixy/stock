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
package net.sf.ojts.jdo.subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.sf.ojts.jdo.portfolio.Position;

/**
 * @author cs
 *
 * @table Portfolio
 * @xml   Portfolio
 */
public class Portfolio extends SecurityPaper {
	
	/**
	 * @lazy
	 * @sql-name   PPosition_id
	 * @field-type net.sf.ojts.jdo.portfolio.Position
	 * @many-table PorPos
	 * @many-key   Portfolio_id
	 * @sql-dirty check
	 */
	protected Collection positions = new ArrayList();
	
    public Portfolio() {
        super();
    }
	
    public Portfolio(String name) {
        super();
        setName(name);
    }
    public Collection getPositions() {
        return positions;
    }
    public void setPositions(Collection positions) {
        this.positions = positions;
    }
    public Position[] getPositionsAsArray() {
        if(null == positions)
            return new Position[0];
        Position[] result = new Position[positions.size()];
        int i = 0;
        for (Iterator iter = positions.iterator(); iter.hasNext();) {
            Position element = (Position) iter.next();
            result[i] = element; i++;
        }
        return result;
    }
    public void setPositionsAsArray(Position[] positions) {        
        this.positions = new ArrayList(Arrays.asList(positions));
    }    
    public void addPosition(Position position) {
        positions.add(position);
    }
    
}
