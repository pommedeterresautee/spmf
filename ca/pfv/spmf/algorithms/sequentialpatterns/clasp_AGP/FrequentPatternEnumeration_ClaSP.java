package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.abstracciones.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.ItemAbstractionPairCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.IDList;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.savers.Saver;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.tries.Trie;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.tries.TrieNode;

/**
 * This is an implementation of the main method of ClaSP algorithm. We can use
 * different kind of IdList although we only makes a implementation:
 * IDListStandard_Mapkeep. However, if we make another new IdList implementing
 * the IDList interface, we can define another different.
 *
 * NOTE: This implementation saves the pattern to a file as soon as they are
 * found or can keep the pattern into memory, depending on what the user choose.
 *
 * Copyright Antonio Gomariz Peñalver 2013
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
 *
 * @author agomariz
 */
public class FrequentPatternEnumeration_ClaSP {

    public long joinCount = 0;  // PFV 2013 - to count the number of intersections
    /**
     * The abstraction creator.
     */
    private AbstractionCreator abstractionCreator;
    /**
     * The absolute minimum support threshold, i.e. the minimum number of
     * sequences where the patterns have to be
     */
    private double minSupAbsolute;
    /**
     * Number of frequent patterns found by the algorithm. Initially set to
     * zero.
     */
    private int numberOfFrequentPatterns = 0;
    /**
     * Map in which we store the different patterns in order to know which ones
     * can be skipped because can be summarized by other ones (the closed
     * patterns)
     */
    private Map<Integer, Map<Integer, List<Entry<Pattern, Trie>>>> matchingMap;
    /**
     * Saver variable to decide where the user want to save the results, if it
     * the case
     */
    private Saver saver;
    /**
     * flag to indicate if we are interesting in only finding the closed
     * sequences
     */
    private boolean findClosedPatterns;
    /**
     * flag to indicate if we are interesting in only finding the closed
     * sequence through the postprocessing step
     */
    private boolean executePruningMethods;

    /**
     * Standard constructor
     *
     * @param abstractionCreator the abstraction creator
     * @param minSupAbsolute The absolute minimum support
     * @param saver The saver for correctly save the results where the user
     * wants
     * @param findClosedPatterns flag to indicate if we are interesting in only
     * finding the closed sequences
     */
    public FrequentPatternEnumeration_ClaSP(AbstractionCreator abstractionCreator, double minSupAbsolute, Saver saver, boolean findClosedPatterns, boolean executePruningMethods) {
        this.abstractionCreator = abstractionCreator;
        this.minSupAbsolute = minSupAbsolute;
        this.saver = saver;
        this.matchingMap = new HashMap<Integer, Map<Integer, List<Entry<Pattern, Trie>>>>();
        this.findClosedPatterns = findClosedPatterns;
        this.executePruningMethods = executePruningMethods;
    }

    /**
     * Execution of the search of frequent patterns.
     *
     * @param equivalenceClass The equivalence class from we start to search
     * for.
     * @param keepPatterns Flag to indicate if we want to keep the patterns
     * found.
     * @param verbose Flag for debugging purposes
     * @param coocMapBefore
     * @param
     */
    public void dfsPruning(Pattern patron, Trie trie, boolean verbose, Map<Integer, Map<Integer, Integer>> coocMapAfter, Map<Integer, Map<Integer, Integer>> coocMapEquals) {
        int tam = trie.levelSize();
        for (int i = 0; i < tam; i++) {
            //For each frequent item (the children of the root Trie)
            TrieNode eq = trie.getNode(i);
            /*
             * We call to the main method of the algorithm for that Trie 
             * associated with the frequent item
             */
            exploreChildren(new Pattern(eq.getPair()), eq, trie.getNodes(), trie.getNodes(), i + 1, coocMapAfter, coocMapEquals, eq.getPair().getItem());
        }
    }

