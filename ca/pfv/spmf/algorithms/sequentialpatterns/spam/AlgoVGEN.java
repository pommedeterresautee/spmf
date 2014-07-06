package ca.pfv.spmf.algorithms.sequentialpatterns.spam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * *
 * This is an implementation of the VGEN algorithm.
 * <br/><br/>
 * 
 * Copyright (c) 2014 Philippe Fournier-Viger, Antonio Gomariz
 * <br/><br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * <br/><br/>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <br/><br/>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <br/><br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see Bitmap
*  @see PrefixVGEN
*  @see PatternVGEN
*  @author Philippe Fournier-Viger  & Antonio Gomariz
 */
public class AlgoVGEN {

    // for statistics
    public long startTime;
    public long endTime;
    public int patternCount;
    
    // minsup
    private int minsup = 0;
    
    // object to write to a file
    BufferedWriter writer = null;
    
    // Vertical database
    Map<Integer, Bitmap> verticalDB = new HashMap<Integer, Bitmap>();
    
    // List indicating the number of bits per sequence
    List<Integer> sequencesSize = null;
    int lastBitIndex = 0;  // the last bit position that is used in bitmaps
    
    // maximum pattern length in terms of item count
    private int maximumPatternLength = Integer.MAX_VALUE;
    
    // Map: key: item   value:  another item that followed the first item + support
    // (could be replaced with a triangular matrix...)
    Map<Integer, Map<Integer, Integer>> coocMapAfter = null;
    Map<Integer, Map<Integer, Integer>> coocMapEquals = null;
    
    // Map indicating for each item, the smallest tid containing this item
    // in a sequence.
    boolean useCMAPPruning = true;
    
    //  =========  VARIABLES THAT ARE SPECIFIC TO VGEN ===================
    // GENERATOR PATTERNS -  The list contains patterns of size k at position k in the list.
    // A map has the sum of sids as key and lists of patterns as value.
    List<Map<Integer, List<PatternVGEN>>> generatorPatterns = null; 

    // variables to enable/disable strategies
	private boolean useImmediateBackwardChecking = true;
	private boolean useBackwardPruning = false;
	
	// if enabled, the result will be verified to see if some patterns found are not generators.
	boolean DEBUG_MODE = true;  	
	
    // the number of transaction in the database (to calculate the support of the empty set)
    int transactionCount = 0;
    
    //=========  END OF VARIABLES THAT ARE SPECIFIC TO VGEN ===================
    

    /**
     * Default constructor
     */
    public AlgoVGEN() {
    }

    /**
     * Method to run the algorithm
     *
     * @param input path to an input file
     * @param outputFilePath path for writing the output file
     * @param minsupRel the minimum support as a relative value
     * @return 
     * @throws IOException exception if error while writing the file or reading
     */
    public List<Map<Integer, List<PatternVGEN>>> runAlgorithm(String input, String outputFilePath, double minsupRel) throws IOException {
    	if(DEBUG_MODE){
    		System.out.println(" %%%%%%%%%%  DEBUG MODE %%%%%%%%%%");
    	}
    	
    	   	
        Bitmap.INTERSECTION_COUNT = 0;
        // create an object to write the file
        writer = new BufferedWriter(new FileWriter(outputFilePath));
        // initialize the number of patterns found
        patternCount = 0;
        // to log the memory used
        MemoryLogger.getInstance().reset();

        // record start time
        startTime = System.currentTimeMillis();
        // RUN THE ALGORITHM
        vgen(input, minsupRel);
        // record end time
        endTime = System.currentTimeMillis();
        
        writeResultTofile(outputFilePath);
        
        // close the file
        writer.close();
        
        // ################################## FOR DEBUGGGING #############################
        // ########  THIS CODE CHECK IF A PATTERN FOUND IS NOT A GENERATOR ##############
        if(DEBUG_MODE) {
        	System.out.println("minsup absolute : " + minsup);
        	
        	List<PatternVGEN> listPatterns = new ArrayList<PatternVGEN>();
        	for(Map<Integer, List<PatternVGEN>> mapSizeI : generatorPatterns) {
	        	if(mapSizeI == null) {
	        		continue;
	        	}
	        	for(List<PatternVGEN> listpattern : mapSizeI.values()) {
//	        		System.out.println(" " + pat.prefix  + "    sup: " + pat.getSupport());
	        		for(PatternVGEN pat : listpattern) {
	        			listPatterns.add(pat);
	        		}
	        	}
	        }
        	// CHECK IF SOME PATTERNS ARE NOTE GENERATORS
        	for(PatternVGEN pat1 : listPatterns) {
        		// if this pattern is not the empty set and the support is same as empty set, then it is not a generator
        		if(pat1.prefix.size() > 0 && pat1.getAbsoluteSupport() == transactionCount) {
        			System.out.println("NOT A GENERATOR !!!!!!!!!  "  + pat1.prefix + "    sup: " + pat1.bitmap.getSupport() + " because of empty set");
        		}
        		
        		// otherwise we have to compare with every other patterns.
        		for(PatternVGEN pat2 : listPatterns) {
            		if(pat1 == pat2) {
            			continue;
            		}
            		
            		if(pat1.getAbsoluteSupport() == pat2.getAbsoluteSupport()) {
            			if(strictlyContains(pat1.prefix, pat2.prefix)) {
            				System.out.println("NOT A GENERATOR !!!!!!!!!  "  + pat1.prefix + " " + pat2.prefix + "   sup: " + pat1.bitmap.getSupport());
                			System.out.println(pat1.bitmap.sidsum + " " +  pat2.bitmap.sidsum);
            			}
            		}
            	}
        	}
        }
        // ############################ END OF DEBUGGING CODE ################################
        
        return generatorPatterns;
    }

