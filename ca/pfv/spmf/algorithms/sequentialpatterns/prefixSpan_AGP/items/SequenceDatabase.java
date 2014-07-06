package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.patterns.PatternCreator;

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
public class SequenceDatabase {

    private AbstractionCreator abstractionCreator;
    private Map<Item, BitSet> frequentItems = new HashMap<Item, BitSet>();
    private List<Sequence> sequences = new LinkedList<Sequence>();
    private ItemFactory<Integer> itemFactory = new ItemFactory<Integer>();
    private PatternCreator patternCreator = new PatternCreator();

    public SequenceDatabase(AbstractionCreator creador) {
        this.abstractionCreator = creador;
    }

    /**
     * From a file located at the given string path, we create a database
     * composed of a list of sequences
     *
     * @param path File path where we have the database
     * @param minSupportRelative Minimum absolute support
     * @throws IOException
     */
    public void loadFile(String path, double minSupportRelative) throws IOException {
        String thisLine;
        BufferedReader myInput = null;
        try {
            FileInputStream fin = new FileInputStream(new File(path));
            myInput = new BufferedReader(new InputStreamReader(fin));
            int sequenceID = 1;
            //For each line
            while ((thisLine = myInput.readLine()) != null) {
                // If the line is not a comment line
                if (thisLine.charAt(0) != '#') {
                    // we read it and add it as a sequence
                    addSequence(thisLine.split(" "), sequenceID);
                    sequenceID++;
                }
            }
            double minSupportAbsolute = (int) Math.ceil(minSupportRelative * sequences.size());
            //We get the set of items
            Set<Item> frequent = frequentItems.keySet();
            //And prepare a list to keep the non-frequent ones
            Set<Item> toRemove = new HashSet<>();
            for (Item frequentItem : frequent) {
                if ((frequentItems.get(frequentItem)).cardinality() < minSupportAbsolute) {
                    toRemove.add(frequentItem);
                }
            }
            //We remove from the original set those non frequent items
            for (Item toRemoveItem : toRemove) {
                frequentItems.remove(toRemoveItem);
            }
        } catch (Exception e) {
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
    }

    /**
     * It adds a sequence from an array of string that we have to interpret
     * @param integers
     * @param sequenceID 
     */
    public void addSequence(String[] integers, int sequenceID) {
        long timestamp = -1;
        Sequence sequence = new Sequence(sequences.size());
        sequence.setID(sequenceID);
        Itemset itemset = new Itemset();
        int inicio = 0;
        Map<Item, Boolean> counted = new HashMap<Item, Boolean>();

        for (int i = inicio; i < integers.length; i++) {
            if (integers[i].codePointAt(0) == '<') {  // Timestamp
                String value = integers[i].substring(1, integers[i].length() - 1);
                timestamp = Long.parseLong(value);
                itemset.setTimestamp(timestamp);
            } else if (integers[i].equals("-1")) { // end of an itemset
                long time = itemset.getTimestamp() + 1;
                sequence.addItemset(itemset);
                itemset = new Itemset();
                itemset.setTimestamp(time);
            } else if (integers[i].equals("-2")) { // end of a sequence
                sequences.add(sequence);
            } else {
                // extract the value for an item
                Item item = itemFactory.getItem(Integer.parseInt(integers[i]));
                Abstraction_Generic abs = abstractionCreator.CreateDefaultAbstraction();
                if (counted.get(item) == null) {
                    counted.put(item, Boolean.TRUE);
                    BitSet appearances = frequentItems.get(item);
                    if (appearances == null) {
                        appearances = new BitSet();
                        frequentItems.put(item, appearances);
                    }
                    appearances.set(sequence.getId());
                }
                itemset.addItem(item);
            }


        }
    }

    /**
     * String representation of the SecuenceDatabase
     * @return the string
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

    /**
     * It returns the number of sequences of the sequence database
     * @return the number of sequences
     */
    public int size() {
        return sequences.size();
    }

    /**
     * It return the list of sequences in this sequence database
     * @return the list of sequences
     */
    public List<Sequence> getSequences() {
        return sequences;
    }

    /**
     * It return the frequent items
     * @return a map of entries where key = item and value = its bitset.
     */
    public Map<Item, BitSet> getFrequentItems() {
        return frequentItems;
    }

    public void clear() {
        abstractionCreator = null;
        if (frequentItems != null) {
            frequentItems.clear();
        }
        frequentItems = null;
        if (sequences != null) {
            sequences.clear();
        }
        sequences = null;
        itemFactory = null;
        patternCreator = null;

    }
}
