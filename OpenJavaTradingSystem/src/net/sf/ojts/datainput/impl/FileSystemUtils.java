/*
 * Created on 21.11.2004 by cs
 *
 */
package net.sf.ojts.datainput.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
public class FileSystemUtils {
    
    public static File[] getFilesInDirectory(String dirname) throws FileNotFoundException {
        File[] result_files = new File[0];
        
        File dir = new File(dirname);
        validateDirectory(dir);
        List result = new ArrayList();

        File[] filesAndDirs = dir.listFiles();
        List filesDirs = Arrays.asList(filesAndDirs);
        Iterator filesIter = filesDirs.iterator();
        File file = null;
        while ( filesIter.hasNext() ) {
          file = (File)filesIter.next();
          if (file.isFile()) {
              result.add(file); //always add, even if directory
          }
        }
        Collections.sort(result);  
        result_files = new File[result.size()];
        for(int i = 0; i < result_files.length; i++) {
            result_files[i] = (File)result.get(i);
        }
        
        return result_files;
    }
    
    public static void validateDirectory (File aDirectory) throws FileNotFoundException {
        if (aDirectory == null) {
          throw new FileNotFoundException("Directory should not be null.");
        }
        if (!aDirectory.exists()) {
          throw new FileNotFoundException("Directory does not exist: " + aDirectory);
        }
        if (!aDirectory.isDirectory()) {
          throw new FileNotFoundException("Is not a directory: " + aDirectory);
        }
        if (!aDirectory.canRead()) {
          throw new FileNotFoundException("Directory cannot be read: " + aDirectory);
        }
    }
    
    public static String readFileContent(String filename) throws FileNotFoundException, IOException {
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String content = "";
        String newLine = null;
        while ( (newLine = br.readLine()) != null )
        {
          content += newLine + "\n";
        }
        br.close();
        return content;        
    }
}
