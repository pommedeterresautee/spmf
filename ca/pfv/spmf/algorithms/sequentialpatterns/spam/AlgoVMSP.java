package ca.pfv.spmf.algorithms.sequentialpatterns.spam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * *
 * This is an implementation of the VMSP algorithm.
 * <br/><br/>
 * 
 * Copyright (c) 2013 Philippe Fournier-Viger, Antonio Gomariz
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
*  @see PrefixVMSP
*  @see PatternVMSP
*  @author Philippe Fournier-Viger  & Antonio Gomariz
 */
public class AlgoVMSP {

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
    Map<Integer, Short> lastItemPositionMap;
    boolean useCMAPPruning = true;
    boolean useLastPositionPruning = false;
    
    // MAXPATTERNS  --  CHANGED
    //  PATTERNS ARE STORED BY ASCENDING ORDER OF SUPPORT
    List<TreeSet<PatternVMSP>> maxPatterns = null; 
    // END- CHANGED
	private boolean useStrategyForwardExtensionChecking = true;
	
//	private double sequenceToUse = 0.10; // 10 %

    /**
     * Default constructor
     */
    public AlgoVMSP() {
    }

    /**
     * Method to run the algorithm
     *
     * @param input path to an input file
     * @param outputFilePath path for writing the output file
     * @param minsupRel the minimum support as a relative value
     * @throws IOException exception if error while writing the file or reading
     */
    public List<TreeSet<PatternVMSP>> runAlgorithm(String input, String outputFilePath, double minsupRel) throws IOException {
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
        vmsp(input, minsupRel);
        // record end time
        endTime = System.currentTimeMillis();
        // save result to the file
        writeResultTofile(outputFilePath);
        // close the file
        writer.close();
        
        // PRINT PATTTERNS FOUND
//        for(TreeSet<Pattern> tree : maxPatterns) {
//        	if(tree == null) {
//        		continue;
//        	}
//        	for(Pattern pat : tree) {
////        		System.out.println(" " + pat.prefix);
//        	}
//        }
        return maxPatterns;
    }

