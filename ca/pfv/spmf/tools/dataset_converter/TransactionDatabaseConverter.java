package ca.pfv.spmf.tools.dataset_converter;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* This class is for converting transaction databases from various formats
* to the SPMF format.

@see Formats
* @author Philippe Fournier-Viger
*/
public class TransactionDatabaseConverter {
	
	String input;  // the path of the input file
	String output; // the path of the file to be written to disk in SPMF format
	int lineCount =0; // the number of sequences in the input file

	/**
	 * This method converts a transaction database from a given format to the SPMF format.
	 * @param input  the path of the input file
	 * @param output the path of the file to be written to disk in SPMF format
	 * @param inputFileformat  the format of the input file
	 * @param lineCount  the number of lines from the input file that should be converted
	 * @throws IOException  an exception is thrown if there is an error reading/writing files Otherwise, null.
	 */
	public void convert(String input, String output, Formats inputFileformat, int lineCount) throws IOException {
		// we save the parameter in the class fields
		this.input = input;
		this.output = output;
		this.lineCount = lineCount;

		// we call the appropriate method for converting a database
		// according to the format of the input file
		if(inputFileformat.equals(Formats.CSV_INTEGER)){
			convertCSV();
		}else if(inputFileformat.equals(Formats.ARFF)){
			convertARFF(true, false);
			
		}else if(inputFileformat.equals(Formats.ARFF_WITH_MISSING_VALUES)){
			convertARFF(false, false);
		}

	}
	
	/**
	 * This method convert a transaction database in ARFF format to SPMF format and
	 * return a map of key = item id  value = corresponding attribute value. This 
	 * method is to be used by the GUI version of SPMF that need to keep the mapping
	 * between item IDs and attribute value in memory to avoid an extra database scan.
	 * @param inputFile the path of the file to be converted
	 * @param outputFile the path for saving the converted file
	 * @param lineCount the number of lines of the input file to be converted
	 * @return a map of entry (key : itemID, value: attribute-value) if the input format is ARFF.
	 * @throws IOException  if an error while reading/writing files
	 */
	public Map<Integer, String> convertARFFandReturnMap(String inputFile, String outputFile,
			int lineCount) throws IOException {
		// we save the parameter in the class fields
		this.input = inputFile;
		this.output = outputFile;
		this.lineCount = lineCount;
		return convertARFF(true, true);
	}
	