    private void exploreChildren(Pattern pattern, TrieNode currentNode, List<TrieNode> sequenceExtensions, List<TrieNode> itemsetsExtensions, int beginning, Map<Integer, Map<Integer, Integer>> coocMapAfter, Map<Integer, Map<Integer, Integer>> coocMapEquals, Item lastAppendedItem) {


        //We get the curretn trie
        Trie currentTrie = currentNode.getChild();
        /*
         * And if we are interested in find the closed patterns, we check if the 
         * generation of the Trie of the current pattern can be avoided
         */
        if (findClosedPatterns && executePruningMethods) {
            if (isAvoidable(pattern, currentTrie)) {
                return;
            }
        }

        //We start increasing the number of frequent patterns
        numberOfFrequentPatterns++;

        //Initialization of new sets
        List<TrieNode> new_sequenceExtension = new ArrayList<TrieNode>();
        List<TrieNode> new_itemsetExtension = new ArrayList<TrieNode>();
        List<Pattern> newPatterns = new ArrayList<Pattern>();
        List<TrieNode> newNodesToExtends = new ArrayList<TrieNode>();

        //Clone for the current pattern
        Pattern clone = pattern.clonePatron();
        //For all the nodes of sequenceExtensions
        loops:
        for (TrieNode node : sequenceExtensions) {
            //node.getPair().getItem();
            //lastAppendedItem
            // ====== PFV 2013 =========================

            if (coocMapAfter != null) {
                Map<Integer, Integer> map = coocMapAfter.get(lastAppendedItem.getId());
                if (map != null) {
                    Integer coocurenceCount = map.get(node.getPair().getItem().getId());
                    if (coocurenceCount == null || coocurenceCount < minSupAbsolute) {
                        continue loops;
                    }
                } else {
                    continue loops;
                }
            }
            // ====== FIN PFV 2013 =========================

            //We create a new pattern based in the elements of the clone
            Pattern extension = new Pattern(new ArrayList<ItemAbstractionPair>(clone.getElements()));
            //And we extend it with the only element of the eq class identifier
            ItemAbstractionPair newPair = node.getPair();
            extension.add(newPair);

            /*
             * We make the join operation between the tries of both patterns in 
             * order to know the appearances of the new pattern and its support.
             */
            joinCount++;
            IDList newIdList = currentTrie.getIdList().join(node.getChild().getIdList(), false, (int) minSupAbsolute);
            //If the new pattern is frequent
            if (newIdList.getSupport() >= minSupAbsolute) {
                //We create a new trie for it
                Trie newTrie = new Trie(null, newIdList);
                //abd we insert it its appearances
                newIdList.setAppearingIn(newTrie);

                //we put in a TrieNode the new pair and the new Trie created
                TrieNode newTrieNode = new TrieNode(newPair, newTrie);
                //And we merge the new Trie with the current one
                currentTrie.mergeWithTrie(newTrieNode);

                /*
                 * Finally we add the new pattern and nodeTrie to the sets that 
                 * are needed for future patterns
                 */
                newPatterns.add(extension);
                newNodesToExtends.add(newTrieNode);
                new_sequenceExtension.add(newTrieNode);
            }
        }

        int sequenceExtensionSize = new_sequenceExtension.size();
        //For all the elements valuables as future s-extensions
        for (int i = 0; i < sequenceExtensionSize; i++) {
            //we get the new pattern and the nodeTrie associated with it
            Pattern newPattern = newPatterns.get(i);
            TrieNode nodeToExtend = newNodesToExtends.remove(0);

            Item last = newPattern.getIthElement(newPattern.size() - 1).getItem(); // PFV 2013

            /* And we make a recursive call to dfs_pruning with the new sequence 
             * extension. Besides we establish the same set as the set which we will
             * make the i-extensions, but beginning from the (i+1)-th element
             */
            exploreChildren(newPattern, nodeToExtend, new_sequenceExtension, new_sequenceExtension, i + 1, coocMapAfter, coocMapEquals, last);
        }

        /* We clear the set of the new pattern and the nodes associated with 
         * them since discovered since we need to store those that we will find 
         * making i-extensions
         */
        newPatterns.clear();
        newNodesToExtends.clear();

        /*
         * From the beginning index to the last equivalence class appearing in 
         * the itemset extension set
         */
        loopi:
        for (int k = beginning; k < itemsetsExtensions.size(); k++) {


            TrieNode eq = itemsetsExtensions.get(k);

            // ====== PFV 2013 =========================
            if (coocMapEquals != null) {
                Map<Integer, Integer> map = coocMapEquals.get(lastAppendedItem.getId());
                if (map != null) {
                    Integer coocurenceCount = map.get(eq.getPair().getItem().getId());
                    if (coocurenceCount == null || coocurenceCount < minSupAbsolute) {
                        continue loopi;
                    }
                } else {
                    continue loopi;
                }
            }
            // ====== FIN PFV 2013 =========================

            // eq.getPair().getItem()  // the item to be appended.
            //We create a new pattern with the elements of the current pattern
            Pattern extension = new Pattern(new ArrayList<ItemAbstractionPair>(clone.getElements()));
            //And we add it the current item of itemset extension set
            ItemAbstractionPair newPair = ItemAbstractionPairCreator.getInstance().getItemAbstractionPair(eq.getPair().getItem(), AbstractionCreator_Qualitative.getInstance().crearAbstraccion(true));
            extension.add(newPair);

            /*
             * We make the join operation between the tries of both patterns in 
             * order to know the appearances of the new pattern and its support.
             */
            joinCount++;
            IDList newIdList = currentTrie.getIdList().join(eq.getChild().getIdList(), true, (int) minSupAbsolute);
            //If the new pattern is frequent
            if (newIdList.getSupport() >= minSupAbsolute) {
                //We create a new trie for it
                Trie newTrie = new Trie(null, newIdList);
                //And we insert it its appearances
                newIdList.setAppearingIn(newTrie);

                //we put in a TrieNode the new pair and the new Trie created
                TrieNode newTrieNode = new TrieNode(newPair, newTrie);
                //And we merge the new Trie with the current one
                currentTrie.mergeWithTrie(newTrieNode);

                /*
                 * Finally we add the new pattern and nodeTrie to the sets that 
                 * are needed for future patterns
                 */
                newPatterns.add(extension);
                newNodesToExtends.add(newTrieNode);
                new_itemsetExtension.add(newTrieNode);
            }
        }

        int itemsetExtensionSize = new_itemsetExtension.size();
        //For all the elements valuables as future i-extensions
        for (int i = 0; i < itemsetExtensionSize; i++) {
            //we get the new pattern and the nodeTrie associated with it
            Pattern newPattern = newPatterns.get(i);
            TrieNode nodeToExtend = newNodesToExtends.remove(0);

            Item last = newPattern.getIthElement(newPattern.size() - 1).getItem(); // PFV 2013


            /* And we make a recursive call to dfs_pruning with the new itemset 
             * extension. Besides we establish the same set as the set which we will
             * make the i-extensions, but beginning from the (i+1)-th element
             */
            exploreChildren(newPattern, nodeToExtend, new_sequenceExtension, new_itemsetExtension, i + 1, coocMapAfter, coocMapEquals, last);
            nodeToExtend.getChild().setIdList(null);

            /*
             * If all the elements of itemsetExtensions have been used, we can 
             * clear the IdList of the current Node
             */
            if (i == itemsetExtensionSize) {
                for (TrieNode nodo : new_sequenceExtension) {
                    nodo.getChild().setIdList(null);
                }
            }
        }
        
    }

