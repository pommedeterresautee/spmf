package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.ItemSimple;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.ItemValued;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.MDPattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.MDPatternsDatabase;

/**
 * Implementation of a "Multi-Dimensional Sequence Database"
 * as used by the SeqDim algorithm (Pinto et al., 2001).
 * <br/><br/>
 * 
 * A MD-Sequences database contains a list of md-Sequences, where
 * each MD-Sequence is composed of an MD-Pattern and a Sequence.
 * 
 * @see MDSequence
 * @see MDSequences
 * @see AlgoSeqDim
* @author Philippe Fournier-Viger
 */
public class MDSequenceDatabase {
	/** List of md-sequences*/
	private final List<MDSequence> sequences = new ArrayList<MDSequence>();
	/** We also keep the sequences and patterns in some separate databases.
	// the sequence database*/
	private final SequenceDatabase sequenceDatabase = new SequenceDatabase();
	/** the mdpattern database*/
	private final MDPatternsDatabase patternDatabase = new MDPatternsDatabase();
	
	/** the set of item IDs in this database*/
	private  final Set<ItemSimple> itemIDs = new HashSet<ItemSimple>();
	
	/** the largest sequence ID in this database*/
	private int sequenceNumber =0;
	
	/**
	 * Get the number of distinct items in this database.
	 * @return an integer
	 */
	public int getItemCount(){
		return itemIDs.size();
	}

	/**
	 * Load a MD-Sequence database from a file
	 * @param path the path of the file
	 * @throws IOException exception if error reading the file
	 */
	public void loadFile(String path) throws IOException {
		// It read the file line by line
		// Each line is a md-sequence except lines starting with #.
		
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each line until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the MDsequence into tokens 
				// and process this MDsequence
				processMDSequence(thisLine.split(" "));	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			// close the file
			if(myInput != null){
				myInput.close();
			}
	    }
	}
	
	/**
	 * Process a line of the input file representing a MDSequence
	 * @param tokens a list of tokens from the line as String[]
	 */
	private void processMDSequence(String[] tokens) {	
		// (1) First, read the MDpattern part of the MDSequence
		
		// create the mdpattern
		MDPattern mdpattern = new MDPattern(sequenceNumber);
		int i= 0;
		// for each token until the end of the mdpattern (-3):
		for(; i< tokens.length; i++){
			// if -3, then it is the end of the mdpattern
			if(tokens[i].equals("-3")){ 
				break;
			// if "*" , it is a wildcard
			}else if(tokens[i].equals("*")){
				mdpattern.addInteger(MDPattern.WILDCARD);
			}else{ // otherwise, it is a dimension value
				mdpattern.addInteger(Integer.valueOf(tokens[i]));
			}
		}
		// (2) Now that the mdpattern has been read, the next step
		// is to read the sequence part of this mdsequence.
		
		// Create the sequence
		Sequence sequence = new Sequence(sequenceNumber);
		// create an itemset for the current itemset that will be read
		Itemset itemset = new Itemset();
		// for each token until the last one
		for(i++ ;i< tokens.length; i++){
			// if the token is a timestamp of an itemset
			if(tokens[i].codePointAt(0) == '<'){
				// set this value as the timestamp of the current itemset
				String value = tokens[i].substring(1, tokens[i].length()-1);
				itemset.setTimestamp(Long.parseLong(value));
			}else if(tokens[i].equals("-1")){
				// if -1, it means the end of the current itemset,
				// so we add it to the sequence and create
				// a new itemset
				sequence.addItemset(itemset);
				itemset = new Itemset();
			}else if(tokens[i].equals("-2")){
				// if -2, that means the end of the MDSequence
				// so we create the object with the
				// mdpattern and sequence objects.
				MDSequence mdsequence = new MDSequence(sequenceNumber, mdpattern, sequence);
				sequences.add(mdsequence);
				sequenceDatabase.addSequence(sequence);
				patternDatabase.addMDPattern(mdpattern);
				sequenceNumber++;
			}else{ 
				// Otherwise, it is an item.
				// An item can have a value between parenthesis.
				// We check if it has a parenthesis
				int indexLeftParenthesis = tokens[i].indexOf("(");
				int value =0;
				// if there is a left parenthesis
				if(indexLeftParenthesis != -1){
					// find the index of the right parenthesis
					int indexRightParenthesis = tokens[i].indexOf(")");
					// extract the value
					value = Integer.parseInt(tokens[i].substring(indexLeftParenthesis+1, indexRightParenthesis));
					// extract the item ID
					tokens[i] = tokens[i].substring(0, indexLeftParenthesis);
					// create the item with the value
					ItemValued item = new ItemValued(Integer.parseInt(tokens[i]), value);
					// add the item to the current itemset
					itemset.addItem(item);
				}else{
					// otherwise, it is just a simple item so
					// we extract the item ID and create a new item
					ItemSimple item = new ItemSimple(Integer.parseInt(tokens[i]));
					// we add the item to the current itemset
					itemset.addItem(item);
				}
			}
		}
	}
	
	/**
	 * Add an MDSequence to this MDSequence database.
	 * @param sequence an MDSequence
	 */
	public void addSequence(MDSequence sequence){
		// add it to the list of sequences
		sequences.add(sequence);
		// add the sequence and mdpattern parts to the respective
		// databases.
		sequenceDatabase.addSequence(sequence.getSequence());
		patternDatabase.addMDPattern(sequence.getMdpattern());
	}

	/**
	 * Print this database to system.out.
	 */
	public void printDatabase(){
		System.out.println(toString());
	}
	
	/**
	 * Get a String representation of this database.
	 * @return a string
	 */
	public String toString(){
		StringBuffer out = new StringBuffer("============  MD Sequence Database ==========\n");
		// for each mdsequence
		for(MDSequence sequence : sequences){ 
			// append the mdsequence
			out.append(sequence.toString() + "\n");
		}
		//return the string
		return out.toString();
	}
	
	/**
	 * Get the number of MDsequences.
	 * @return a integer value
	 */
	public int size(){
		return sequences.size();
	}

	/**
	 * Get the list of MDSequences stored in this database
	 * @return a List of MDSequence objects.
	 */
	public List<MDSequence> getSequences() {
		return sequences;
	}
	
	/**
	 * Get the i-th MDSequence.
	 * @param index the position i.
	 * @return the MDSequence
	 */
	public MDSequence get(int index) {
		return sequences.get(index);
	}

	/**
	 * Get the list of item IDs in this database.
	 * @return a set of Item objects.
	 */
	public Set<ItemSimple> getItemIDs() {
		return itemIDs;
	}

	/**
	 * Get the list of sequences contained in the MDSequences.
	 * @return a SequencDatabase
	 */
	public SequenceDatabase getSequenceDatabase() {
		return sequenceDatabase;
	}

	/**
	 * Get the list of MDPatterns contained in the MDSequences.
	 * @return a MDPatternsDatabase
	 */
	public MDPatternsDatabase getPatternDatabase() {
		return patternDatabase;
	}
}
