package ca.pfv.spmf.tools.dataset_generator;

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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
* This class is a random sequence database generator such that
* the user provides some parameters and this class generate a sequence database
* that is written to the disk.
* 
* @author Philippe Fournier-Viger
*/
public class SequenceDatabaseGenerator {

	// a random number generator
	private static Random random = new Random(System.currentTimeMillis());

	/**
	 * This method randomly generates a sequence database according to parameters provided.
	 * @param sequenceCount the number of sequences required
	 * @param maxDistinctItems the maximum number of distinct items
	 * @param itemCountByItemset the number of items by itemset
	 * @param itemsetCountBySequence the number of itemsets by sequence
	 * @param output the file path for writting the generated database
	 * @param withTimestamps if true, this database will contain timestamps, otherwise not
	 * @throws IOException 
	 */
	public  void generateDatabase(int sequenceCount, int maxDistinctItems, int itemCountByItemset,
			int itemsetCountBySequence, String output, boolean withTimestamps) throws IOException {
		
		// We create a BufferedWriter to write the database to disk
		BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 
		
		// For the number of sequences to be generated
		for (int i = 0; i < sequenceCount; i++) {
			// if it is not the first one, we write on a new line
			if(i != 0){
				writer.newLine();
			}
			// for the number of itemsets to be generated
			for (int j = 0; j < itemsetCountBySequence; j++) {
				// This hashset will be used to remember which items have
				// already been added to this itemset.
				HashSet<Integer> alreadyAdded = new HashSet<Integer>();
				
				// if the user asked for timestamps, we write the timestamp
				if(withTimestamps){
					writer.write("<" + j + "> ");
				}
				
				// create an arraylist to store items from the itemset that will be generated
				List<Integer> itemset = new ArrayList<Integer>();
				// for the number of items by itemset
				for (int k = 0; k < itemCountByItemset; k++) {
					// we generate the item randomly and write it to disk
					int item = random.nextInt(maxDistinctItems) + 1;
					// if we already added this item to this itemset
					// we choose another one
					while(alreadyAdded.contains(item)){
						item = random.nextInt(maxDistinctItems) + 1;
					}
					alreadyAdded.add(item);
					itemset.add(item);
				}
				// sort the itemset
				Collections.sort(itemset);
				// write the itemset
				for(Integer item : itemset){
					writer.write(item + " ");
				}
				
				// we write the itemset separator
				writer.write("-1 ");
			}
			// we write the end of line
			writer.write("-2 ");
		}
		writer.close(); // we close the file
	}
}