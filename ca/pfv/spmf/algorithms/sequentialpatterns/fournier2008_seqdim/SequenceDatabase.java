package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim;
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

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.kmeans_for_fournier08.Cluster;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;

/**
 * Implementation of a sequence database as used by the SeqDim and Fournier-Vier (2008) algorithms.
 * Each sequence need to hvae a unique ID.
 * See examples in the /test/ directory for the format of input files. that can be read by this class.
 * 
 * @see AlgoFournierViger08
 * @see AlgoSeqDim
 * @see Sequence
*  @author Philippe Fournier-Viger
 */
public class SequenceDatabase{

	/** List of sequences */
	private final List<Sequence> sequences = new ArrayList<Sequence>();
	
	/** for the Fournier08 algorithm, the cluster that was used to do the projection 
	  that results in this database. */
	private Cluster cluster = null;
	
	/**
	 * Load a sequence database from an input file
	 * @param path the input file path
	 * @throws IOException exception if error reading file
	 */
	public void loadFile(String path) throws IOException {
		// we will read line by line
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// For each line (sequence) until end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// process this line (sequence) splitted into tokens
				processSequence(thisLine.split(" "));		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			// Close the input file
			if(myInput != null){
				myInput.close();
			}
	    }
	}
	
	/**
	 * Process a line from the input file, splitted into tokens.
	 * @param tokens  a list of tokens (String).
	 */
	void processSequence(String[] tokens) {	//
		// create a new Sequence
		Sequence sequence = new Sequence(sequences.size());
		// Create an itemset that will be used to store items
		// from the first itemset and eventually the next itemsets.
		Itemset itemset = new Itemset();
		// for each tokens
		for(String integer:  tokens){
			// if this token is a timestamp
			if(integer.codePointAt(0) == '<'){ 
				// we extract the timestamp and set it as the timestamp
				// of the current itemset
				String value = integer.substring(1, integer.length()-1);
				itemset.setTimestamp(Long.parseLong(value));
			}else if(integer.equals("-1")){ 
				// If -1, this indicate the end of the current itemset,
				// so we add the itemset to the sequence, and
				// create a new itemset.
				sequence.addItemset(itemset);
				itemset = new Itemset();
			}else if(integer.equals("-2")){ 
				// If -2, it indicates the end of a sequence
				// If the last itemset is not empty, it means
				// that a -1 was missing.
				if(itemset.size() >0){
					// in this case we add the current itemset to the sequence
					// because it is not empty
					sequence.addItemset(itemset);
					itemset = new Itemset();
				}
				// finally, we add the sequence to the sequence database
				sequences.add(sequence);
			}else{ 
				// otherwise, check if it is 
				// an item with the format : id(value)  where
				// id is the item ID and value is a value associated with the item
				
				// we find the position of the left parenthesis
				int indexLeftParenthesis = integer.indexOf("(");
				// if there is a left parenthesis
				if(indexLeftParenthesis != -1){
					// we find the position of the left parenthesis
					int indexRightParenthesis = integer.indexOf(")");
					// we extract the value
					int value = Integer.parseInt(integer.substring(indexLeftParenthesis+1, indexRightParenthesis));
					// we extract the item ID
					integer = integer.substring(0, indexLeftParenthesis);
					// We create a new item with the item ID and the value
					ItemValued item = new ItemValued(Integer.parseInt(integer), value);
					// The item is then added to the current itemset
					itemset.addItem(item);
				}else{
					// Otherwise, it is just a regular item without value.
					
					//The item ID is extracted
					ItemSimple item = new ItemSimple(Integer.parseInt(integer));
					// If the item is not already in this itemset
					if(!itemset.getItems().contains(item)){
						// we add it to the itemset.
						itemset.addItem(item);
					}
				}
				
			}
		}
	}
	
	
	/**
	 * Add a sequence to the sequence database
	 * @param sequence the sequence
	 */
	public void addSequence(Sequence sequence){
		sequences.add(sequence);
	}
	
	/**
	 * Print this sequence database to System.out
	 */
	public void print(){
		System.out.println("============  Context ==========");
		// for each sequence
		for(Sequence sequence : sequences){ 
			// print the sequence
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	
	/**
	 * Get a string representation of this sequence
	 */
	public String toString(){
		// create a string buffer
		StringBuffer r = new StringBuffer();
		// for each sequence
		for(Sequence sequence : sequences){ 
			// append the sequence id
			r.append(sequence.getId());
			r.append(":  ");
			// append the itemsets of the sequence
			r.append(sequence.toString());
			r.append('\n');
		}
		// return the string
		return r.toString();
	}
	
	/**
	 * Get the number of sequences.
	 * @return an integer
	 */
	public int size(){
		return sequences.size();
	}

	/**
	 * Get the list of sequences.
	 * @return a List of sequences.
	 */
	public List<Sequence> getSequences() {
		return sequences;
	}

	/**
	 * Get the set of sequence IDs.
	 * @return a Set of Integer.
	 */
	public Set<Integer> getSequenceIDs() {
		Set<Integer> set = new HashSet<Integer>();
		for(Sequence sequence : getSequences()){
			set.add(sequence.getId());
		}
		return set;
	}

	/**
	 * Get the cluster that was used to create this projected sequence database.
	 * @return the Cluster or null if none
	 */
	Cluster getCluster() {
		return cluster;
	}

	/**
	 * Set the cluster that was used to create this projected sequence database.
	 * @param cluster the Cluster 
	 */
	void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
}
