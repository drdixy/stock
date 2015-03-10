/*
 * Created on 21.11.2004 by cs
 *
 */
package net.sf.ojts.datainput.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;

import net.sf.ojts.util.ConfigurationHandler;
import net.sf.ojts.util.ConfigurationHandler.ConfigurationException;

import org.apache.log4j.Logger;

import com.pushtotest.tool.NoSuchClassException;
import com.pushtotest.tool.ToolException;
import com.pushtotest.tool.UnsupportedTypeException;
import com.pushtotest.tool.protocolhandler.Body;
import com.pushtotest.tool.protocolhandler.Header;
import com.pushtotest.tool.protocolhandler.NotReadyException;
import com.pushtotest.tool.protocolhandler.Protocol;
import com.pushtotest.tool.protocolhandler.ProtocolHandler;
import com.pushtotest.tool.response.Response;

/**
 * @author cs
 *
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
public class HttpWebUtils {

	protected Protocol                          protocol_    = null;
    
	public HttpWebUtils() {
		Logger log = Logger.getLogger(this.getClass());
		
		try {
			protocol_     = ProtocolHandler.getProtocol("http");
			Body   body   = ProtocolHandler.getBody("http");
			Header header = ProtocolHandler.getHeader("http");
			header.set("User-Agent", "Mozilla");
			protocol_.setBody(body);
			protocol_.setHeader(header);
			
			String proxy_host = ConfigurationHandler.getString(ConfigurationHandler.HTTP_PROXY_HOST);
			int    proxy_port = ConfigurationHandler.getInt   (ConfigurationHandler.HTTP_PROXY_PORT);
			String proxy_user = ConfigurationHandler.getString(ConfigurationHandler.HTTP_PROXY_USER);
			String proxy_pass = ConfigurationHandler.getString(ConfigurationHandler.HTTP_PROXY_PASS);
			protocol_.setProxyHost(proxy_host);
			protocol_.setProxyPort(proxy_port);
			protocol_.setProxyUser(proxy_user);
			protocol_.setProxyPass(proxy_pass);
		} catch (NoSuchClassException e) {
			log.error("Could not create the http protocol handler!", e);
		} catch (UnsupportedTypeException e) {
			log.error("Unsupported Type exception because of http body!", e);
		} catch (ConfigurationException e) {
			log.info("There was a problem configuring the http protocol handler, check what was going wrong.", e);
		}
	}
	
    public String getContent(String url) {
		Logger log = Logger.getLogger(this.getClass());

		Response response = null;
		if(null == protocol_) {
			String msg = "HttpWebUtils.getContent(): the protocol is not configured correctly!";
			log.error(msg);
			return null;
		}
		log.debug("Fetching data from URL: " + url);
		try {
			do {
				// handle redirects
				protocol_.setUrl(url);
				response = protocol_.connect();
				url = (String) response.getParameterValue("Location");
			} while (HttpURLConnection.HTTP_MOVED_PERM == response.getResponseCode());

			log.debug("HttpWebUtils.getContent(): Total time needed for get      : " + response.getTotalTime());
			log.debug("HttpWebUtils.getContent(): Time needed for data retrieval : " + response.getDataTime());
			log.debug(response.getContent());
		} catch (UnsupportedTypeException e) {
			log.error("HttpWebUtils.getContent(): Unsupported protocol type in url: " + url, e);
		} catch (NotReadyException e) {
			log.error("HttpWebUtils.getContent(): Connection not ready for url: " + url, e);
		} catch (NoSuchClassException e) {
			log.error("HttpWebUtils.getContent(): There is no handler class for the protocol in url: " + url, e);
		} catch (ToolException e) {
			log.error("HttpWebUtils.getContent(): No idea what a ToolException is?? url: " + url, e);
		} catch (FileNotFoundException  e) {
			log.info("HttpWebUtils.getContent(): The url: " + url + " could not be retrieved. Probably there is no data available for that date range or day.");
		} catch (IOException e) {
			log.error("HttpWebUtils.getContent(): IO problem for url: " + url, e);		    
		}

		if (null == response) {
			String msg = "HttpWebUtils.getContent(): There was no response from the url: " + url; 
			log.error(msg);
			return null;
		}

		String content = response.getContent();			
        return content;
    }
    
}
