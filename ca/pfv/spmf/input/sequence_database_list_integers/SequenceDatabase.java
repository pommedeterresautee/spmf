package ca.pfv.spmf.input.sequence_database_list_integers;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * Implementation of a sequence database, where each sequence is implemented
 * as a list of integers and should have a unique id.
*
* @see Sequence
 * @author Philipe-Fournier-Viger
 */
public class SequenceDatabase {

	// variable that contains the sequences of this database
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
			int i=0;
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
	
	/**
	 * Method to process a line from the input file
	 * @param tokens A list of tokens from the line (which were separated by spaces in the original file).
	 */
	void addSequence(String[] tokens) { 
		// create a new Sequence to store the sequence
		Sequence sequence = new Sequence(sequences.size());
		// create a list of strings for the first itemset.
		
		List<Integer> itemset = new ArrayList<Integer>();
		// for each token in this line
		for (String token : tokens) {
			// if the token start with "<", it indicates a timestamp. 
			// We just ignore it because algorithms that use this class
			// don't need it.
			if (token.codePointAt(0) == '<') { 
				// we ignore
			} 
			// if the token is -1, it means that we reached the end of an itemset.
			else if (token.equals("-1")) { 
				// add the current itemset to the sequence
				sequence.addItemset(itemset);
				// create a new itemset
				itemset = new ArrayList<Integer>();
			} 
			// if the token is -2, it means that we reached the end of 
			// the sequence.
			else if (token.equals("-2")) { 
				// we add it to the list of sequences
				sequences.add(sequence);
			} else { 
				// otherwise it is an item.
				// we parse it as an integer and add it to 
				// the current itemset.
				itemset.add(Integer.parseInt(token));
			}
		}
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
		System.out.println("============  SEQUENCE DATABASE ==========");
		for (Sequence sequence : sequences) { // pour chaque objet
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	
	/**
	 * Print statistics about this database.
	 */
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		
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
		for (Sequence sequence : sequences) { 
			r.append(sequence.getId());
			r.append(":  ");
			r.append(sequence.toString());
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

	/**
	 * Get the list of sequence IDs for this database.
	 * @return A set containing the sequence IDs of sequence in this
	 * database.
	 */
	public Set<Integer> getSequenceIDs() {
		// create a set
		Set<Integer> set = new HashSet<Integer>();
		// for each sequence
		for (Sequence sequence : getSequences()) {
			set.add(sequence.getId()); // add the id to the set.
		}
		return set; // return the set.
	}


//	public void loadFileKosarakFormat(String filepath, int nblinetoread)
//			throws IOException {
//		String thisLine;
//		BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(filepath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			int i = 0;
//			while ((thisLine = myInput.readLine()) != null) {
//				// ajoute une séquence
//				String[] split = thisLine.split(" ");
//				i++;
//				if (nblinetoread == i) {
//					break;
//				}
//				Sequence sequence = new Sequence(sequences.size());
//				for (String value : split) {
//					List<Integer> itemset = new ArrayList<Integer>();
//					itemset.add(Integer.parseInt(value));
//					sequence.addItemset(itemset);
//				}
//				sequences.add(sequence);
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
//				// ajoute une séquence
//				String[] split = thisLine.split(" ");
//				i++;
////				if (nblinetoread == i) {
////					break;
////				}
//				if(split.length < 25){
//					continue;
//				}
//				Sequence sequence = new Sequence(sequences.size());
//				int count=0;
//				List<Integer> itemset = new ArrayList<Integer>();
//				for (String value : split) {
//					itemset.add(Integer.parseInt(value));
//					count++;
//					if(count == 3){
//						sequence.addItemset(itemset);
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
//				// ajoute une séquence
//				String[] split = thisLine.split(" ");
//				int id = Integer.parseInt(split[0]);
//				int val = Integer.parseInt(split[1]);
//				
//				if(lastId != id){
//					if(lastId!=0 ){ //&& sequence.size() >=2
//						sequences.add(sequence);
//						realID++;
//					}
//					sequence = new Sequence(realID);
//					lastId = id;
//				}
//				List<Integer> itemset = new ArrayList<Integer>();
//				itemset.add(val);
//				sequence.addItemset(itemset);
//			}
//			myInput.close();
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
//			int seqid =0;
//			while ((thisLine = myInput.readLine()) != null) {
//				if(thisLine.length() >= 1 && thisLine.charAt(0) != '#'){
//					String []tokens = thisLine.split(" ");
//					String currentUtterance = tokens[0];
//					if(!currentUtterance.equals(oldUtterance)){
//						if(sequence != null){
//							sequences.add(sequence);
//						}
//						sequence = new Sequence(seqid++);
//						oldUtterance = currentUtterance;
//					}
//					for(int j=1; j< tokens.length; j++){
//						int character = Integer.parseInt(tokens[j]);
//						if(character == -11 || character == -12){
//							continue;
//						}
//						List<Integer> itemset = new ArrayList<Integer>();
//						itemset.add(character);
//						sequence.addItemset(itemset);
//					}
//				}
//			}
//			sequences.add(sequence);
//			System.out.println(sequence.toString());
//			myInput.close();
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
//				// ajoute une séquence
//				String[] split = thisLine.split(" ");
//				int id = Integer.parseInt(split[0]);
//				int val = Integer.parseInt(split[1]);
//				
//				if(lastId != id){
//					if(lastId!=0  && sequence.size() >=5){
//						sequences.add(sequence);
//						realID++;
//					}
//					sequence = new Sequence(realID);
//					lastId = id;
//				}
//				List<Integer> itemset = new ArrayList<Integer>();
//				itemset.add(val);
//				sequence.addItemset(itemset);
//			}
//			myInput.close();
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
//			int realID = 0;
//			while ((thisLine = myInput.readLine()) != null) {
//				if(thisLine.length() >= 50){
//					Sequence sequence = new Sequence(realID++);
//					for(int i=0; i< thisLine.length(); i++){
//						List<Integer> itemset = new ArrayList<Integer>();
//						int character = thisLine.toCharArray()[i] - 65;
//						System.out.println(thisLine.toCharArray()[i] + " " + character);
//						itemset.add(character);
//						sequence.addItemset(itemset);
//					}
//					sequences.add(sequence);
//				}
//			}
//			myInput.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
//	}
//	
//	
//	public void loadFileMSNBCFormat(String filepath, int nblinetoread)
//	throws IOException {
//	String thisLine;
//	BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(filepath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			int i = 0;
//			while ((thisLine = myInput.readLine()) != null) {
//				// ajoute une séquence
//				String[] split = thisLine.split(" ");
//				i++;
//				if (nblinetoread == i) {
//					break;
//				}
//				Sequence sequence = new Sequence(sequences.size());
//				for (String value : split) {
//					Itemset itemset = new Itemset();
//					itemset.addItem(Integer.parseInt(value));
//					sequence.addItemset(itemset);
//				}
//				if(sequence.size() > 4){
//					sequences.add(sequence);
//				}
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//				if (myInput != null) {
//					myInput.close();
//				}
//			}
//		}

}
