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
 * Created on 25.05.2005 by cs
 *
 */


package net.sf.ojts.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
	
	public static String getExecutablePathLocation() {
		String result = null;
		
		String classpath = System.getProperty("java.class.path");
		String[] elements = classpath.split(File.pathSeparator);
		if(elements.length < 1)
			return result;
		File file = new File(elements[0]).getParentFile();
		if(file.exists() && file.isDirectory())
			result = file.getAbsolutePath();
		
		if(null != result && File.separator.equals("\\")) 
			result = result.replaceAll("\\\\", "/");
		
		return result;
	}

	public static String readTextFile(String fullPathFilename) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
				
		char[] chars = new char[1024];
		int numRead = 0;
		while( (numRead = reader.read(chars)) > -1){
			sb.append(String.valueOf(chars).substring(0,numRead));	
		}

		reader.close();

		return sb.toString();
	}
	
	public static void writeTextFile(String contents, String fullPathFilename) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathFilename));
		writer.write(contents);
		writer.flush();
		writer.close();	
	}	
}