	/**
	 * This method convert a file from the ARFF format to the SPMF format.
	 * 
	 * @param returnMapItemIDValue
	 * @throws IOException  exception if error while reading/writing files.
	 * @return a map where an entry indicates for an item (key), the corresponding attribute value (value).
	 */
	private Map<Integer, String> convertARFF(boolean ignoreMissingValues, boolean returnMapItemIDValue) throws IOException {
		// This map will be used to store mapping from item id (key) to attribute value (value).
		// It is used only if returnMapItemIDValue is set to true.  This is used by the GUI of SPMF
		// which need to keep this information in memory to avoid an extra database scan after an algorithm
		// is applied.

		Map<Integer, String> mapItemsIDsToAttributeValues = null;
		if(returnMapItemIDValue){
			mapItemsIDsToAttributeValues = new HashMap<Integer, String>();
		}
		
		// object for writing the output file
		BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 

		BufferedReader myInput = null;
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin));
			
			int count = 0; // to count the number of data instance lines
			int attributeCount =0; // to count the number fo attributes
			
			// the last item ID used in the output file
			int lastItemAdded =0;
			
			
			// A list that stores a map for each attribute.
			// An entry in the map is :
			//   key  =  String (attribute value)
			//   value = Integer (item id)
			List<Map<String, Integer>> mapAttributeValuesItemsID = null;
			
			List<String> listAttributeNames = new ArrayList<String>();
			
			String thisLine; // variable to read a line
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is too short (e.g emptylines), skip it
				if(thisLine.length() <2){
					continue;
				}
				
				// if the line starts with a comment
				if(thisLine.startsWith("%")){
					continue;
				}
				
				// check if the line contains a comment later in the line
				int indexComment = thisLine.indexOf('%');
				// if yes, then remove it
				if(indexComment >=0){
					thisLine = thisLine.substring(0, indexComment);
				}
				
				// if the line is the relation name
				// (e.g. " @RELATION 'sunburn' ")
				if(thisLine.startsWith("@RELATION") || thisLine.startsWith("@relation")){
					String relationName = thisLine.split(" ")[1];
					// if the name is between quotes, we remove them
					if(relationName.contains("'")){
						relationName = relationName.split("'")[1];
					}
					if(returnMapItemIDValue == false){
						writer.write("@CONVERTED_FROM_ARFF");
						writer.newLine();
						writer.write("@RELATION_NAME=");
						writer.write(relationName + "=");
						writer.newLine();
					}
					continue;
				}
				
				// if the line is an attribute definition
				// For example:
				//     @ATTRIBUTE 'hair'   {blonde, brown, red}  
				//     @attribute   class {positive,negative}
				//     @attribute col_17 INTEGER
				//     @attribute col_18 {0,1,2,3,4,5}
				//     @ATTRIBUTE petalwidth   NUMERIC
				// @data
				// 
				if(thisLine.startsWith("@ATTRIBUTE") || thisLine.startsWith("@attribute") ){
					// increase the number of attributes
					attributeCount++;
					
					if(returnMapItemIDValue == false){
						writer.write("@ATTRIBUTE=");
					}
					
					// get the first position of the attribute name after the space before it
					int firstPositionOfAttributeName = thisLine.indexOf(' ') +1;
					// if the first character is a quote
					boolean useQuotes = false;
					if(thisLine.charAt(firstPositionOfAttributeName) == '\''){
						useQuotes = true;
						firstPositionOfAttributeName++;
					}
					// remove the part of the string before the attribute name
					thisLine = thisLine.substring(firstPositionOfAttributeName);
					
					// if there is extra spaces, we remove them just in case
					thisLine = thisLine.trim();
					
					// If quotes are use
					if(useQuotes){
						// get the position of the character just before the second quote
						int quotePosition = thisLine.indexOf('\'');
						// write attribute name
						String attributeName = thisLine.substring(0, quotePosition);
						if(returnMapItemIDValue == false){
							writer.write(attributeName + "=");
						}
						listAttributeNames.add(attributeName);
						// cut the string to remove the attribute name
						thisLine =  thisLine.substring(quotePosition+1);
					}else{
						// get the position of the character just before the space after the attribute name
						int spacePosition = thisLine.indexOf(' ');
						// write attribute name
						String attributeName = thisLine.substring(0, spacePosition);
						if(returnMapItemIDValue == false){
							writer.write(attributeName + "=");
						}
						listAttributeNames.add(attributeName);
						// cut the string to remove the attribute name
						thisLine =  thisLine.substring(spacePosition+1);
					}
						
					// remove spaces before or after what is remaining in this
					// line
					thisLine = thisLine.trim();
					
//					System.out.println(thisLine);
					
					// WRITE TYPE
					String type = thisLine;
					if(type.startsWith("{")){
						if(returnMapItemIDValue == false){
							writer.write("ENUMERATION=");
						}
						// Remove the brackets {}
						thisLine = thisLine.substring(1,thisLine.length()-1);
						
						
						// NEED TO READ THE ENUMERATION VALUES
						for (String token : thisLine.split(",")) {
							// remove spaces i they are some
							token = token.trim();
							// write the enumeration value
							if(returnMapItemIDValue == false){
								writer.write(token + "=");
							}
						}
					}else{
						// this is not an enumeration so we don't need
						// to write enumeration values.
						if(returnMapItemIDValue == false){
							writer.write(type + "=");
						}
					}
					if(returnMapItemIDValue == false){
						writer.newLine();
					}
					continue;
				}
				
				// if the line is the data separator
				if(thisLine.startsWith("@data") || thisLine.startsWith("@DATA")){
//					System.out.println("DATA");
					// initialize the map for storing attribute values
					//  by creating an empty hashmap for each attribute.

					mapAttributeValuesItemsID =
								new ArrayList<Map<String, Integer>>(attributeCount);
					for(int i=0; i< attributeCount; i++){
						mapAttributeValuesItemsID.add(new HashMap<String, Integer>());
					}
					continue;
				}
				
			
				// ===== NOW WE WILL PROCESS THE DATA INSTANCES IN THE FILE ====
				
				//Create a list to store the items of this transaction
				List<Integer> transaction = new ArrayList<Integer>();
				
				// Create a temporary stringbuffer for storing attributes
				// definition of attribute values that have not been seen before
				StringBuffer unseenAttributeValues = new StringBuffer();
				
				// IF SPARSE DATA
				// For example:
				//     {2 W, 4 "class B"}
				//   where each instance is a pair indicating the attribute number and the value.
				//  Ommitted values means the value 0
				//  Unknown values are represented by ?
				if(thisLine.startsWith("{")){
//					System.out.println(thisLine);
					// remove the brackets
					thisLine = thisLine.substring(1).trim();
					thisLine = thisLine.substring(0, thisLine.length()-1).trim();
//					System.out.println(thisLine);
					
					// we will use a HashSet<Integer> to remember which attribute
					// position are included and which one are not.
					// This is important because if an attribute is ommited, 
					// the value 0 should be used according to the ARFF specification.
					Set<Integer> positionProcessed = new HashSet<Integer>();
					

//					System.out.println(thisLine);
					
					// for each entry
					for(String entry : thisLine.split(",")){
						entry = entry.trim();
						// separate the entry into position + value
						int indexOfFirstSpace = entry.indexOf(' ');
						// extract the attribute number
						int i = Integer.parseInt(entry.substring(0, indexOfFirstSpace));
						// extract the attribute value
//						System.out.println(entry.substring(indexOfFirstSpace+1));
						String val = entry.substring(indexOfFirstSpace+1);
						
						positionProcessed.add(i);
						
						// if the user want to ignore missing values,
						// we skip the value
						if("?".equals(val) && ignoreMissingValues){
							continue;
						}
						
						// get the corresponding item id
						Map<String, Integer> mapValueToItemID = mapAttributeValuesItemsID.get(i);
						
						Integer itemID = mapValueToItemID.get(val);
						if(itemID == null){
							// if it is the first time that we see this attribute,
							// increase item ID.
							itemID = ++lastItemAdded; 
							// record the itemID that is given for this value
							mapValueToItemID.put(val, itemID);
							if(mapItemsIDsToAttributeValues != null){
								mapItemsIDsToAttributeValues.put(itemID, listAttributeNames.get(i) +
										"=" + val);
							}
							// add the unseen attribute value to the string for
							// unseen attribute values.
							unseenAttributeValues.append("@ITEM=" + itemID +"=" + listAttributeNames.get(i) +
									"=" + val + "\n");
						}
						
						// USE THE ITEM ID
						transaction.add(itemID);
					}
					
					// We will put the value 0 for all position that have not been
					// seen.
					for(int i=0; i< attributeCount; i++){
						// if the attriute i has not been processed yet
						if(positionProcessed.contains(i) == false){
							String val = "0";
							// if the user want to ignore missing values,
							// we skip the value
							if("?".equals(val) && ignoreMissingValues){
								continue;
							}
							
							// get the corresponding item id
							Map<String, Integer> mapValueToItemID = mapAttributeValuesItemsID.get(i);
							
							Integer itemID = mapValueToItemID.get(val);
							if(itemID == null){
								// if it is the first time that we see this attribute,
								// increase item ID.
								itemID = ++lastItemAdded; 
								// record the itemID that is given for this value
								mapValueToItemID.put(val, itemID);
								if(mapItemsIDsToAttributeValues != null){
									mapItemsIDsToAttributeValues.put(itemID, listAttributeNames.get(i) +
											"=" + val);
								}
								// add the unseen attribute value to the string for
								// unseen attribute values.
								unseenAttributeValues.append("@ITEM=" + itemID +"=" + listAttributeNames.get(i) +
										"=" + val + "\n");
							}
							
							// USE THE ITEM ID
							transaction.add(itemID);
						}
					}
					
					
				}else{
					// IF NOT SPARSE DATA
					// For example : 
					//    0, X, 0, Y, "class A"
					// Values are separated by "," and spaces
					// we split the line according to comma
					String[] split = thisLine.split(",");
					for(int i=0; i< attributeCount; i++){
						String val = split[i].trim();
						
						// if the user want to ignore missing values,
						// we skip the value
						if("?".equals(val) && ignoreMissingValues){
							continue;
						}
						
						// get the corresponding item id
						Map<String, Integer> mapValueToItemID = mapAttributeValuesItemsID.get(i);
						
						Integer itemID = mapValueToItemID.get(val);
						if(itemID == null){
							// if it is the first time that we see this attribute,
							// increase item ID.
							itemID = ++lastItemAdded; 
							// record the itemID that is given for this value
							mapValueToItemID.put(val, itemID);
							if(mapItemsIDsToAttributeValues != null){
								mapItemsIDsToAttributeValues.put(itemID, listAttributeNames.get(i) +
										"=" + val);
							}
							// add the unseen attribute value to the string for
							// unseen attribute values.
							unseenAttributeValues.append("@ITEM=" + itemID +"=" + listAttributeNames.get(i) +
									"=" + val + "\n");
						}
						
						// USE THE ITEM ID
						transaction.add(itemID);
					}
				}