    /**
     * It returns the number of frequent patterns found by the last execution of
     * the algorithm.
     *
     * @return
     */
    public int getFrequentPatterns() {
        return numberOfFrequentPatterns;
    }

    public void setPatronesFrecuentes(int patronesFrecuentes) {
        this.numberOfFrequentPatterns = patronesFrecuentes;
    }

    /**
     * Method that checks if the prefix given as parameter can be skipped by
     * means of prune methods backward subpattern or backward superpattern. The
     * method uses a map where the different patterns are kept in order to check
     * both pruning methods. The hash keys used can vary, and we give some
     * aproaches by the methods:
     *
     * key_standard() key_standardAndSupport() key_standardAndSumIDs()
     * key_standardAndCumulativeSum() Key_standardAndElements()
     *
     * @param prefix Current pattern which is going to be checked
     * @param trie Trie associated with prefix
     * @return
     */
    private boolean isAvoidable(Pattern prefix, Trie trie) {
        //We get the support of the pattern
        int support = trie.getSupport();
        //We get the IdList of the pattern
        IDList idList = trie.getIdList();
        /* 
         * We get as a first key the sum of all sequences identifiers where the 
         * current prefix appear
         */
        int key1 = trie.getSumIdSequences();
        int prefixSize = prefix.size();

        /*
         * Different approaches for the key2 can be used
         */
        int key2 = key2(idList, trie);

        /* 
         * We make a new entry associating the current prefix with its 
         * corresponding prefixTrie
         */
        Entry<Pattern, Trie> newEntry = new AbstractMap.SimpleEntry<Pattern, Trie>(prefix, trie);

        /* 
         * Map where there appear all the patterns with the same key1 of the 
         * current prefix, that makes a correspondence between a value given by
         * key2 and all the patterns that have it
         */
        Map<Integer, List<Entry<Pattern, Trie>>> associatedMap = matchingMap.get(key1);
        /* 
         * If there is not any pattern with the same key2 value, we add the current
         * prefix as a new entry, and we also insert it in the matching map
         */
        if (associatedMap == null) {
            associatedMap = new HashMap<Integer, List<Entry<Pattern, Trie>>>();
            List entryList = new ArrayList<Pattern>();
            entryList.add(newEntry);
            associatedMap.put(key2, entryList);
            matchingMap.put(key1, associatedMap);
        } else {
            /* 
             * If, conversely, there are some patterns with the same key2 value 
             * (and extensively with the same key1 value) we check if we can apply
             * backward subpattern or backward superpattern pruning
             */

            //We get the list of entries
            List<Entry<Pattern, Trie>> associatedList = associatedMap.get(key2);
            //If is still empty, we create one
            if (associatedList == null) {
                associatedList = new ArrayList<Entry<Pattern, Trie>>();
                associatedList.add(newEntry);
                associatedMap.put(key2, associatedList);
            } else {
                int i = 0;
                int superPattern = 0;
                for (i = 0; i < associatedList.size(); i++) {
                    //For all the elements of the associated list
                    Entry<Pattern, Trie> storedEntry = associatedList.get(i);
                    //We get both pattern and trie from the entry
                    Pattern p = storedEntry.getKey();
                    Trie t = storedEntry.getValue();
                    //If the support of the current prefix and the p pattern are equal
                    if (support == t.getSupport()) {
                        //We keep the size of the pattern
                        int pSize = p.size();
                        if (pSize != prefixSize) {
                            //if the prefix size is less than the size of p
                            if (prefixSize < pSize) {
                                //and prefix is a subpattern of p
                                if (prefix.isSubpattern(abstractionCreator, p)) {
                                    /* 
                                     * We dfsPruning backward subpattern pruning and 
                                     * establish as new nodes the nodes of the trie
                                     * of p
                                     */
                                    trie.setNodes(t.getNodes());
                                    /*
                                     * We end the method since we have already 
                                     * done the prune
                                     */
                                    return true;
                                }
                            } else if (p.isSubpattern(abstractionCreator, prefix)) {
                                /*
                                 * if, conversely, the prefix size is greater than 
                                 * the size of p and prefix is a superpattern of p
                                 */

                                //we update a counter of superpatterns
                                superPattern++;
                                /* 
                                 * and we make the prefix trie point to the nodes
                                 * of the trie of p
                                 */
                                trie.setNodes(t.getNodes());
                                /*
                                 * and we make null the nodes of t since p is 
                                 * included in prefix
                                 */
                                //t.setNodes(null);
                                //And we remove the entry of the list
                                associatedList.remove(i);
                                i--;
                            }
                        }
                    }
                }
                //In this point we add the new entry of the current prefix
                associatedList.add(newEntry);
                //If we found any superPattern
                if (superPattern > 0) {
                    /*if (superPattern > 1) {
                     System.out.println("We removed more than one pattern!!");
                     }*/
                    //We return the correspondent output
                    return true;
                }
            }
        }
        /* 
         * We did not find any subpattern or supperpattern in order to skip the 
         * generation of the current prefix
         */
        return false;
    }

