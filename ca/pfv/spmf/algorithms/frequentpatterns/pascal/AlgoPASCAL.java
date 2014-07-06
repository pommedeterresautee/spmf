package ca.pfv.spmf.algorithms.frequentpatterns.pascal;

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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_HT.ItemsetHashTree;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the PASCAL algorithm. It is an Apriori-based
 * algorithm that use information about generators to skip some database scans. <br/>
 * <br/>
 * 
 * The PASCAL algorithm is described in : <br/>
 * <br/>
 * 
 * Yves Bastide, Rafik Taouil, Nicolas Pasquier et al. (2002) Pascal : un
 * algorithme d'extraction des motifs fr�quents, 65-95. In Techniques et Science
 * Informatiques 21 (1). <br/>
 * <br/>
 * 
 * The PASCAL algorithm finds all the frequents itemsets and their support in a
 * transaction database. It also identify itemsets that are generators. <br/>
 * <br/>
 * 
 * 
 * @see ItemsetPascal
 * @see AbstractOrderedItemsetsAdapter
 * @see ItemsetHashTree
 * @author Philippe Fournier-Viger
 */
public class AlgoPASCAL {

	// the maximul level reached by Apriori
	protected int k;

	// For statistics
	protected int totalCandidateCount = 0; // total number of candidates
											// generated
	protected long startTimestamp; // start time
	protected long endTimestamp; // end time
	private int itemsetCount; // number of itemsets found

	// the relative minimum support used to find itemsets
	private int minsupRelative;

	// an in-memory representation of the transaction database
	private List<int[]> database = null;

	// write to file
	BufferedWriter writer = null;

	/**
	 * Default constructor
	 */
	public AlgoPASCAL() {

	}

	/**
	 * Run the Apriori-HT algorithm
	 * 
	 * @param minsup
	 *            the minimum support threshold
	 * @param input
	 *            path to the input file
	 * @param output
	 *            path to save the result to an output file
	 * @throws IOException
	 *             if an error while reading/writing files
	 */
	public void runAlgorithm(double minsup, String input, String output) throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();

		// prepare object for writing the file
		writer = new BufferedWriter(new FileWriter(output));

		// reset statistics
		itemsetCount = 0;
		totalCandidateCount = 0;
		MemoryLogger.getInstance().reset();
		int transactionCount = 0;

		// structure to count the support of each item
		// Key: item Value: support count
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();

		// the database in memory (intially empty)
		database = new ArrayList<int[]>();