    /**
     * This is the main method for the VMSP algorithm
     *
     * @param an input file
     * @param minsupRel the minimum support as a relative value
     * @throws IOException
     */
    private void vmsp(String input, double minsupRel) throws IOException {
    	// create maxPattern array
    	maxPatterns = new ArrayList<TreeSet<PatternVMSP>>(20);
        maxPatterns.add(null);
    	maxPatterns.add(new TreeSet<PatternVMSP>());
    	
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
//		minsup = 163;
        minsup = (int) Math.ceil((minsupRel * sequencesSize.size()));
        if (minsup == 0) {
            minsup = 1;
        }
//        System.out.println("minsup : " + minsup);

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
				// TODO Auto-generated method stub
				return verticalDB.get(arg0).getSupport() - verticalDB.get(arg1).getSupport();
			}
        	
        });

        // STEP 3.1  CREATE CMAP
        coocMapEquals = new HashMap<Integer, Map<Integer, Integer>>(frequentItems.size());
        coocMapAfter = new HashMap<Integer, Map<Integer, Integer>>(frequentItems.size());

        if (useLastPositionPruning) {
            lastItemPositionMap = new HashMap<Integer, Short>(frequentItems.size());
        }
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
//				System.out.println(itemsetCount);

                // update lastItemMap
                if (useLastPositionPruning) {
                    Short last = lastItemPositionMap.get(itemI);
                    if (last == null || last < itemsetCount) {
                        lastItemPositionMap.put(itemI, itemsetCount);
                    }
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
        // for each frequent item
        for (Entry<Integer, Bitmap> entry : verticalDB.entrySet()) {
            // We create a prefix with that item
            PrefixVMSP prefix = new PrefixVMSP();
            prefix.addItemset(new Itemset(entry.getKey()));
            boolean itemIsEven = entry.getKey() % 2 == 0;
            if(itemIsEven) {
            	prefix.sumOfEvenItems = (Integer)entry.getKey();
            	prefix.sumOfOddItems = 0;
            }else {
            	prefix.sumOfEvenItems = 0;
            	prefix.sumOfOddItems = (Integer)entry.getKey();
            }
           
           boolean hasExtension =  dfsPruning(prefix, entry.getValue(), frequentItems, frequentItems, entry.getKey(), 2, entry.getKey());

           if(hasExtension == false) {
        	   savePatternSingleItem(entry.getKey(), entry.getValue(), itemIsEven);
           }
            
            // We call the depth first search method with that prefix
            // and the list of frequent items to try to find
            // larger sequential patterns by appending some of these
            // items.
//            if(!isSubsumedAndNonClosed) {
            	//            }
         }
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
    boolean dfsPruning(PrefixVMSP prefix, Bitmap prefixBitmap, List<Integer> sn, List<Integer> in, int hasToBeGreaterThanForIStep, int m, Integer lastAppendedItem) throws IOException {
    	boolean atLeastOneFrequentExtension = false;
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
            atLeastOneFrequentExtension = true;
            
            int item = sTemp.get(k);
            // create the new prefix
            PrefixVMSP prefixSStep = prefix.cloneSequence();
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
           boolean hasFrequentExtension = false;
            // recursively try to extend that pattern
            if (maximumPatternLength > m) {
                dfsPruning(prefixSStep, newBitmap, sTemp, sTemp, item, m + 1, item);
            }
            
            if(hasFrequentExtension == false) {
                savePatternMultipleItems(prefixSStep, newBitmap, m);
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
            atLeastOneFrequentExtension = true;
            
            int item = iTemp.get(k);
            // create the new prefix
            PrefixVMSP prefixIStep = prefix.cloneSequence();
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

            // recursively try to extend that pattern
            boolean hasFrequentExtension = false;
            if (maximumPatternLength > m) {
            	hasFrequentExtension = dfsPruning(prefixIStep, newBitmap, sTemp, iTemp, item, m + 1, item);
            }
            
            if(hasFrequentExtension == false) {
                // save the pattern
                savePatternMultipleItems(prefixIStep, newBitmap, m);
            }
        }
        // check the memory usage
        MemoryLogger.getInstance().checkMemory();
        
        return atLeastOneFrequentExtension 
        		|| useStrategyForwardExtensionChecking == false;
    }

    /**
     * Save a pattern of size 1 to the output file
     *
     * @param item the item
     * @param bitmap its bitmap
     * @param itemIsEven 
     * @throws IOException exception if error while writing to the file
     * @return true if is subsumed
     */
    private boolean savePatternSingleItem(Integer item, Bitmap bitmap, boolean itemIsEven) throws IOException {
//    	System.out.println("prefix :" + prefix);
    	
        // FOR THE CASE OF SINGLE ITEM, WE DON'T NEED TO DO SUB-PATTERN CHECKING:
        // WE JUST NEED TO DO SUPER-PATTERN CHECKING
    	// #################
    	// IMPORTANT STRATEGY  :   FROM LARGER TO SMALLER......  AND IN ASCENDING SUPPORT ORDER
    	// ##################
    	if(itemIsEven) {
	    	for(int i=maxPatterns.size()-1; i > 1; i--) {
	    		for(PatternVMSP pPrime :  maxPatterns.get(i)) {
	    			
		        	if(pPrime.prefix.sumOfOddItems 
		        			+ pPrime.prefix.sumOfEvenItems < item) {
		        		break;
		        	}
	    			// if the pattern already found contains the single item 
		        	if(pPrime.prefix.sumOfEvenItems > item &&
		        			bitmap.getSupport() >= pPrime.support) {
		        		if(pPrime.prefix.containsItem(item)) {
			        		return true;
		        		}
		        	}
				}
	        }
    	}else {
    		for(int i=maxPatterns.size()-1; i > 1; i--) {
	    		for(PatternVMSP pPrime :  maxPatterns.get(i)) {
	    			
		        	if(pPrime.prefix.sumOfOddItems 
		        			+ pPrime.prefix.sumOfEvenItems < item) {
		        		break;
		        	}
	    			// if the pattern already found contains the single item 
		        	if(pPrime.prefix.sumOfOddItems > item &&
		        			bitmap.getSupport() >= pPrime.support) {
		        		if(pPrime.prefix.containsItem(item)) {
			        		return true;
		        		}
		        	}
				}
	        }
    	}
        // OTHERWISE THE NEW PATTERN IS NOT SUBSUMMED
        patternCount++;  // INCREASE COUNT
        PrefixVMSP prefix = new PrefixVMSP();
        prefix.addItemset(new Itemset(item));
        if(itemIsEven) {
        	 prefix.sumOfEvenItems = item;
        	 prefix.sumOfOddItems = 0;
        }else{
	       	 prefix.sumOfEvenItems = 0;
	       	 prefix.sumOfOddItems = item;
       }
        
        PatternVMSP newPat = new PatternVMSP(prefix, bitmap.getSupport());
//        System.out.println(" ADD: " + item);
        maxPatterns.get(1).add(newPat);
		
		return false;

        // END CHANGED ------
    }
    
	

    /**
     * Save a pattern of size > 1 to the output file.
     *
     * @param prefix the prefix
     * @param bitmap its bitmap
     * @throws IOException exception if error while writing to the file
     * @return true if pattern is subsumed
     */
    private boolean savePatternMultipleItems(PrefixVMSP prefix, Bitmap bitmap, int length) throws IOException {
        // CHANGED ------
//    	System.out.println("prefix :" + prefix);
//    	if(true == true) return false;
        // WE COMPARE WITH LARGER PATTERNS FOR SUPER-PATTERN CHECKING
    	// #################
    	// IMPORTANT STRATEGY  :   FROM LARGER TO SMALLER......
    	// ##################
    	for(int i=maxPatterns.size()-1; i > length; i--) {
    		for(PatternVMSP pPrime :  maxPatterns.get(i)) {
//    			System.out.println(pPrime.prefix.sumOfItems);
	        	// if the prefix pattern has a support higher or equal to the current pattern
    			if(pPrime.prefix.sumOfOddItems + pPrime.prefix.sumOfEvenItems < prefix.sumOfOddItems + prefix.sumOfEvenItems) {
	        		break;
	        	}
    			
    			if(prefix.sumOfEvenItems <= pPrime.prefix.sumOfEvenItems &&
    			   prefix.sumOfOddItems <= pPrime.prefix.sumOfOddItems &&
    			   bitmap.getSupport() >= pPrime.support && 
    			   strictlyContains(pPrime.prefix, prefix)) {
	        			return true;
	        	}

    		}
    	}

//        System.out.println("  ADD : " + prefix);
    	
     // WE COMPARE WITH SMALLER PATTERNS FOR SUB-PATTERN CHECKING
    	for(int i=1; i < length && i < maxPatterns.size(); i++) {
	        // for each pattern already found of size i
    		
    		// IMPORTANT : WE USE A DESCENDNIG ITERATOR...
    		// HOWEVER' WE COULD USE THIS FOR PRUNING...
    		Iterator<PatternVMSP> iter  = maxPatterns.get(i).descendingIterator();  // DESCENDING ITERATOR !!!!!!!!!!
	        while (iter.hasNext()) {
	        	PatternVMSP pPrime = iter.next();
	        	
	        	// CAN DO THIS BECAUSE OF DESCENDING ORDER
	        	if(pPrime.prefix.sumOfOddItems + pPrime.prefix.sumOfEvenItems >= prefix.sumOfOddItems + prefix.sumOfEvenItems) {
	        		break;
	        	}
	        	
	        	if(prefix.sumOfEvenItems >= pPrime.prefix.sumOfEvenItems &&
	     			   prefix.sumOfOddItems >= pPrime.prefix.sumOfOddItems &&
	     			   bitmap.getSupport() <= pPrime.support
	        		&& strictlyContains(prefix, pPrime.prefix)) {
		        		patternCount--;  // DECREASE COUNT
		        		iter.remove();
	        	}
			}
        }
       
        // OTHERWISE THE NEW PATTERN IS NOT SUBSUMMED

        while(maxPatterns.size() -1 < length) {
        	maxPatterns.add(new TreeSet<PatternVMSP>());
        }
        
        TreeSet<PatternVMSP> patternsOfSizeM = maxPatterns.get(length);

        patternCount++;  // INCREASE COUNT
        patternsOfSizeM.add(new PatternVMSP(prefix, bitmap.getSupport()));

//        if(patternCount % 200 == 0) {
//        	System.out.println(patternCount);
//        }
        return false;  // not subsumed
        

//        StringBuilder r = new StringBuilder("");
//        for (Itemset itemset : prefix.getItemsets()) {
////			r.append('(');
//            for (Integer item : itemset.getItems()) {
//                String string = item.toString();
//                r.append(string);
//                r.append(' ');
//            }
//            r.append("-1 ");
//        }
//
//        r.append("SUP: ");
//        r.append(bitmap.getSupport());
//
//        writer.write(r.toString());
////		System.out.println(r.toString());
//        writer.newLine();
        
     // END CHANGED ------
    }
    
    /**
	 * This methods checks if a seq. pattern "pattern2" is strictly contained in a seq. pattern "pattern1".
     * @param pattern1 a sequential pattern
     * @param pattern2 another sequential pattern
	 * @return true if the pattern1 contains pattern2.
	 */
	boolean strictlyContains(PrefixVMSP pattern1, PrefixVMSP pattern2) {
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
        r.append("=============  Algorithm VMSP - STATISTICS =============\n Total time ~ ");
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
        
//        // PRINT PATTERNS
//        System.out.println("PATTERNS FOUND ===============");
//        for(Entry<Integer, List<Pattern>> entry : maxPatterns.entrySet()) {
//        	for(Pattern  pat1: entry.getValue()) {
//        		System.out.println(pat1.prefix.toString());
//        		 
//        		for(Entry<Integer, List<Pattern>> entry2 : maxPatterns.entrySet()) {
//        	        	for(Pattern  pat2: entry2.getValue()) {
//        	        		if(pat1 != pat2 && strictlyContains(pat1.prefix, pat2.prefix)) {
//        	        			System.out.println("REDUNDANT : " + pat1.prefix + "   " + pat2.prefix);
//        	        		}
//
//        	        	}
//    	        }
//        	}
//        }
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
      for(TreeSet<PatternVMSP> tree : maxPatterns) {
    	  if(tree == null) {
    		  continue;
    	  }
    	  // for each pattern
    	  for(PatternVMSP pattern : tree) {
    		// save the pattern
				StringBuffer r = new StringBuffer("");
				for(Itemset itemset : pattern.prefix.getItemsets()){
//					r.append('(');
					for(Integer item : itemset.getItems()){
						String string = item.toString();
						r.append(string);
						r.append(' ');
					}
					r.append("-1 ");
				}

				r.append("SUP: ");
				r.append(pattern.support);
				
				writer.write(r.toString());
				writer.newLine(); 
    		  //
    	  }
      }
	}
}