    /**
     * Method used to obtain the value for the second key of matchingMap.
     *
     * @param idlist
     * @param t
     * @return
     */
    private int key2(IDList idlist, Trie t) {
        /*
         * If you are interested in changing the method, just comment the line
         * of below and uncomment one of the others
         */
        return FrequentPatternEnumeration_ClaSP.key_standardAndSupport(idlist, t);
        //return FrequentPatternEnumeration_ClaSP.key_standard(idlist);
        //return FrequentPatternEnumeration_ClaSP.key_standardAndSumIDs(idlist, t);
    }

    /**
     * One of the methods used by key2 in the method isAvoidable that return the
     * number of elements that appear in the projected database
     *
     * @param idList IdList of the prefix to consider
     * @return
     */
    private static int key_standard(IDList idList) {
        return idList.getTotalElementsAfterPrefixes();
    }

    /**
     * One of the methods used by key2 in the method isAvoidable that return the
     * addition of the number of elements that appear in the projected database
     * and the support of the related prefix
     *
     * @param idList IdList of the prefix to consider
     * @param trie Trie of the pattern to consider
     * @return
     */
    private static int key_standardAndSupport(IDList projection, Trie trie) {
        return projection.getTotalElementsAfterPrefixes() + trie.getSupport();
    }

    /**
     * One of the methods used by key2 in the method isAvoidable that return the
     * addition of the number of elements that appear after of each appearance
     * of the pattern in the sequences and sum of the sequence identifiers where
     * the given prefix appears
     *
     * @param idList IdList of the prefix to consider
     * @param trie Trie of the pattern to consider
     * @return
     */
    private static int key_standardAndSumIDs(IDList idList, Trie trie) {
        return (idList.getTotalElementsAfterPrefixes() + trie.getSumIdSequences());
    }

