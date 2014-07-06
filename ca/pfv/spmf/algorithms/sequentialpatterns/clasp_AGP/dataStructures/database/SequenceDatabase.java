package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.ItemFactory;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.ItemAbstractionPairCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.IDList;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.tries.Trie;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.tries.TrieNode;

/**
 * Inspired in SPMF. Implementation of a sequence database. Each sequence should
 * have a unique id. See examples in /test/ directory for the format of input
 * files.
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
public class SequenceDatabase{

    private AbstractionCreator abstractionCreator;
    private IdListCreator idListCreator;
    private Map<Item, TrieNode> frequentItems = new HashMap<Item, TrieNode>();
    private List<Sequence> sequences = new ArrayList<Sequence>();
    private ItemFactory<Integer> itemFactory = new ItemFactory<Integer>();
    private int nSequences = 1;
    /**
     * Map where we keep the original length for all the sequences
     */
    private Map<Integer, Integer> sequencesLengths = new HashMap<Integer, Integer>();
    /**
     * Map where, for each sequence, we have a list of integers corresponding 
     * to all the sizes of all the itemsets that the sequence has
     */
    private Map<Integer, List<Integer>> sequenceItemsetSize = new HashMap<Integer, List<Integer>>();
    /**
     * For each item, we match it with a map of entries <sequence id, number of elements after item>.
     * We will use this map in order to maintain the values necessaries for making the pruning methods.
     */
    private Map<Item, Map<Integer, List<Integer>>> projectingDistance = new HashMap<Item, Map<Integer, List<Integer>>>();

    /**
     * Standard constructor
     * @param abstractionCreator
     * @param IdListCreator 
     */
    public SequenceDatabase(AbstractionCreator abstractionCreator, IdListCreator IdListCreator) {
        this.abstractionCreator = abstractionCreator;
        this.idListCreator = IdListCreator;
    }

    /**
     * Method that load a database from a path file given as parameter
     *
     * @param path Path file where the database is
     * @param minSupport Minimum absolute support
     * @throws IOException
     */
    public double loadFile(String path, double minSupport) throws IOException {
        String thisLine;
        BufferedReader myInput = null;
        try {
            FileInputStream fin = new FileInputStream(new File(path));
            myInput = new BufferedReader(new InputStreamReader(fin));
            //For each line
            while ((thisLine = myInput.readLine()) != null) {
                // If the line is not a comment line
                if (thisLine.charAt(0) != '#') {
                    // we add a new sequence to the sequenceDatabase
                    addSequence(thisLine.split(" "));
                }
            }
            double support = (int) Math.ceil(minSupport * sequences.size());
            Set<Item> frequentItemsSet = frequentItems.keySet();
            Set<Item> itemsToRemove = new HashSet<Item>();
            //We remove those items that are not frequent
            for (Item frequentItem : frequentItemsSet) {
                //From the item set of frequent items
                TrieNode nodo = frequentItems.get(frequentItem);
                if (nodo.getChild().getIdList().getSupport() < support) {
                    itemsToRemove.add(frequentItem);
                } else {
                    nodo.getChild().getIdList().setAppearingIn(nodo.getChild());
                }
            }
            
            for(Item item: itemsToRemove){
                frequentItems.remove(item);
            }
            //And from the original database
            reduceDatabase(frequentItems.keySet());

            /*
             * We initialize all the maps
             */
            idListCreator.initializeMaps(frequentItems, projectingDistance, sequencesLengths, sequenceItemsetSize/*, itemsetTimestampMatching*/);
            return support;
        } catch (Exception e) {
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
        return -1;

    }
    
    /**
     * Method that adds a sequence from a array of string
     *
     * @param integers
     */
    public void addSequence(String[] integers) {
        ItemAbstractionPairCreator pairCreator = ItemAbstractionPairCreator.getInstance();
        long timestamp = -1;
        Sequence sequence = new Sequence(sequences.size());
        Itemset itemset = new Itemset();
        sequence.setID(nSequences);
        nSequences++;

        int beginning = 0;

        List<Integer> sizeItemsetsList = new ArrayList<Integer>();

        for (int i = beginning; i < integers.length; i++) {
            if (integers[i].codePointAt(0) == '<') {  // Timestamp
                String value = integers[i].substring(1, integers[i].length() - 1);
                timestamp = Long.parseLong(value);
                itemset.setTimestamp(timestamp);
            } else if (integers[i].equals("-1")) { // End of an Itemset
                //insertMatchItemsetTimestamp(nSequences, sequence.size(), timestamp);
                timestamp = itemset.getTimestamp() + 1;
                sequence.addItemset(itemset);
                itemset = new Itemset();
                itemset.setTimestamp(timestamp);
                sizeItemsetsList.add(sequence.length());

            } else if (integers[i].equals("-2")) { // End of a sequence
                sequences.add(sequence);
                nSequences++;
                sequencesLengths.put(sequence.getId(), sequence.length());
                sequenceItemsetSize.put(sequence.getId(), sizeItemsetsList);
            } else { // an item with the format : id(value)  ou:  id
                int indexParentheseGauche = integers[i].indexOf("(");
                if (indexParentheseGauche != -1) {
                } else {
                    // extract the value for an item
                    Item item = itemFactory.getItem(Integer.parseInt(integers[i]));
                    TrieNode node = frequentItems.get(item);
                    if (node == null) {
                        IDList idlist = idListCreator.create();
                        node = new TrieNode(pairCreator.getItemAbstractionPair(item, abstractionCreator.createDefaultAbstraction()), new Trie(null, idlist));
                        frequentItems.put(item, node);
                    }
                    IDList idlist = node.getChild().getIdList();
                    if (timestamp < 0) {
                        timestamp = 1;
                        itemset.setTimestamp(timestamp);
                    }
                    itemset.addItem(item);
                    idListCreator.addAppearance(idlist, sequence.getId(), (int) timestamp, sequence.length() + itemset.size());
                    idListCreator.updateProjectionDistance(projectingDistance, item, sequence.getId(), sequence.size(), sequence.length() + itemset.size());
                }
            }
        }
    }

    public void addSequence(Sequence sequence) {
        sequences.add(sequence);
    }

    /**
     * Get the string representation of this SequenceDatabase
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();
        for (Sequence sequence : sequences) {
            r.append(sequence.getId());
            r.append(":  ");
            r.append(sequence.toString());
            r.append('\n');
        }
        return r.toString();
    }

    public int size() {
        return sequences.size();
    }

    public List<Sequence> getSequences() {
        return sequences;
    }

    /**
     * Get the equivalence classes associated with the frequent items
     * that we have found.
     * @return the trie
     */
    public Trie frequentItems() {
        Trie result = new Trie();
        List<TrieNode> frequentItemsNodes = new ArrayList<TrieNode>(frequentItems.values());
        result.setNodes(frequentItemsNodes);
        result.sort();
        return result;
    }

    /**
     * Get the map that makes the matching between items and 
     * equivalence classes
     * @return the map
     */
    public Map<Item, TrieNode> getFrequentItems() {
        return frequentItems;
    }

    /**
     * It reduces the original database to just frequent items
     * @param keySet 
     */
    private void reduceDatabase(Set<Item> keySet) {
        for (int k = 0; k < sequences.size(); k++) {
            Sequence sequence = sequences.get(k);
            for (int i = 0; i < sequence.size(); i++) {
                Itemset itemset = sequence.get(i);
                for (int j = 0; j < itemset.size(); j++) {
                    Item item = itemset.get(j);
                    if (!keySet.contains(item)) {
                        sequence.remove(i, j);
                        j--;
                    }
                }
                if (itemset.size() == 0) {
                    sequence.remove(i);
                    i--;
                }
            }
            if (sequence.size() == 0) {
                sequences.remove(k);
                k--;
            }
        }
    }

    public void clear() {
        if (sequences != null) {
            sequences.clear();
        }
        sequences = null;
        if (frequentItems != null) {
            frequentItems.clear();
        }
        frequentItems = null;
        itemFactory = null;

        projectingDistance = null;
        sequenceItemsetSize = null;
        sequencesLengths = null;
    }
}
