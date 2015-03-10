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
 * Created on 08.05.2005
 *
 */
package net.sf.ojts.util.context;

import java.util.HashMap;

import net.sf.ojts.functionality.DBAccessConvenience;
import net.sf.ojts.jdo.DataSource;
import net.sf.ojts.jdo.subject.Fund;
import net.sf.ojts.jdo.subject.MarketPlace;
import net.sf.ojts.jdo.subject.Share;

/**
 * @author cs
 *
 */
public class DataInputContext {
	
	public static int SHARE_CONTEXT = 0;
	public static int FUNDS_CONTEXT = 1;
	
	protected static DataInputContext[] contexts_ = new DataInputContext[]{null,null};
	protected static HashMap class_to_type_map_ = new HashMap();
	
	static {
		class_to_type_map_.put(Share.class.getName(), new Integer(SHARE_CONTEXT));
		class_to_type_map_.put(Fund.class.getName() , new Integer(FUNDS_CONTEXT));
	}
	
	protected DataSource  data_source_  = null;
	protected MarketPlace market_place_ = null;
	
	public DataInputContext(DataSource ds, MarketPlace mt) {
		data_source_  = ds;
		market_place_ = mt;
	}
	
	public DataSource getDataSource() {
		return data_source_;
	}
	
	public MarketPlace getMarketPlace() {
		return market_place_;
	}

	public static DataInputContext getContext(Class cls) {
		return getContext(cls.getName());
	}

	public static DataInputContext getContext(String cls) {
		Integer index = (Integer) class_to_type_map_.get(cls);
		if(null == index)
			return null;
		int type = index.intValue();
		return getContext(type);
	}
	
	public static DataInputContext getContext(int type) {
		if(null != contexts_[type])
			return contexts_[type];
        DataSource funds_ds = DBAccessConvenience.getDataSource("onvista-htmlfund");
        DataSource share_ds = DBAccessConvenience.getDataSource("onvista-htmlshare");
		MarketPlace mt = DBAccessConvenience.getMarketPlace("UNDEFINED");

		DataInputContext share_context = new DataInputContext(share_ds, mt);
		contexts_[SHARE_CONTEXT] = share_context;
		DataInputContext funds_context = new DataInputContext(funds_ds, mt);
		contexts_[FUNDS_CONTEXT] = funds_context;
		return contexts_[type];
	}
	
}