    /**
     * It removes the non closed patterns from the list of patterns given as
     * parameter
     *
     * @param frequentPatterns List of patterns from which we want to remove the
     * non-closed patterns
     * @param keepPatterns Flag indicating if we want to keep the final output
     */
    void removeNonClosedPatterns(List<Entry<Pattern, Trie>> frequentPatterns, boolean keepPatterns) {
        System.err.println("Before removing NonClosed patterns there are " + numberOfFrequentPatterns + " patterns");
        numberOfFrequentPatterns = 0;

        /* 
         * We make a map to match group of patterns linked by their addition of 
         * sequence identifiers
         */
        Map<Integer, List<Pattern>> totalPatterns = new HashMap<Integer, List<Pattern>>();
        //and we classify the patterns there by their sumIdSequences number
        for (Entry<Pattern, Trie> entrada : frequentPatterns) {
            Pattern p = entrada.getKey();
            Trie t = entrada.getValue();
            p.setAppearingIn(t.getAppearingIn());
            List<Pattern> listaPatrones = totalPatterns.get(t.getSumIdSequences());
            if (listaPatrones == null) {
                listaPatrones = new LinkedList<Pattern>();
                totalPatterns.put(t.getSumIdSequences(), listaPatrones);
            }
            listaPatrones.add(p);
        }

        //For all the list associated with de different sumSequencesIDs values
        for (List<Pattern> lista : totalPatterns.values()) {
            //For all their patterns
            for (int i = 0; i < lista.size(); i++) {
                for (int j = i + 1; j < lista.size(); j++) {
                    Pattern p1 = lista.get(i);
                    Pattern p2 = lista.get(j);
                    //If the patterns has the same support
                    if (p1.getAppearingIn().size() == p2.getAppearingIn().size()) {
                        if (p1.size() != p2.size()) {
                            /* 
                             * And one is subpattern of the other, we remove the 
                             * shorter pattern and keep the longer one
                             */
                            if (p1.size() < p2.size()) {
                                if (p1.isSubpattern(abstractionCreator, p2)) {
                                    lista.remove(i);
                                    i--;
                                    break;
                                }
                            } else {
                                if (p2.isSubpattern(abstractionCreator, p1)) {
                                    lista.remove(j);
                                    j--;
                                }
                            }
                        }
                    }
                }
            }
        }
        /*
         * We calcule the number of frequent patterns and we store in the chosen 
         * output if the flag is activated
         */
        for (List<Pattern> list : totalPatterns.values()) {
            numberOfFrequentPatterns += list.size();
            if (keepPatterns) {
                for (Pattern p : list) {
                    saver.savePattern(p);
                }
            }
        }

    }

    public void clear() {
        if (matchingMap != null) {
            matchingMap.clear();
            matchingMap = null;
        }
    }
}