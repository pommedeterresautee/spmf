package ca.pfv.spmf.input.sequence_database_array_integers;
/* Copyright (c) 2008-2013 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a sequence database, where each sequence is implemented
 * as an array of integers and should have a unique id.
 *
 * @see Sequence
 * @author Philipe-Fournier-Viger
 */
public class SequenceDatabase {

	/** smallest item in this database*/
	public int minItem = Integer.MAX_VALUE; 
	/** largest item in this database */
	public int maxItem = 0; 
	/** the number of sequences in this database */
	public int tidsCount =0; 

	/** variable that contains the sequences of this database */
	private final List<Sequence> sequences = new ArrayList<Sequence>();

	/**
	 * Method to load a sequence database from a text file in SPMF format.
	 * @param path  the input file path.
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String path) throws IOException {
		String thisLine; // variable to read each line.
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is not a comment, is not empty or is not other
				// kind of metadata
				if (thisLine.isEmpty() == false &&
						thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {
					// split this line according to spaces and process the line
					addSequence(thisLine.split(" "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
	
//	

	/**
	 * Method to process a line from the input file
	 * @param tokens A list of tokens from the line (which were separated by spaces in the original file).
	 */
	public void addSequence(String[] tokens) { 
		// create a new Sequence to store the sequence
		Sequence sequence = new Sequence();
		// create a list of strings for the first itemset.
		List<Integer> itemset = new ArrayList<Integer>();
		
		// for each token in this line
		for (String token : tokens) {
			// if the token start with "<", it indicates a timestamp. 
			// We just ignore it because algorithms that use this class
			// don't need it.
			if (token.codePointAt(0) == '<') { 
				// we just ignore
			} 
			// if the token is -1, it means that we reached the end of an itemset.
			else if (token.equals("-1")) { 
				// add the current itemset to the sequence
				sequence.addItemset(itemset.toArray());
				// create a new itemset
				itemset = new ArrayList<Integer>();
			} 
			// if the token is -2, it means that we reached the end of 
						// the sequence.
			else if (token.equals("-2")) { 
				// we add it to the list of sequences
				sequences.add(sequence);
			} else { 
				// Otherwise it is an item.
				// We parse it as an integer.
				Integer item = Integer.parseInt(token);
				// we update the maximum item for statistics
				if(item >= maxItem){
					maxItem = item;
				}
				// we update the minimum item for statistics
				if(item < minItem){
					minItem = item;
				}
				// we add the item to the current itemset
				itemset.add(item);
			}
		}
//		tidsCount++;
	}

	/**
	 * Method to add a sequence to this sequence database
	 * @param sequence A sequence of type "Sequence".
	 */
	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}

	/**
	 * Print this sequence database to System.out.
	 */
	public void print() {
		System.out.println("============  CONTEXTE ==========");
		for (int i=0 ; i < sequences.size(); i++) { // pour chaque objet
			System.out.print(i + ":  ");
			sequences.get(i).print();
			System.out.println("");
		}
	}
	
	/**
	 * Print statistics about this database.
	 */
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		System.out.println("Min item:" + minItem);
		System.out.println("Max item:" + maxItem);
		
		// Calculate the average size of sequences in this database
		long size = 0;
		for(Sequence sequence : sequences){
			size += sequence.size();
		}
		double meansize = ((float)size) / ((float)sequences.size());
		System.out.println("mean size" + meansize);
	}
	
	/**
	 * Return a string representation of this sequence database.
	 */
	public String toString() {
		StringBuffer r = new StringBuffer();
		// for each sequence
		for (int i=0 ; i < sequences.size(); i++) { 
			r.append(i);
			r.append(":  ");
			r.append(sequences.get(i).toString());
			r.append('\n');
		}
		return r.toString();
	}
	
	/**
	 * Get the sequence count in this database.
	 * @return the sequence count.
	 */
	public int size() {
		return sequences.size();
	}
	
	/**
	 * Get the sequences from this sequence database.
	 * @return A list of sequences (Sequence).
	 */
	public List<Sequence> getSequences() {
		return sequences;
	}
