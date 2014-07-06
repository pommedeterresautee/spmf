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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
* This class is for converting the result from an item (integer) representation
* back to a string representation by using the metadata provided in the original
* database.  See the class MainTestConvertTransactionDatabaseARFFtoSPMF in the same package for a detailed
* explanation of the correspondance between string and integer formats.
* 
* @author Philippe Fournier-Viger
*/
public class ResultConverter {

	
	/**
	 * This method converts a result file by converting item IDs to strings according to 
	 * a provided mapping.
	 * @param mapItemIDtoStringValue  a mapping between item ID (key) and attribute value (value).
	 * @param outputFile the path of an output file to be converted
	 * @param outputFileConverted the path of the result file to be written to disk 
	 * @throws IOException  an exception is thrown if there is an error reading/writing files
	 */
	public void convert(Map<Integer, String> mapItemIDtoStringValue,
			String outputFile, String outputFileConverted) throws IOException {
		// SECOND STEP:  READ THE RESULT FILE AND CONVERT IT BY USING THE MAP
			// AND AT THE SAME TIME WRITE THE OUTPUT FILE.
			FileInputStream finResult = new FileInputStream(new File(outputFile));
			BufferedReader myInputResult = new BufferedReader(new InputStreamReader(finResult));
			
//				// we create an object for writing the output file
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileConverted)); 
			
			String thisLine = null;
			boolean firstLine = true;
			// we read the file line by line until the end of the file
			while ((thisLine = myInputResult.readLine()) != null) {
				
				boolean noItemsLeft = false;
				
				// if the line starts with a comment
				if(thisLine.isEmpty() == false){
					if(firstLine){
						firstLine = false;
					}else{
						writer.newLine();
					}
					// split the line according to spaces into tokens
					String [] split = thisLine.split(" ");
					// for each token
					for(int i=0; i< split.length; i++){
						String token = split[i];
						// if the character "#" is met, it means that the rest of the line
						// does not contains items, we note it and we write the token directly
						// to the file as well as all the following tokens.
						if(token.startsWith("#") || noItemsLeft){
							noItemsLeft = true;
							// write the token as it is to the output file
							writer.write(token);
						}else{ 
							// check if the current token is a positive integer >0.
							// if so, we will convert to its string representation
							Integer itemID = isInteger(token);
							if(itemID == null){
								// write the token as it is to the output file
								writer.write(token);
							}else if (itemID >= 0){
								// convert the item to its string and write it to the output file
								writer.write(mapItemIDtoStringValue.get(itemID));
							}
							
						}
						
						// if not the last item, we write a space to the output file
						if(i != split.length-1){
							writer.write(" ");
						}
						
					}
				}
			}
			// we close the output file
			myInputResult.close();
			writer.close();
	}
	
	/**
	 * This method converts a result file by converting item IDs to strings according to 
	 * a provided mapping.
	 * @param inputDB an input file providing the mapping between item ID (key) and attribute value (value)
	 * as metadata.
	 * @param outputFile the path of an output file to be converted
	 * @param outputFileConverted the path of the result file to be written to disk 
	 * @throws IOException  an exception is thrown if there is an error reading/writing files
	 */
	public void convert(String inputDB, String outputFile, String outputFileConverted) throws IOException {

		// WE FIRST READ THE DATABASE FILE TO READ THE METADATA INDICATING
		// THE MAPPING BETWEEN ITEM TO ATTRIBUTE VALUE.
		// For example, a line: @ITEM=16=weight=red
		// indicate that the item 16 correspond to the string "weight=red"

		// Objects to read the file
		FileInputStream fin = new FileInputStream(new File(inputDB));
		BufferedReader myInputDB = new BufferedReader(new InputStreamReader(fin));

		// A map that
		// An entry in the map is :
		//   key  =  String (attribute value)
		//   value = Integer (item id)
		Map<Integer, String> mapItemIDtoStringValue = new HashMap<Integer, String>();
		
		String thisLine; // variable to read a line
		// we read the file line by line until the end of the file
		while ((thisLine = myInputDB.readLine()) != null) {
			// if the line starts with a comment
			if(thisLine.startsWith("@ITEM")){
				// remove "@ITEM="
				thisLine = thisLine.substring(6);
				// get the position of the first = in the remaining string
				int index = thisLine.indexOf("=");
				int itemID = Integer.parseInt(thisLine.substring(0, index));
				String stringValue = thisLine.substring(index+1);
//				System.out.println(itemID);
//				System.out.println(stringValue);
				mapItemIDtoStringValue.put(itemID, stringValue);
			}
		}// close the file
		myInputDB.close();
		
		convert(mapItemIDtoStringValue, outputFile, outputFileConverted);
	}

	/**
	 * Get the integer representation of a string or null if the string is not an integer.
	 * @param string a string
	 * @return an integer or null if the string is not an integer.
	 */
	Integer isInteger(String string) {
		Integer result = null;
	    try { 
	    	result = Integer.parseInt(string); 
	    } catch(NumberFormatException e) { 
	        return null; 
	    }
	    // only got here if we didn't return false
	    return result;
	}

}
