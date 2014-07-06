package ca.pfv.spmf.tools.resultConverter;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * This example shows how to convert a result file from an integer format  (item represented as integers
 * to a string format (item represented as strings).
 * This feature is used when an input file is converted from ARFF to the integer format.
 * After that, when an algorithms is applied, the result is in integer format 
 * and we want to convert it back to string format before presenting  the result to the user.
 * <br/><br/>
 * The conversion from integer to string is done by using the metadata about the equivalence 
 * between integer and strings that is stored in the converted input file.
 * 
* @author Philippe Fournier-Viger
 */
class MainTestResultConverter {
	
	public static void main(String [] arg) throws IOException{
		
		// the input file in integer format that contain metadata about
		// which integer correspond to which string
		String inputDB = fileToPath("example.txt");
		// the result file in integer format
		String inputResult = fileToPath("frequent_itemsets.txt");
		// the resulting result file in string format
		String outputFile = ".//output.txt";
		
		try{
			// create a converted
			ResultConverter converter = new ResultConverter();
			// do the conversion
			converter.convert(inputDB, inputResult, outputFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestResultConverter.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