    /**
     * This is the main method for the VGEN algorithm
     *
     * @param an input file
     * @param minsupRel the minimum support as a relative value
     * @throws IOException
     */
    private void vgen(String input, double minsupRel) throws IOException {
    	// create maxPattern array
    	generatorPatterns = new ArrayList<Map<Integer, List<PatternVGEN>>>(20);
        generatorPatterns.add(new HashMap<Integer, List<PatternVGEN>>());
        generatorPatterns.add(new HashMap<Integer, List<PatternVGEN>>());
        
        // the structure to store the vertical database
        // key: an item    value : bitmap
        verticalDB = new HashMap<Integer, Bitmap>();

        // structure to store the horizontal database
        List<int[]> inMemoryDB = new ArrayList<int[]>();

        // STEP 0: SCAN THE DATABASE TO STORE THE FIRST BIT POSITION OF EACH SEQUENCE 
        // AND CALCULATE THE TOTAL NUMBER OF BIT FOR EACH BITMAP
        sequencesSize = new ArrayList<Integer>();
        lastBitIndex = 0; // variable to record the last bit position that we will use in bitmaps
        try {
            // read the file
            FileInputStream fin = new FileInputStream(new File(input));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String thisLine;
            int bitIndex = 0;
            // for each line (sequence) in the file until the end
            while ((thisLine = reader.readLine()) != null) {
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (thisLine.isEmpty() == true
                        || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
                        || thisLine.charAt(0) == '@') {
                    continue;
                }

                // record the length of the current sequence (for optimizations)
                sequencesSize.add(bitIndex);
                // split the sequence according to spaces into tokens

                String tokens[] = thisLine.split(" ");
                int[] transactionArray = new int[tokens.length];
                inMemoryDB.add(transactionArray);

                for (int i = 0; i < tokens.length; i++) {
                    int item = Integer.parseInt(tokens[i]);
                    transactionArray[i] = item;
                    // if it is not an itemset separator
                    if (item == -1) { // indicate the end of an itemset
                        // increase the number of bits that we will need for each bitmap
                        bitIndex++;
                    }
                }
            }
            // record the last bit position for the bitmaps
            lastBitIndex = bitIndex - 1;
            reader.close(); // close the input file
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Calculate the absolute minimum support 
        // by multipling the percentage with the number of
        // sequences in this database
        minsup = (int) Math.ceil(minsupRel * sequencesSize.size());
        if (minsup == 0) {
            minsup = 1;
        }
        
        // variable to count the number of transactions
    	transactionCount = 0;

        // STEP1: SCAN THE DATABASE TO CREATE THE BITMAP VERTICAL DATABASE REPRESENTATION
        try {
            FileInputStream fin = new FileInputStream(new File(input));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String thisLine;
            int sid = 0; // to know which sequence we are scanning
            int tid = 0;  // to know which itemset we are scanning

            // for each line (sequence) from the input file
            while ((thisLine = reader.readLine()) != null) {
                // split the sequence according to spaces into tokens
                for (String token : thisLine.split(" ")) {
                    if (token.equals("-1")) { // indicate the end of an itemset
                        tid++;
                    } else if (token.equals("-2")) { // indicate the end of a sequence
//						determineSection(bitindex - previousBitIndex);  // register the sequence length for the bitmap
                        sid++;
                        tid = 0;
                    } else {  // indicate an item
                        // Get the bitmap for this item. If none, create one.
                        Integer item = Integer.parseInt(token);
                        Bitmap bitmapItem = verticalDB.get(item);
                        if (bitmapItem == null) {
                            bitmapItem = new Bitmap(lastBitIndex);
                            verticalDB.put(item, bitmapItem);
                        }
                        // Register the bit in the bitmap for this item
                        bitmapItem.registerBit(sid, tid, sequencesSize);
                    }
                }
                transactionCount++;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // STEP2: REMOVE INFREQUENT ITEMS FROM THE DATABASE BECAUSE THEY WILL NOT APPEAR IN ANY FREQUENT SEQUENTIAL PATTERNS
        List<Integer> frequentItems = new ArrayList<Integer>();
        Iterator<Entry<Integer, Bitmap>> iter = verticalDB.entrySet().iterator();
        // we iterate over items from the vertical database that we have in memory
        while (iter.hasNext()) {
            //  we get the bitmap for this item
            Map.Entry<Integer, Bitmap> entry = (Map.Entry<Integer, Bitmap>) iter.next();
            // if the cardinality of this bitmap is lower than minsup
            if (entry.getValue().getSupport() < minsup) {
                // we remove this item from the database.
                iter.remove();
            } else {
                // otherwise, we save this item as a frequent
                // sequential pattern of size 1
            	// CHANGED
                // and we add this item to a list of frequent items
                // that we will use later.
               frequentItems.add(entry.getKey());
                // END CHANGED
            }
        }
        
        // SET 2.1  SORT ITEMS BY DESCENDING SUPPORT
        Collections.sort(frequentItems, new Comparator<Integer>() {

			@Override
			public int compare(Integer arg0, Integer arg1) {
				return verticalDB.get(arg0).getSupport() - verticalDB.get(arg1).getSupport();
			}
        	
        });

        // STEP 3.1  CREATE CMAP
        coocMapEquals = new HashMap<Integer, Map<Integer, Integer>>(frequentItems.size());
        coocMapAfter = new HashMap<Integer, Map<Integer, Integer>>(frequentItems.size());

        for (int[] transaction : inMemoryDB) {
            short itemsetCount = 0;

            Set<Integer> alreadyProcessed = new HashSet<Integer>();
            Map<Integer, Set<Integer>> equalProcessed = new HashMap<>();
            loopI:
            for (int i = 0; i < transaction.length; i++) {
                Integer itemI = transaction[i];

                Set<Integer> equalSet = equalProcessed.get(itemI);
                if (equalSet == null) {
                    equalSet = new HashSet<Integer>();
                    equalProcessed.put(itemI, equalSet);
                }

                if (itemI < 0) {
                    itemsetCount++;
                    continue;
                }

                Bitmap bitmapOfItem = verticalDB.get(itemI);
                if (bitmapOfItem == null || bitmapOfItem.getSupport() < minsup) {
                    continue;
                }

                Set<Integer> alreadyProcessedB = new HashSet<Integer>(); // NEW

                boolean sameItemset = true;
                for (int j = i + 1; j < transaction.length; j++) {
                    Integer itemJ = transaction[j];

                    if (itemJ < 0) {
                        sameItemset = false;
                        continue;
                    }

                    Bitmap bitmapOfitemJ = verticalDB.get(itemJ);
                    if (bitmapOfitemJ == null || bitmapOfitemJ.getSupport() < minsup) {
                        continue;
                    }
//									if (itemI != itemJ){
                    Map<Integer, Integer> map = null;
                    if (sameItemset) {
                        if (!equalSet.contains(itemJ)) {
                            map = coocMapEquals.get(itemI);
                            if (map == null) {
                                map = new HashMap<Integer, Integer>();
                                coocMapEquals.put(itemI, map);
                            }
                            Integer support = map.get(itemJ);
                            if (support == null) {
                                map.put(itemJ, 1);
                            } else {
                                map.put(itemJ, ++support);
                            }
                            equalSet.add(itemJ);
                        }
                    } else if (!alreadyProcessedB.contains(itemJ)) {
                        if (alreadyProcessed.contains(itemI)) {
                            continue loopI;
                        }
                        map = coocMapAfter.get(itemI);
                        if (map == null) {
                            map = new HashMap<Integer, Integer>();
                            coocMapAfter.put(itemI, map);
                        }
                        Integer support = map.get(itemJ);
                        if (support == null) {
                            map.put(itemJ, 1);
                        } else {
                            map.put(itemJ, ++support);
                        }
                        alreadyProcessedB.add(itemJ); // NEW
                    }
                }
                alreadyProcessed.add(itemI);
            }
        }

        // STEP3: WE PERFORM THE RECURSIVE DEPTH FIRST SEARCH
        // to find longer sequential patterns recursively

        if (maximumPatternLength == 1) {
            return;
        }
        
        if(DEBUG_MODE) {
        	System.out.println("transaction count = " + transactionCount );
        } 
      
        
        // NEW2014: SAVE ALL SINGLE FREQUENT ITEMS FIRST  BEFORE PERFORMING DEPTH FIRST SEARCH   =========
        List<PatternVGEN> prefixSingleItems = new ArrayList<PatternVGEN>(verticalDB.entrySet().size());
        for (Entry<Integer, Bitmap> entry : verticalDB.entrySet()) {
            // We create a prefix with that item
            PrefixVGEN prefix = new PrefixVGEN();
            prefix.addItemset(new Itemset(entry.getKey()));
            boolean itemIsEven = entry.getKey() % 2 == 0;
            if(itemIsEven) {
            	prefix.sumOfEvenItems = (Integer)entry.getKey();
            	prefix.sumOfOddItems = 0;
            }else {
            	prefix.sumOfEvenItems = 0;
            	prefix.sumOfOddItems = (Integer)entry.getKey();
            }
            PatternVGEN pattern = new PatternVGEN(prefix, entry.getValue());
            prefixSingleItems.add(pattern);
            
            // NEW 2014 : IMPORTANT!!!! -- > DON'T OUTPUT PATTERN IF SUPPORT IS EQUAL TO SDB SIZE
            // BUT NOTE THAT WE WILL STILL NEED TO DO THE DEPTH FIRST SEARCH FOR THIS PATTERN IN THE NEXT FOR LOOP...
            if(transactionCount != entry.getValue().getSupport()) {
               // SAVE THE PATTERN TO THE RESULT
               List<PatternVGEN> listPatterns = generatorPatterns.get(1).get(pattern.bitmap.sidsum);
               if(listPatterns == null) {
            	   listPatterns = new ArrayList<PatternVGEN>();
            	   generatorPatterns.get(1).put(pattern.bitmap.sidsum, listPatterns);
               }
        	   listPatterns.add(pattern);
               patternCount++;
            }
         }
        
        // PERFORM THE DEPTH FIRST SEARCH
        for (PatternVGEN pattern: prefixSingleItems) {
            // We create a prefix with that item
            int item = pattern.prefix.get(0).get(0);
            dfsPruning(pattern.prefix, pattern.bitmap, frequentItems, frequentItems, item, 2, item);
         }
        
        // THE EMPTY SET IS ALWAYS GENERATOR, SO ADD IT TO THE RESULT SET
    	Bitmap bitmap = new Bitmap(0);
    	bitmap.setSupport(transactionCount);
    	PatternVGEN pat = new PatternVGEN(new PrefixVGEN(), bitmap);
    	List<PatternVGEN> listLevel0 = new ArrayList<PatternVGEN>();
    	listLevel0.add(pat);
    	generatorPatterns.get(0).put(0, listLevel0);
    	patternCount++;
        // END NEW 2014 =============
    }

    /**
     * This is the dfsPruning method as described in the SPAM paper.
     *
     * @param prefix the current prefix
     * @param prefixBitmap the bitmap corresponding to the current prefix
     * @param sn a list of items to be considered for i-steps
     * @param in a list of items to be considered for s-steps
     * @param hasToBeGreaterThanForIStep
     * @param m size of the current prefix in terms of items
     * @param lastAppendedItem the last appended item to the prefix
     * @throws IOException if there is an error writing a pattern to the output
     * file
     * @return TRUE IF A FREQUENT PATTERN WAS CREATED USING THE PREFIX.
     */
    void dfsPruning(PrefixVGEN prefix, Bitmap prefixBitmap, List<Integer> sn, List<Integer> in, int hasToBeGreaterThanForIStep, int m, Integer lastAppendedItem) throws IOException {
    	//		System.out.println(prefix.toString());

        //  ======  S-STEPS ======
        // Temporary variables (as described in the paper)
        List<Integer> sTemp = new ArrayList<Integer>();
        List<Bitmap> sTempBitmaps = new ArrayList<Bitmap>();

        // for CMAP pruning, we will only check against the last appended item
        Map<Integer, Integer> mapSupportItemsAfter = coocMapAfter.get(lastAppendedItem);

        // for each item in sn
        loopi:
        for (Integer i : sn) {

            // LAST POSITION PRUNING
            /*if (useLastPositionPruning && lastItemPositionMap.get(i) < prefixBitmap.firstItemsetID) {
             //				System.out.println("TEST");
             continue loopi;
             }*/

            // CMAP PRUNING
            // we only check with the last appended item
            if (useCMAPPruning) {
                if (mapSupportItemsAfter == null) {
                    continue loopi;
                }
                Integer support = mapSupportItemsAfter.get(i);
                if (support == null || support < minsup) {
//							System.out.println("PRUNE");
                    continue loopi;
                }
            }

            // perform the S-STEP with that item to get a new bitmap
            Bitmap.INTERSECTION_COUNT++;
            Bitmap newBitmap = prefixBitmap.createNewBitmapSStep(verticalDB.get(i), sequencesSize, lastBitIndex);
            // if the support is higher than minsup
            if (newBitmap.getSupport() >= minsup) {
                // record that item and pattern in temporary variables
                sTemp.add(i);
                sTempBitmaps.add(newBitmap);
            }
        }
        // for each pattern recorded for the s-step
        for (int k = 0; k < sTemp.size(); k++) {
        	// STRATEGY: NEWWW
            
            int item = sTemp.get(k);
            // create the new prefix
            PrefixVGEN prefixSStep = prefix.cloneSequence();
            prefixSStep.addItemset(new Itemset(item));
            if(item % 2 == 0) {
            	prefixSStep.sumOfEvenItems = item + prefix.sumOfEvenItems;
            	prefixSStep.sumOfOddItems = prefix.sumOfOddItems;
            }else {
            	prefixSStep.sumOfEvenItems = prefix.sumOfEvenItems;
            	prefixSStep.sumOfOddItems = item + prefix.sumOfOddItems;
            }
//            prefixSStep.sumOfItems = item + prefix.sumOfItems;
            
            // create the new bitmap
            Bitmap newBitmap = sTempBitmaps.get(k);

            // save the pattern to the file

           // NEW STRATEGY :  IMMEDIATE BACKWARD EXTENSION
           boolean hasNoImmediateBackwardExtension = useImmediateBackwardChecking ||
        		   prefixBitmap.getSupport() != newBitmap.getSupport();
           
            if (maximumPatternLength > m && hasNoImmediateBackwardExtension) {
            	
                boolean hasBackWardExtension = savePatternMultipleItems(prefixSStep, newBitmap, m);
                // NEW 2014: IF BACKWARD EXTENSION, THEN WE DON'T CONTINUE...
                if(hasBackWardExtension == false) {
                	dfsPruning(prefixSStep, newBitmap, sTemp, sTemp, item, m + 1, item);
                }
            }
        }

        Map<Integer, Integer> mapSupportItemsEquals = coocMapEquals.get(lastAppendedItem);
        // ========  I STEPS =======
        // Temporary variables
        List<Integer> iTemp = new ArrayList<Integer>();
        List<Bitmap> iTempBitmaps = new ArrayList<Bitmap>();

        // for each item in in
        loop2:
        for (Integer i : in) {


            // the item has to be greater than the largest item
            // already in the last itemset of prefix.
            if (i > hasToBeGreaterThanForIStep) {

                // LAST POSITION PRUNING
                /*if (useLastPositionPruning && lastItemPositionMap.get(i) < prefixBitmap.firstItemsetID) {
                 continue loop2;
                 }*/

                // CMAP PRUNING
                if (useCMAPPruning) {
                    if (mapSupportItemsEquals == null) {
                        continue loop2;
                    }
                    Integer support = mapSupportItemsEquals.get(i);
                    if (support == null || support < minsup) {
                        continue loop2;
                    }
                }

                // Perform an i-step with this item and the current prefix.
                // This creates a new bitmap
                Bitmap.INTERSECTION_COUNT++;
                Bitmap newBitmap = prefixBitmap.createNewBitmapIStep(verticalDB.get(i), sequencesSize, lastBitIndex);
                // If the support is no less than minsup
                if (newBitmap.getSupport() >= minsup) {
                    // record that item and pattern in temporary variables
                    iTemp.add(i);
                    iTempBitmaps.add(newBitmap);
                }
            }
        }
        // for each pattern recorded for the i-step
        for (int k = 0; k < iTemp.size(); k++) {// STRATEGY: NEWWW
           // atLeastOneFrequentExtension = true;
            
            int item = iTemp.get(k);
            // create the new prefix
            PrefixVGEN prefixIStep = prefix.cloneSequence();
            prefixIStep.getItemsets().get(prefixIStep.size() - 1).addItem(item);
            if(item % 2 == 0) {
            	prefixIStep.sumOfEvenItems = item + prefix.sumOfEvenItems;
            	prefixIStep.sumOfOddItems = prefix.sumOfOddItems;
            }else {
            	prefixIStep.sumOfEvenItems = prefix.sumOfEvenItems;
            	prefixIStep.sumOfOddItems = item + prefix.sumOfOddItems;
            }
            // create the new bitmap
            Bitmap newBitmap = iTempBitmaps.get(k);

            
         // NEW STRATEGY :  IMMEDIATE BACKWARD EXTENSION
            boolean hasNoImmediateBackwardExtension = useImmediateBackwardChecking ||
         		   prefixBitmap.getSupport() == newBitmap.getSupport();
            
            if (maximumPatternLength > m  && hasNoImmediateBackwardExtension) {
            	boolean hasBackWardExtension = savePatternMultipleItems(prefixIStep, newBitmap, m);
                // NEW 2014: IF NO BACKWARD EXTENSION, THEN WE TRY TO EXTEND THAT PATTERN
                if(hasBackWardExtension == false) {
                	dfsPruning(prefixIStep, newBitmap, sTemp, iTemp, item, m + 1, item);
                }
            }
        }
        // check the memory usage
        MemoryLogger.getInstance().checkMemory();
        
    }  

    /**
     * Save a pattern of size > 1 to the output file.
     *
     * @param prefix the prefix
     * @param bitmap its bitmap
     * @throws IOException exception if error while writing to the file
     * @return true IF THE PATTERN HAS A BACKWARD EXTENSION WITH THE SAME PROJECTED DATABASE
     */
    private boolean savePatternMultipleItems(PrefixVGEN prefix, Bitmap bitmap, int length) throws IOException {
//    	System.out.println("prefix :" + prefix);
    	int sidsum = bitmap.sidsum;
    	
    	// IF THE SUPPORT OF THIS PATTERN "PREFIX" IS THE SUPPORT OF THE EMPTY SET, THEN
    	// THIS PATTERN IS NOT A GENERATOR.
    	if(bitmap.getSupport() == transactionCount) {
    		return false;
    	}
    	

        // WE COMPARE PATTERN "PREFIX" WITH SMALLER PATTERNS FOR SUB-PATTERN CHECKING
    	boolean mayBeAGenerator = true;
    	// FOR PATTERNS OF SIZE 1 TO THE SIZE OF THE PATTERN MINUS 1
       	for(int i=1; i < length && i < generatorPatterns.size(); i++) {
       		// GET ALL THE PATTERNS HAVING THE SAME SID-SUM AS THE CURRENT PATTERN
       		List<PatternVGEN> level = generatorPatterns.get(i).get(sidsum); 
       		if(level == null) {
       			continue;
       		}
       		for(PatternVGEN pPrime : level) {
   	        	
   	        	// CHECK THE SUM OF EVEN AND ODD ITEMS AND THE SUPPORT
   	        	if(prefix.sumOfEvenItems >= pPrime.prefix.sumOfEvenItems &&
   	     			   prefix.sumOfOddItems >= pPrime.prefix.sumOfOddItems &&
   	     			   bitmap.getSupport() == pPrime.getAbsoluteSupport() &&
   	     			   strictlyContains(prefix, pPrime.prefix)) {
   	        		
   	        		// CHECK HERE IF THERE IS A BACKWARD EXTENSION...
   	        		if (useBackwardPruning) {
   	        			if(isThereBackwardExtension(bitmap, pPrime.bitmap)){
   	        				// THERE IS A BACKWARD EXTENSION SO WE RETURN TRUE TO PRUNE EXTENSIONS
   	        				// OF THE PATTERN "PREFIX"
   	        				return true;  
   	        			}else {
   	        				// WE FLAG THE PATTERN "PREFIX" HAS NOT BEING A GENERATOR BUT
   	        				// WE CONTINUE COMPARING WITH OTHER PATTERNS TO SEE IF WE COULD PRUNE
   	        				mayBeAGenerator = false;
   	        			}
   	        		}else {
   	        			// IF BACKWARD EXTENSION CHECKING IS DISABLED, WE RETURN FALSE  
   	        		// WE JUST RETURN FALSE IF WE DON'T USE THE BACKWARD PRUNING. THIS IS A TRADE-OFF
   	   	        		return false;  
   	        		}
   	        		// END IMPORTANT
   	        	}
   			}
           }
    	
       	if(mayBeAGenerator == false) {
       		return false;
       	}
    	
        // WE COMPARE WITH LARGER PATTERNS FOR SUPER-PATTERN CHECKING
    	for(int i=generatorPatterns.size()-1; i > length; i--) {
    		
       		List<PatternVGEN> level = generatorPatterns.get(i).get(sidsum); 
       		if(level == null) {
       			continue;
       		}
       		Iterator<PatternVGEN> iter  = level.iterator(); 
       		
       		while (iter.hasNext()) {
   	        	PatternVGEN pPrime = iter.next();

    			if(prefix.sumOfEvenItems <= pPrime.prefix.sumOfEvenItems &&
    			   prefix.sumOfOddItems <= pPrime.prefix.sumOfOddItems &&
    			   bitmap.getSupport() == pPrime.getAbsoluteSupport() && 
    			   strictlyContains(pPrime.prefix, prefix)) {
    				
	    	 		patternCount--;  // DECREASE COUNT
	        		iter.remove();
	        	}
    		}
    	}

        // OTHERWISE THE PATTERN "PREFIX" MAY BE A GENERATOR SO WE KEEP IT 
        while(generatorPatterns.size() -1 < length) {
        	generatorPatterns.add(new HashMap<Integer, List<PatternVGEN>>());
        }
        
        List<PatternVGEN> listPatterns = generatorPatterns.get(length).get(sidsum);
        if(listPatterns == null) {
     	   listPatterns = new ArrayList<PatternVGEN>();
     	   generatorPatterns.get(length).put(sidsum, listPatterns);
        }

        patternCount++;  // INCREASE COUNT
        listPatterns.add(new PatternVGEN(prefix, bitmap));

        return false; // No backward extension has been found. 
    }
    
    /**
     * Check if there is a backward extension by comparing the bitmap of two patterns
     * P1 and P2, such that P1 is a superset of P2
     * @param bitmap  bitmap of P1
     * @param bitmap2 bitmap of P2
     * @return true if there is a backward extension
     */
    private boolean isThereBackwardExtension(Bitmap bitmap1, Bitmap bitmap2) {
//    	System.out.println("is there backward?");
//    	System.out.println(bitmap1.bitmap.toString());
//    	System.out.println(bitmap2.bitmap.toString());
    	BitSet bitset1 = bitmap1.bitmap;
    	BitSet bitset2 = bitmap2.bitmap;
    	
    	int currentBit1 = bitset1.nextSetBit(0);
    	int currentBit2 = bitset2.nextSetBit(0);
    	
    	do {
    		if(currentBit1 > currentBit2) {
    			return false;
    		}
    		
    		currentBit1 = bitset1.nextSetBit(currentBit1+1);
        	currentBit2 = bitset2.nextSetBit(currentBit2+1);
    	}while(currentBit1 >0);
    	
    	return true;
//		return bitmap.equals(bitmap2);
	}

	/**
	 * This methods checks if a seq. pattern "pattern2" is strictly contained in a seq. pattern "pattern1".
     * @param pattern1 a sequential pattern
     * @param pattern2 another sequential pattern
	 * @return true if the pattern1 contains pattern2.
	 */
	boolean strictlyContains(PrefixVGEN pattern1, PrefixVGEN pattern2) {
//		// if pattern2 is larger or equal in size, then it cannot be contained in pattern1
//		if(pattern1.size() <= pattern2.size()){
//			return false;
//		} 
		
		// To see if pattern2 is strictly contained in pattern1,
		// we will search for each itemset i of pattern2 in pattern1 by advancing
		// in pattern 1 one itemset at a time.
		
		int i =0; // position in pattern2
		int j= 0; // position in pattern1
		while(true){
			//if the itemset at current position in pattern1 contains the itemset
			// at current position in pattern2
			if(pattern1.get(j).containsAll(pattern2.get(i))){
				// go to next itemset in pattern2
				i++;
				
				// if we reached the end of pattern2, then return true
				if(i == pattern2.size()){
					return true;
				}
			}
				
			// go to next itemset in pattern1
			j++;
			
			// if we reached the end of pattern1, then pattern2 is not strictly included
			// in it, and return false
			if(j >= pattern1.size()){
				return false;
			}
			
//			// lastly, for optimization, we check how many itemsets are left to be matched.
//			// if there is less itemsets left in pattern1 than in pattern2, then it will
//			// be impossible to get a total match, and so we return false.
			if((pattern1.size() - j) < pattern2.size()  - i){
				return false;
			}
		}
	}

    /**
     * Print the statistics of the algorithm execution to System.out.
     */
    public void printStatistics() {
        StringBuilder r = new StringBuilder(200);
        r.append("=============  Algorithm VGEN - STATISTICS =============\n Total time ~ ");
        r.append(endTime - startTime);
        r.append(" ms\n");
        r.append(" Frequent sequences count : " + patternCount);
        r.append('\n');
        r.append(" Max memory (mb) : ");
        r.append(MemoryLogger.getInstance().getMaxMemory());
        r.append(patternCount);
        r.append('\n');
        r.append("minsup " + minsup);
        r.append('\n');
        r.append("Intersection count " + Bitmap.INTERSECTION_COUNT + " \n");
        r.append("===================================================\n");
        System.out.println(r.toString());
    }

    /**
     * Get the maximum length of patterns to be found (in terms of itemset
     * count)
     *
     * @return the maximumPatternLength
     */
    public int getMaximumPatternLength() {
        return maximumPatternLength;
    }

    /**
     * Set the maximum length of patterns to be found (in terms of itemset
     * count)
     *
     * @param maximumPatternLength the maximumPatternLength to set
     */
    public void setMaximumPatternLength(int maximumPatternLength) {
        this.maximumPatternLength = maximumPatternLength;
    }
    
	/**
	 * Write the result to an output file
	 * @param path the output file path
	 * @throws IOException exception if an error occur when writing the file.
	 */
	public void writeResultTofile(String path) throws IOException {
		// for each level (pattern having a same size)
		for(Map<Integer, List<PatternVGEN>> level : generatorPatterns) {
			// for each list of patterns having the same hash value
			for(List<PatternVGEN> patterns : level.values()) {
				// for each pattern
				for(PatternVGEN pattern : patterns) {
					// save the pattern
					StringBuffer r = new StringBuffer("");
					for(Itemset itemset : pattern.prefix.getItemsets()){
//						r.append('(');
						for(Integer item : itemset.getItems()){
							String string = item.toString();
							r.append(string);
							r.append(' ');
						}
						r.append("-1 ");
					}

					r.append("SUP: ");
					r.append(pattern.getAbsoluteSupport());
					
					writer.write(r.toString());
					writer.newLine();
				}
			}
		}
	}
}
