package ca.pfv.spmf.input.sequence_database_list_strings;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of a sequence database as list of strings. 
 * Each sequence should have a unique id.
 * See examples in /test/ directory for the format of input files.
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
		List<String> itemset = new ArrayList<String>();
		// for each token in this line
		for (String item : tokens) {
			// if the token start with "<", it indicates a timestamp. 
			// We just ignore it because algorithms that use this class
			// don't need it.
			if (item.codePointAt(0) == '<') { 
 				//nothing to do
			} 
			// if the token is -1, it means that we reached the end of an itemset.
			else if (item.equals("-1")) { 
				// We sort the itemset to make sure that it is sorted
				// (it is important for several algorithms)
				Collections.sort(itemset, new Comparator<String>() {
					public int compare(String arg0, String arg1) {
						return arg0.hashCode() - arg1.hashCode();
					}
				});
				// we add the current itemset to the sequence
				sequence.addItemset(itemset);
				// we create a new itemset for the next items that we will read (if any).
				itemset = new ArrayList<String>();
			} 
			// if the token is -2, it means that we reached the end of 
			// the sequence.
			else if (item.equals("-2")) { 
				// we thus add the sequence to the sequence database
				sequences.add(sequence);
			} 
			// else, it is an item
			else { 
				// we add the item to the current itemset.
				itemset.add(item);	
			}
		}
	}

	/**
	 * Add a sequence to this sequence database.
	 * @param sequence A sequence.
	 */
	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}

	/**
	 * Print this sequence database to System.out.
	 */
	public void printDatabase() {
		System.out.println("============  Database ==========");
		for (Sequence sequence : sequences) { // pour chaque objet
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	
	/**
	 * Print some statistics about this sequence database to
	 * System.out.
	 */
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		// Calculate the average size of sequences
		long size = 0;
		for(Sequence sequence : sequences){
			size += sequence.size();
		}
		double meansize = ((float)size) / ((float)sequences.size());
		System.out.println("Average sequence size : " + meansize);
	}

	/**
	 * Return a string representation of this sequence database.
	 */
	public String toString() {
		StringBuffer r = new StringBuffer();
		for (Sequence sequence : sequences) { // pour chaque objet
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
}