		// scan the database to load it into memory and count the support of
		// each single item at the same time
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) of the input file until the end of file
		while (((line = reader.readLine()) != null)) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() == true || line.charAt(0) == '#'
					|| line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}

			// split the line into items
			String[] lineSplited = line.split(" ");

			// create an array to store the items
			int transaction[] = new int[lineSplited.length];

			// for each item in the current transaction
			for (int i = 0; i < lineSplited.length; i++) {
				// convert to integer
				Integer item = Integer.parseInt(lineSplited[i]);
				// add the item to the transaction
				transaction[i] = item;

				// increase the support count of the item
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}
			// add transaction to the database
			database.add(transaction);
			// increase the transaction count
			transactionCount++;
		}
		// close the input file
		reader.close();

		// convert absolute minimum support to a relative minimum support
		// by multiplying by the database size
		this.minsupRelative = (int) Math.ceil(minsup * transactionCount);

		// System.out.println("database size = " +database.size() +
		// "  minsuprel = " + minsupRelative);

		// Set variable k=1 because we start with itemsets of size 1
		k = 1;

		// Create the list of all frequent items of size 1
		List<ItemsetPascal> frequent1 = new ArrayList<ItemsetPascal>();
		// For each item
		for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			// if its support is higher than the support
			int itemsetSupport = entry.getValue();
			if (itemsetSupport >= minsupRelative) {
				// keep the item into memory for generating itemsets of size 2

				// ------ CODE SPECIFIC TO PASCAL --------
				// an itemset of size 1 is a generator if it has not the support
				// equal to the transaction count.
				ItemsetPascal itemset = new ItemsetPascal(
						new int[] { entry.getKey() });
				itemset.isGenerator = (itemsetSupport != transactionCount);
				itemset.pred_sup = transactionCount;
				// ------ END OF CODE SPECIFIC TO PASCAL --------

				frequent1.add(itemset);
				// and also save it to the output file
				saveItemsetToFile(itemset);
			}
		}
		mapItemCount = null; // we don't need it anymore

		// Sort the list of frequent items of size 1 by lexical order because
		// Apriori need itemset sorted by a total order.
		Collections.sort(frequent1, new Comparator<ItemsetPascal>() {
			public int compare(ItemsetPascal o1, ItemsetPascal o2) {
				return o1.get(0) - o2.get(0);
			}
		});

		// if no frequent item, we stop there!
		if (frequent1.size() == 0) {
			return;
		}

		// increase the number of candidates
		totalCandidateCount += frequent1.size();

		// Now we will perform a loop to find all frequent itemsets of size > 1
		// starting from size k = 2.
		// The loop will stop when no candidates can be generated.
		List<ItemsetPascal> level = null;
		k = 2;
		do {
			// we check the memory usage
			MemoryLogger.getInstance().checkMemory();

			// Generate candidates of size K
			List<ItemsetPascal> candidatesK;

			// if we are at level k=2, we use an optimization to generate
			// candidates
			if (k == 2) {
				candidatesK = generateCandidate2(frequent1);
			} else {
				// otherwise we use the regular way to generate candidates
				candidatesK = generateCandidateSizeK(level);
			}

			// we add the number of candidates generated to the total
			totalCandidateCount += candidatesK.size();

			
			// We scan the database one time to calculate the support
			// of each candidates and keep those with higher suport.
			for (ItemsetPascal candidate : candidatesK) {
				// CODE SPECIFIC TO PASCAL
				// PRUNING STRATEGY
				// for each itemset that is a generator, if the support
				// is the same as the minimum of its subsets, then
				// it is not a generator and we want to remember that
				if(candidate.isGenerator == false) {
					continue;
				}
				// END CODE SPECIFIC TO PASCAL
				
				// For each transaction:
				loop:	for (int[] transaction : database) {
						// NEW OPTIMIZATION 2013: Skip transactions shorter than k!
						if (transaction.length < k) {
							// System.out.println("test");
							continue;
						}
						// END OF NEW OPTIMIZATION

						
							// a variable that will be use to check if
							// all items of candidate are in this transaction
							int pos = 0;
							// for each item in this transaction
							for (int item : transaction) {
								
								// if the item correspond to the current item of
								// candidate
								if (item == candidate.itemset[pos]) {
									// we will try to find the next item of candidate
									// next
									pos++;
									// if we found all items of candidate in this
									// transaction
									if (pos == candidate.itemset.length) {
										// we increase the support of this candidate
										candidate.support++;
										continue loop;
									}
									// Because of lexical order, we don't need to
									// continue scanning the transaction if the current
									// item
									// is larger than the one that we search in
									// candidate.
								} else if (item > candidate.itemset[pos]) {
									continue loop;
								}

							}
						}
				
			}

	

			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			level = new ArrayList<ItemsetPascal>();
			for (ItemsetPascal candidate : candidatesK) {
				// if the support is > minsup
				if (candidate.getAbsoluteSupport() >= minsupRelative) {
					
					// CODE SPECIFIC TO PASCAL
					// for each itemset that is a generator, if the support
					// is the same as the minimum of its subsets, then
					// it is not a generator and we want to remember that
					if(candidate.getAbsoluteSupport() == candidate.pred_sup) {
						candidate.isGenerator = false;
					}
					// END CODE SPECIFIC TO PASCAL
					
					
					// add the candidate
					level.add(candidate);
					// the itemset is frequent so save it into results
					saveItemsetToFile(candidate);
				}
			}
			// we will generate larger itemsets next.
			k++;
		} while (level.isEmpty() == false);

		// record end time
		endTimestamp = System.currentTimeMillis();
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// close the output file if the result was saved to a file.
		if (writer != null) {
			writer.close();
		}
	}

	/**
	 * This method generates candidates itemsets of size 2 based on itemsets of
	 * size 1.
	 * 
	 * @param frequent1
	 *            the list of frequent itemsets of size 1.
	 * @return a List of Itemset that are the candidates of size 2.
	 */
	private List<ItemsetPascal> generateCandidate2(List<ItemsetPascal> frequent1) {
		List<ItemsetPascal> candidates = new ArrayList<ItemsetPascal>();

		// For each itemset I1 and I2 of level k-1
		for (int i = 0; i < frequent1.size(); i++) {
			ItemsetPascal itemset1 = frequent1.get(i);
			int item1 = itemset1.get(0);
			for (int j = i + 1; j < frequent1.size(); j++) {
				ItemsetPascal itemset2 = frequent1.get(j);
				int item2 = itemset2.get(0);
				
				// CODE SPECIFIC TO PASCAL
				ItemsetPascal itemset = new ItemsetPascal(new int[] { item1, item2 });
				itemset.isGenerator 
				   = itemset1.isGenerator && itemset2.isGenerator;
				itemset.pred_sup = Math.min(itemset1.getAbsoluteSupport(), itemset2.getAbsoluteSupport());
				if(itemset.isGenerator == false) {
					itemset.support = itemset.pred_sup;
				}
				// END OF CODE SPECIFIC TO PASCAL
						
				// Create a new candidate by combining itemset1 and itemset2
				candidates.add(itemset);
			}
		}
		return candidates;
	}

	/**
	 * Method to generate itemsets of size k from frequent itemsets of size K-1.
	 * 
	 * @param levelK_1
	 *            frequent itemsets of size k-1
	 * @return itemsets of size k
	 */
	protected List<ItemsetPascal> generateCandidateSizeK(List<ItemsetPascal> levelK_1) {
		// create a variable to store candidates
		List<ItemsetPascal> candidates = new ArrayList<ItemsetPascal>();

		// For each itemset I1 and I2 of level k-1
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			int[] itemset1 = levelK_1.get(i).itemset;
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				int[] itemset2 = levelK_1.get(j).itemset;

				// we compare items of itemset1 and itemset2.
				// If they have all the same k-1 items and the last item of
				// itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a
				// candidate
				for (int k = 0; k < itemset1.length; k++) {
					// if they are the last items
					if (k == itemset1.length - 1) {
						// the one from itemset1 should be smaller (lexical
						// order)
						// and different from the one of itemset2
						if (itemset1[k] >= itemset2[k]) {
							continue loop1;
						}
					}
					// if they are not the last items, and
					else if (itemset1[k] < itemset2[k]) {
						continue loop2; // we continue searching
					} else if (itemset1[k] > itemset2[k]) {
						continue loop1; // we stop searching: because of lexical
										// order
					}
				}

				// Create a new candidate by combining itemset1 and itemset2
				int newItemset[] = new int[itemset1.length + 1];
				System.arraycopy(itemset1, 0, newItemset, 0, itemset1.length);
				newItemset[itemset1.length] = itemset2[itemset2.length - 1];

				// The candidate is tested to see if its subsets of size k-1 are
				// included in
				// level k-1 (they are frequent).
				ItemsetPascal newItemsetPascal = new ItemsetPascal(newItemset);
				if (allSubsetsOfSizeK_1AreFrequent(newItemsetPascal, levelK_1)) {
					// ------ CODE SPECIFIC TO PASCAL --------
					// if the candidate is not a generator then the support  of this
					// itemset is the same as the lowest support of its subsets of size k-1
					if(newItemsetPascal.isGenerator == false) {
						newItemsetPascal.support = newItemsetPascal.pred_sup;
					}
					// ------ END CODE SPECIFIC TO PASCAL --------
					candidates.add(newItemsetPascal);
				}
			}
		}
		return candidates; // return the set of candidates
	}

	/**
	 * Method to check if all the subsets of size k-1 of a candidate of size k
	 * are freuqnet
	 * 
	 * @param candidate
	 *            a candidate itemset of size k
	 * @param levelK_1
	 *            the frequent itemsets of size k-1
	 * @return true if all the subsets are frequet
	 */
	protected boolean allSubsetsOfSizeK_1AreFrequent(ItemsetPascal candidateItemset,
			List<ItemsetPascal> levelK_1) {
		int[] candidate = candidateItemset.itemset;
		
		// generate all subsets by always each item from the candidate, one by
		// one
		for (int posRemoved = 0; posRemoved < candidate.length; posRemoved++) {

			// perform a binary search to check if the subset appears in level
			// k-1.
			int first = 0;
			int last = levelK_1.size() - 1;

			// variable to remember if we found the subset
			boolean found = false;
			// the binary search
			while (first <= last) {
	        	int middle = ( first + last ) >>> 1; // divide by 2

				if (ArraysAlgos.sameAs(levelK_1.get(middle).getItems(), candidate, posRemoved) < 0) {
					first = middle + 1; // the itemset compared is larger than
										// the subset according to the lexical
										// order
				} else if (ArraysAlgos.sameAs(levelK_1.get(middle).getItems(), candidate, posRemoved) > 0) {
					last = middle - 1; // the itemset compared is smaller than
										// the subset is smaller according to
										// the lexical order
				} else {
					// WE HAVE FOUND IT
					found = true; 
					
					// ------ CODE SPECIFIC TO PASCAL --------
					// CHECK IF THE SUBSET IS A GENERATOR
					int supportMiddle = levelK_1.get(middle).getAbsoluteSupport();
					boolean isAGenerator = levelK_1.get(middle).isGenerator;
					if(isAGenerator == false) {
						// IF NOT THEN THE candidate itemset is also not a generator
						candidateItemset.isGenerator = false;
					}
					
					// if the support of this subset is smaller than the 
					// support of all subsets until now, we remember it.
					if(supportMiddle < candidateItemset.pred_sup) {
						candidateItemset.pred_sup = supportMiddle;
					}
					// ------ END OF CODE SPECIFIC TO PASCAL --------
					
					break;
				}
			}

			if (found == false) { // if we did not find it, that means that
									// candidate is not a frequent itemset
									// because
				// at least one of its subsets does not appear in level k-1.
				return false;
			}
		}
		return true;
	}

	/**
	 * Method to save a frequent itemset to file
	 * 
	 * @param itemset
	 * @throws IOException
	 */
	void saveItemsetToFile(ItemsetPascal itemset) throws IOException {
		writer.write(itemset.toString() + " #SUP: "
				+ itemset.getAbsoluteSupport() + " #IS_GENERATOR " + itemset.isGenerator);
		writer.newLine();
		System.out.println(itemset.toString());
		itemsetCount++;
	}

	/**
	 * Method to print statistics about the execution of the algorithm.
	 */
	public void printStats() {
		System.out.println("=============  PASCAL - STATS =============");
		System.out.println(" Candidates count : " + totalCandidateCount);
		System.out.println(" The algorithm stopped at size " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}
}
