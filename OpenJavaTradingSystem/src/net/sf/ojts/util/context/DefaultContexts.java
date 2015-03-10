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
import net.sf.ojts.jdo.Unit;
import net.sf.ojts.jdo.subject.MarketPlace;

/**
 * @author cs
 *
 */
public class DefaultContexts {

	protected static CurrencyContextCache currency_context_cache_ = null;
	
	public static DataInputContext getDefaultDataInputContext(int type) {
		return DataInputContext.getContext(type);
	}
	
	public static CurrencyContextCache getDefaultCurrencyContextCache() {
		if(null != currency_context_cache_)
			return currency_context_cache_;
		String default_data_source  = "onvista-htmlcurrency";
		String default_market_place = "UNDEFINED";
        DataSource  cur_ds = DBAccessConvenience.getDataSource(default_data_source);
        MarketPlace cur_mt = DBAccessConvenience.getMarketPlace(default_market_place);
		currency_context_cache_ = new CurrencyContextCache(cur_ds, cur_mt);
		return currency_context_cache_;
	}
	
	public static class CurrencyContextCache {
		protected HashMap     cache_ = new HashMap();
		protected DataSource  ds_    = null;
		protected MarketPlace mt_    = null;
		public CurrencyContextCache(DataSource ds, MarketPlace mt) {
			ds_ = ds;
			mt_ = mt;
		}
		public CurrencyConversionContext getContextForTargetCurrency(String currency) {
	        Unit cur = DBAccessConvenience.getUnit(currency);
			return getContextForTargetCurrency(cur);
		}
		public CurrencyConversionContext getContextForTargetCurrency(Unit currency) {
			if(null == currency)
				return null;
			CurrencyConversionContext context = (CurrencyConversionContext) cache_.get(currency.getName());
			if(null != context)
				return context;
	        context = new CurrencyConversionContext(currency, ds_, mt_);
			if(null == context)
				return null;
			cache_.put(currency.getName(), context);			
			return context;
		}
	}

}