//				// sort the transaction in lexical order
				Collections.sort(transaction);
				
//				if(returnMapItemIDValue == false){
					writer.write(unseenAttributeValues.toString());
	
					// for each item, we will output them
					for (int i=0; i<transaction.size(); i++) {
						if(i != transaction.size() -1){
							// if not the last item
							// write the item with an itemset separator
							writer.write(transaction.get(i) + " ");   
						}else{
							// if the last item
							// write the item
							writer.write(transaction.get(i) + "");   
						}
					}
					writer.newLine();
//				}
				
				count++; // increase the number of sequences
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
			}
			// close output file
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		return mapItemsIDsToAttributeValues;	
	}

	/**
	 * This method convert a file from the CSV format to the SPMF format
	 */
	private void convertCSV() throws IOException {
		BufferedReader myInput = null;
		try {
			// we create an object for writing the output file
			BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 
			
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin));
			
			int count = 0; // to count the number of line

			String thisLine; // variable to read a line
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// if not the first line, we create a new line
				if(count !=0){
					writer.newLine(); // create new line
				}
				
				// we split the line according to spaces
				String[] split = thisLine.split(",");
				// we use a set to store the values to avoid duplicates
				// because they are not allowed in a transaction
				Set<Integer> values = new HashSet<Integer>();
				for(int i=0; i< split.length; i++){
					values.add(Integer.parseInt(split[i]));
				}
				
				// sort the transaction in lexical order
				List<Integer> listValues = new ArrayList<Integer>(values);
				Collections.sort(listValues);
				
				// for each item, we will output them
				for (int i=0; i<listValues.size(); i++) {
					if(i != listValues.size() -1){
						// if not the last item
						// write the item with an itemset separator
						writer.write(listValues.get(i) + " ");   
					}else{
						// if the last item
						// write the item
						writer.write(listValues.get(i) + "");   
					}
				}
				
				count++; // increase the number of sequences
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
			}
			
			// close the output file
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}



}
