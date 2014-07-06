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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AbstractAlgoPrefixSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoPrefixSpanMDSPM;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.Sequences;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.AlgoDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.MDPattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.MDPatterns;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.MDPatternsDatabase;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * Implementation of the SeqDim algorithm for multi-dimensional
 * sequential pattern mining proposed by Pinto et al (2001). 
 * <br/><br/>
 * SeqDIM is a generic algorithm that can be used in theory with any combination of sequential pattern mining algorithm
 * and MDPattern mining algorithm.<br/>
 * In SPMF, the sequential pattern mining algorithm must be chosen from:<br/>
 *     AlgoPrefixspanMDSPM/AlgoBIDEPlus/AlgoFournierViger08 algorithms <br/>
 * In SPMF, the MD-Pattern mining algorithm is AlgoDim and it offers to choose between Charm and AprioriClose
 *  (see the AlgoDim class for details).
 * @see AlgoFournierViger08
 * @see AlgoDim
 * @see AlgoBIDEPlus
 * @see AlgoPrefixSpanMDSPM
 * @see MDSequence
 * @see MDSequences
 * @see MDSequenceDatabase
* @author Philippe Fournier-Viger
 */
public class AlgoSeqDim {
	// The set of frequent MDSequences found by the algorithm
	protected MDSequences sequences = new MDSequences("FREQUENT MD-SEQUENCES");
	private long startTime;  // the start time of the algorithm
	private long endTime;    // the end time of the algorithm
	private boolean mineClosedPatterns = false; // if true, only closed patterns are found
	
	// object to write the output to a file
	BufferedWriter writer = null;
	// the number of frequent mdsequences found 
	private int patternCount; 
	
	// the number of mdsequences in the mdsequence database
	private int databaseSize =0;

	/**
	 * Run the algorithm
	 * @param database  and MDSequence database
	 * @param algoPrefixSpan  a prefixpsan based algorithm (BIDE, PrefixSpan or Fournier08)
	 * @param algoDim  an instance of the DIM algorithm
	 * @param mineClosedPatterns  if true, only closed mdsequential pattern will be returned
	 * @param output  a path for writting the result to an output file
	 * @return  the set of MD-sequential patterns found
	 * @throws IOException exception if error writing to file
	 */
	public MDSequences runAlgorithm(MDSequenceDatabase database,
			AbstractAlgoPrefixSpan algoPrefixSpan, AlgoDim algoDim,
			boolean mineClosedPatterns, String output) throws IOException {

		// reset the utility for memory usage logging
		MemoryLogger.getInstance().reset();
		// reset number of pattern found
		patternCount =0;
		// save start time
		startTime = System.currentTimeMillis();
		// prepare object to write output file
		writer = new BufferedWriter(new FileWriter(output));
		
		// save the number of mdsequences in the database
		databaseSize =  database.size();  
		
		// save user preference
		this.mineClosedPatterns = mineClosedPatterns;
		
		// (1) First mine sequential patterns by applying
		// a prefixspan based algorithm
		Sequences sequencesFound = algoPrefixSpan.runAlgorithm(database
				.getSequenceDatabase());

		// (2) For each frequent sequential pattern found, ç
		// form projected MD-Database
		// and then find MD-patterns within projected databases
		
		// for each level
		for (int j = 0; j < sequencesFound.getLevelCount(); j++) {
			List<Sequence> sequencesList = sequencesFound.getLevel(j);
			// for each sequential pattern
			for (Sequence sequence : sequencesList) {
				// try to use this sequential pattern to
				// generate md-sequential patterns
				trySequence(sequence, database, algoPrefixSpan.getMinSupp(),
						algoDim);
			}
		}

		// (3) IF the user wants closed patterns only, we eliminate 
		// non-closed multidimensional sequential patterns
		if (mineClosedPatterns) {
			removeRedundancy();
		}
		
		// record end time
		endTime = System.currentTimeMillis();
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// return the set of MD sequential patterns
		return sequences;
	}


	/**
	 * Try to use a sequential pattern to generate MD sequential patterns
	 * @param sequence a sequential pattern
	 * @param database the MD sequence database
	 * @param minsupp the minsup threshold (double)
	 * @param algoDim an instance of the DIM algorithm
	 * @throws IOException exception if error writing output file
	 */
	private void trySequence(Sequence sequence, MDSequenceDatabase database,
			double minsupp, AlgoDim algoDim) throws IOException {
		// (a) Create a projected database by using only
		// the sequence containing the given sequential pattern.
		MDPatternsDatabase newContexte = createProjectedDatabase(
				sequence.getSequencesID(), database.getPatternDatabase());

		// (b) Run the DIM algorithm on the projected database.
		// To do that we need to adjust the minimum support based
		// on the number of sequences on the projected database as follows
		double newMinSupp = minsupp * database.size() / newContexte.size();
		// Run the DIM algorithm
		MDPatterns patterns = algoDim.runAlgorithm(newContexte, newMinSupp);

		// (c) Create MD-Sequences by combining the mdpatterns found
		// with the sequential pattern received as parameter
		
		// for each level
		for (int i = 0; i < patterns.getLevelCount(); i++) {
			// for each mdpattern
			for (MDPattern pattern : patterns.getLevel(i)) {
				// combine the mdpattern with the seq. pattern to
				// form a md sequential pattern
				MDSequence mdsequence = new MDSequence(0, pattern, sequence);
				// check if there is only wild cards in this mdpattern
				boolean onlyWildcards = true;
				for(Integer id: pattern.getPatternsID()){
					if(id != MDPattern.WILDCARD){
						onlyWildcards = false;
						break;
					}
				}
				// if only wilcard, then the support is the support
				// of the sequential pattern
				if(onlyWildcards){
					mdsequence.setSupport(sequence.getSequencesID().size()); 
					
				}else{
					// otherwise it is the support of the mdpattern
					mdsequence.setSupport(pattern.getAbsoluteSupport()); 
					
				}

				// finally we save the mdsequential pattern
				savePattern(sequence, mdsequence);
			}
		}

	}