//	
//	public void printDatabaseStats() {
//		System.out.println("============  STATS ==========");
//		System.out.println("Number of sequences : " + sequences.size());
//		System.out.println("Min item:" + minItem);
//		System.out.println("Max item:" + maxItem);
//		// average size of sequence
//		long size = 0;
//		long sizeItems = 0;
//		ArrayList<Integer> listItems = new ArrayList<Integer>();
//		for(Sequence sequence : sequences){
//			int itemCount = 0;
//			for(Integer[] array : sequence.getItemsets()){
//				itemCount += array.length;
//			}
//			sizeItems += itemCount;
//			listItems.add(itemCount);
//			size += sequence.size();
//		}
//		double meansizeItems = ((float)sizeItems) / ((float)sequences.size());
//		// standard deviation
//		double std =0;
//		for(Integer elementList : listItems){
//			std += Math.abs((meansizeItems - elementList) / ((float)sequences.size()));
//		}
//		std = Math.sqrt(std);
//		
//		System.out.println("mean item count" + meansizeItems + " std : " + std);
////		System.out.println("mean itemset count" + meansize);
//	}



	
//	public void loadFile(String path, int maxlineCount) throws IOException {
//		String thisLine;
//		BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(path));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			int i=0;
//			while ((thisLine = myInput.readLine()) != null) {
//				// si la ligne n'est pas un commentaire
//				if (thisLine.charAt(0) != '#') {
//					// ajoute une s�quence
//					addSequence(thisLine.split(" "));
//					i++;
//					if(i == maxlineCount){
//						break;
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (myInput != null) {
//				myInput.close();
//			}
//		}
//	}
//	// ---------------------- Pour le fomat g�n�r� par seq_data_generator
//	public void loadFileBinaryFormat(String path, int maxcount) {
//		// TODO Auto-generated method stub
//		DataInputStream myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(path));
//			myInput = new DataInputStream(fin);
//
//			Sequence sequence = new Sequence();
//			List<Integer> itemset = new ArrayList<Integer>();
//			while (true) {
//				int value = INT_little_endian_TO_big_endian(myInput.readInt());
//
//				// System.out.println(value);
//				if (value == -1) {
//					sequence.addItemset(itemset.toArray());
//					itemset = new ArrayList<Integer>();
//				} else if (value == -2) {
//					sequences.add(sequence);
//					if (sequences.size() == maxcount) {
//						break;
//					}
//					sequence = new Sequence();
//				} else {
//					itemset.add(value);
//				}
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	// 4-byte number this function was taken from the internet (by Anghel
//	// Leonard)
//	int INT_little_endian_TO_big_endian(int i) {
//		return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8)
//				+ ((i >> 24) & 0xff);
//	}
//
//	public void loadFileKosarakFormat(String filepath, int nblinetoread)
//	throws IOException {
//String thisLine;
//BufferedReader myInput = null;
//try {
//	FileInputStream fin = new FileInputStream(new File(filepath));
//	myInput = new BufferedReader(new InputStreamReader(fin));
//	int i = 0;
//	while ((thisLine = myInput.readLine()) != null) {
//		// ajoute une s�quence
//		String[] split = thisLine.split(" ");
//		i++;
//		if (nblinetoread == i) {
//			break;
//		}
//		Sequence sequence = new Sequence();
//		for (String value : split) {
//			List<Integer> itemset = new ArrayList<Integer>();
//			Integer item = Integer.parseInt(value);
//			if(item >= maxItem){
//				maxItem = item;
//			}
//			if(item < minItem){
//				minItem = item;
//			}
//			itemset.add(item);
//			sequence.addItemset(itemset.toArray());
//		}
//		sequences.add(sequence);
//		
//	}
//} catch (Exception e) {
//	e.printStackTrace();
//} finally {
//	if (myInput != null) {
//		myInput.close();
//	}
//}
//}
//	
//	
//	public void loadFileKosarakFormatV2(String filepath, int nblinetoread)
//	throws IOException {
//		String thisLine;
//		BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(filepath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			int i = 0;
//			while ((thisLine = myInput.readLine()) != null) {
//				// ajoute une s�quence
//				String[] split = thisLine.split(" ");
//				i++;
////				if (nblinetoread == i) {
////					break;
////				}
//				if(split.length < 25){
//					continue;
//				}
//				Sequence sequence = new Sequence();
//				int count=0;
//				List<Integer> itemset = new ArrayList<Integer>();
//				for (String value : split) {
//					Integer item = Integer.parseInt(value);
//					itemset.add(item);
//					if(item >= maxItem){
//						maxItem = item;
//					}
//					if(item < minItem){
//						minItem = item;
//					}
//					count++;
//					if(count == 3){
//						sequence.addItemset(itemset.toArray());
//						itemset = new ArrayList<Integer>();
//						count =0;
//					}
//				}
//				if(sequence.size() >10 ){
//					sequences.add(sequence);
//				}
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (myInput != null) {
//				myInput.close();
//			}
//		}
//	}
//
//	public void loadFileWebViewFOrmat(String filepath, int nbLine) {
//		String thisLine;
//		BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(filepath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			int realID = 0;
//			int lastId = 0;
//			Sequence sequence = null;
//			while ((thisLine = myInput.readLine()) != null) {
//				// ajoute une s�quence
//				String[] split = thisLine.split(" ");
//				int id = Integer.parseInt(split[0]);
//				int val = Integer.parseInt(split[1]);
//				
//				if(lastId != id){
//					if(lastId!=0 ){ //&& sequence.size() >=2
//						sequences.add(sequence);
//						realID++;
//					}
//					sequence = new Sequence();
//					lastId = id;
//				}
//				List<Integer> itemset = new ArrayList<Integer>();
//				itemset.add(val);
//				if(val >= maxItem){
//					maxItem = val;
//				}
//				if(val < minItem){
//					minItem = val;
//				}
//				sequence.addItemset(itemset.toArray());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
//	}
//	
//	public void loadFileWebViewFOrmatV2(String filepath, int nbLine) {
//		String thisLine;
//		BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(filepath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			int realID = 0;
//			int lastId = 0;
//			Sequence sequence = null;
//			while ((thisLine = myInput.readLine()) != null) {
//				// ajoute une s�quence
//				String[] split = thisLine.split(" ");
//				int id = Integer.parseInt(split[0]);
//				int val = Integer.parseInt(split[1]);
//				
//				if(lastId != id){
//					if(lastId!=0  && sequence.size() >=5){
//						sequences.add(sequence);
//						realID++;
//					}
//					sequence = new Sequence();
//					lastId = id;
//				}
//				List<Integer> itemset = new ArrayList<Integer>();
//				itemset.add(val);
//				if(val >= maxItem){
//					maxItem = val;
//				}
//				if(val < minItem){
//					minItem = val;
//				}
//				sequence.addItemset(itemset.toArray());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
//	}
//	
//	public void loadSnakeDataset(String filepath, int nbLine) {
//		String thisLine;
//		BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(filepath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			while ((thisLine = myInput.readLine()) != null) {
//				if(thisLine.length() >= 50){
//					Sequence sequence = new Sequence();
//					for(int i=0; i< thisLine.length(); i++){
//						List<Integer> itemset = new ArrayList<Integer>();
//						int character = thisLine.toCharArray()[i] - 65;
//						System.out.println(thisLine.toCharArray()[i] + " " + character);
//						itemset.add(character);
//						if(character >= maxItem){
//							maxItem = character;
//						}
//						if(character < minItem){
//							minItem = character;
//						}
//						sequence.addItemset(itemset.toArray());
//					}
//					sequences.add(sequence);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
//	}
//
//	public void loadFileSignLanguage(String fileToPath, int i) {
//		String thisLine;
//		BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(fileToPath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			String oldUtterance = "-1";
//			Sequence sequence = null;
//			while ((thisLine = myInput.readLine()) != null) {
//				if(thisLine.length() >= 1 && thisLine.charAt(0) != '#'){
//					String []tokens = thisLine.split(" ");
//					String currentUtterance = tokens[0];
//					if(!currentUtterance.equals(oldUtterance)){
//						if(sequence != null){
//							sequences.add(sequence);
//						}
//						sequence = new Sequence();
//						oldUtterance = currentUtterance;
//					}
//					for(int j=1; j< tokens.length; j++){
//						int character = Integer.parseInt(tokens[j]);
//						if(character == -11 || character == -12){
//							continue;
//						}
//						if(character >= maxItem){
//							maxItem = character;
//						}
//						if(character < minItem){
//							minItem = character;
//						}
//						sequence.addItemset(new Object[]{character});
//					}
//				}
//			}
//			sequences.add(sequence);
//			System.out.println(sequence.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
//	}
	
//	Sequence sequence = new Sequence();
//	for(int i=0; i< thisLine.length(); i++){
//		List<Integer> itemset = new ArrayList<Integer>();
//		int character = thisLine.toCharArray()[i] - 65;
//		System.out.println(thisLine.toCharArray()[i] + " " + character);
//		itemset.add(character);
//		if(character >= maxItem){
//			maxItem = character;
//		}
//		if(character < minItem){
//			minItem = character;
//		}
//		sequence.addItemset(itemset.toArray());
//	}
//	sequences.add(sequence);
}
