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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
* This class is for converting sequence databases from various formats
* to the SPMF format.
* 
* @see Formats
* @author Philippe Fournier-Viger
*/
public class SequenceDatabaseConverter {
	
	String input;  // the path of the input file
	String output; // the path of the file to be written to disk in SPMF format
	int lineCount =0; // the number of sequences in the input file
	BufferedWriter writer; // to write the output file

	/**
	 * This method converts a sequence database from a given format to the SPMF format.
	 * @param input  the path of the input file
	 * @param output the path of the file to be written to disk in SPMF format
	 * @param inputFileformat  the format of the input file
	 * @param lineCount  the number of lines from the input file that should be converted
	 * @throws IOException  an exception is thrown if there is an error reading/writing files
	 */
	public void convert(String input, String output, Formats inputFileformat, int lineCount) throws IOException {
		
		// we save the parameter in the class fields
		this.input = input;
		this.output = output;
		this.lineCount = lineCount;
		
		// we create an object fro writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// we call the appropriate method for converting a database
		// according to the format of the input file
		if(inputFileformat.equals(Formats.IBMGenerator)){
			convertIBMGenerator();
		}
		else if(inputFileformat.equals(Formats.Kosarak)){
			convertKosarak();
		}else if(inputFileformat.equals(Formats.CSV_INTEGER)){
			convertCSV();
		}else if(inputFileformat.equals(Formats.BMS)){
			convertBMS();
		}else if(inputFileformat.equals(Formats.Snake)){
			convertSnake();
		}
		
		// we close the output file
		writer.close();
	}

	/**
	 * This method convert a file from the SNAKE format to SPMF format
	 */
	private void convertSnake() {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;  
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin));
			
			int count =0;  // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line contains more than 11 elements
				// (we use this to filter smaller lines)
				if(thisLine.length() >= 11){   
					// for each integer on this line, we consider that it is an item
					for(int i=0; i< thisLine.length(); i++){
						// we subtract 65 to get the item number and
						// write the item to the file
						int character = thisLine.toCharArray()[i] - 65;
						// we write an itemset separator
						writer.write(character + " -1 ");   
					}
					// we write the end of the line
					writer.write("-2");
				}
				count++; // we increase the number of line that was read until now
				
				// if we have read enough lines, we stop.
				if(count == lineCount){
					break;
				}
				// start a new line
				writer.newLine();
			}
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method convert a file from the BMS format to SPMF format
	 */
	private void convertBMS() {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin));
			
			// In the BMS format, the sequencs of webpage of a user
			// is separated on several lines.
			// We use this variable to remember the id of the current user 
			// that we are reading.
			int lastId = 0; 
			
			int count = 0; // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {

				// we split the line according to spaces
				String[] split = thisLine.split(" ");
				
				// each line is a user id with a webpage
				int id = Integer.parseInt(split[0]); // id of the user on this line
				int val = Integer.parseInt(split[1]); // webpage viewed by this user
				
				// if the id of the current user is not the same as the previous line
				if(lastId != id){
					// and it is not the first line
					if(lastId!=0 ){ 
						count++; // increase sequence count
						
						// write the end of line
						writer.write("-2");
						writer.newLine();
					}
					lastId = id; // remember the current user id for this line  so that we know it for next line
				}
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
				// after each line we write an itemset separator "-1"
				writer.write(val + " -1 ");   // WRITE
			}
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method convert a file from the CSV format to SPMF format
	 */
	private void convertCSV() throws IOException {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin));
			
			int count = 0; // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				
				// we split the line according to spaces
				String[] split = thisLine.split(",");
				// for each value
				for (String value : split) {
					// we convert to integer and write the item
					Integer item = Integer.parseInt(value);
					writer.write(item + " -1 ");   // write an itemset separator
				}
				writer.write("-2");    // write end of line
				
				count++; // increase the number of sequences
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
				writer.newLine(); // create new line
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	/**
	 * This method convert a file from the KOSARAK format to SPMF format
	 */
	private void convertKosarak() throws IOException {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin));
			
			int count = 0; // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// we split the line according to spaces
				String[] split = thisLine.split(" ");
				// for each string on this line
				for (String value : split) {
					// we convert to integer and write it to file (it is an item)
					Integer item = Integer.parseInt(value);
					writer.write(item + " -1 ");   // write an itemset separator
				}
				writer.write("-2");  // write end of line
				
				count++;// increase the number of sequences
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
				writer.newLine(); // create new line
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	/**
	 * This method convert a file from the IBM GENERATOR format to SPMF format
	 */
	private void convertIBMGenerator() {
		DataInputStream myInput = null;
		try {
			// Objects to read the input file in binary format
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new DataInputStream(fin);
			
			// Variable to remember if we have written -1 after a group of items or not
			// (because in the binary format, at the end of a line there is no -1 before the -2
			//  but in spmf format there is one).
			boolean lastMinus1 = false; 
			
			int count = 0; // to count the number of line
			
			// we read the file integer by integer until the end of the file
			while (myInput.available() != 0) {
				// we read the first 32 bits and convert to big indian
				int value = INT_little_endian_TO_big_endian(myInput.readInt());
				// if it is "-1", the end of an itemset
				if (value == -1) { 
					// we write the same thing as output
					writer.write("-1 "); 
					lastMinus1 = true; // to remember that we have written -1
				} 
				// if it is "-2", the end of a sequence
				else if (value == -2) { 
					// check if the last "-1" was not written
					if (lastMinus1 == false) {
						writer.write("-1 "); // write "-1"
					}
					writer.write("-2 "); // write end of line
					
					count++;// increase the number of sequences
					
					// if we have read enough sequences, we stop.
					if(count == lineCount){
						break;
					}
					writer.newLine(); // create new line
				}
				// else it is an item
				else {
					// we write the item
					writer.write(value + " ");  
					
					lastMinus1 = false; // to remember that we need to write a -1
				}
			}
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  This method converts integer values from little indian to big endian
	 * @param i  an integer in little indian
	 * @return  the integer converted to big indian
	 */
	int INT_little_endian_TO_big_endian(int i) {
		return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8)
				+ ((i >> 24) & 0xff);
	}
}