	/**
	 * This method saves an md seq. pattern to a file or to memory
	 * @param sequence the sequence in the md seq. pattern
	 * @param mdsequence  the md seq. pattern
	 * @throws IOException exception if error writing to file
	 */
	private void savePattern(Sequence sequence, MDSequence mdsequence) throws IOException {
		// if the user wants only closed patterns
		if(mineClosedPatterns == false){
			// write to file
			writeToFile(mdsequence);
		}else{
			// if the user wants all patterns, then save to memory.
			sequences.addSequence(mdsequence, sequence.size());
		}
		// increase number of md seq. patterns found
		patternCount++;
	}


	/**
	 * Write a md sequence to the output file
	 * @param mdsequence an md sequence
	 * @throws IOException if error while writing to file
	 */
	private void writeToFile(MDSequence mdsequence) throws IOException {
		// create string buffer
		StringBuffer buffer = new StringBuffer();
		// append mdpattern
		buffer.append(mdsequence.getMdpattern().toStringShort());
		// append mdsequence
		buffer.append(mdsequence.getSequence().toStringShort());
		// append support
		buffer.append(" #SUP: ");
		buffer.append(mdsequence.getAbsoluteSupport());
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	
	/**
	 * Create a projected MD-pattern. database by keeping only the 
	 * MDPatterns corresponding to a set of sequence IDs.
	 * @param patternsIds
	 *            The set of sequence IDS
	 * @param patternsDatabase
	 *            The original md patterns database
	 * @return A new database containing only the MDPatterns to keep.
	 */
	private MDPatternsDatabase createProjectedDatabase(
			Set<Integer> patternsIds, MDPatternsDatabase patternsDatabase) {
		// create projected database
		MDPatternsDatabase projectedDatabase = new MDPatternsDatabase();
		// for each pattern
		for (MDPattern pattern : patternsDatabase.getMDPatterns()) {
			// if the id is in the set of desired ids
			if (patternsIds.contains(pattern.getId())) {
				// add to the projected database
				projectedDatabase.addMDPattern(pattern);
			}
		}
		// return projected database
		return projectedDatabase;
	}

	/**
	 * Print statistics about the algorithm execution
	 * @param databaseSize the number of MDsequences in the original database.
	 */
	public void printStatistics(int databaseSize) {
		StringBuffer r = new StringBuffer(140);
		r.append("=============  SEQ-DIM - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" max memory : ");
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append("\n Frequent sequences count : ");
		r.append(patternCount);
		System.out.println(r.toString());
//		sequences.printFrequentSequences(objectsCount);
		System.out
				.println("===================================================");
	}

	/**
	 * Eliminate non-closed multidimensional sequential patterns by simply
	 * looping and eliminating redundant patterns. This is necessary if we want
	 * to mine closed multi-dim. seq. patterns, because: closed sequential patt.
	 * mining + closed itemset mining != closed multi-dim seq. patt. mining. 
	 * For more details about this, see the paper published by
	 * Panida Songram, Veera Boonjing and Sarun Intakosum (2006) that 
	 * explains why we can do that.
	 * @throws IOException  exception if error while writing to file
	 */
	private void removeRedundancy() throws IOException {

		// For each level
		for (int i = sequences.getLevels().size() - 1; i > 0; i--) {
			// for each md sequential pattern
			for (MDSequence sequence : sequences.getLevel(i)) {
				// We check if the  md sequential pattern is 
				// strictly included in another 
				//  md sequential pattern having the same support.
				boolean included = false;
				// for each level
				for (int j = i; j < sequences.getLevels().size() && !included; j++) {
					//for each other md sequential pattern
					for (MDSequence sequence2 : sequences.getLevel(j)) {
						// if the first md sequential pattern is included
						// in the second and they have the same support
						if (sequence != sequence2
								&& sequence2.getAbsoluteSupport() == sequence
										.getAbsoluteSupport()
								&& sequence2.contains(sequence)) {
							// note it
							included = true;
							// then break.
							break;
						}
					}
				}
				// if the md sequential pattern is NOT included in another sequential pattern
				// having the same support
				if (!included) {
					// save the pattern.
					writeToFile(sequence);
				}
			}
		}
	}
	
}
