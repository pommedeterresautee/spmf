package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.EquivalenceClass;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.ItemFactory;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.ItemAbstractionPairCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.PatternCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator;

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
    private IdListCreator idListCreator;
    private Map<Item, EquivalenceClass> frequentItems = new HashMap<Item, EquivalenceClass>();
    private List<Sequence> sequences = new LinkedList<Sequence>();
    private ItemFactory<Integer> itemFactory = new ItemFactory<Integer>();
    private PatternCreator patternCreator = PatternCreator.getInstance();
    private int nSequences = 1;

    public SequenceDatabase(AbstractionCreator abstractionCreator, IdListCreator idListCreator) {
        this.abstractionCreator = abstractionCreator;
        this.idListCreator = idListCreator;
    }

    /**
     * Method that load a database from a path file given as parameter
     *
     * @param path Path file where the database is
     * @param minSupport Minimum absolute support
     * @throws IOException
     */
    public void loadFile(String path, double minSupport) throws IOException {
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
                EquivalenceClass equivalenceClass = frequentItems.get(frequentItem);
                if (equivalenceClass.getIdList().getSupport() < support) {
                    itemsToRemove.add(frequentItem);
                } else {
                    equivalenceClass.getIdList().setAppearingSequences(equivalenceClass.getClassIdentifier());
                }
            }
            for (Item itemToRemove : itemsToRemove) {
                frequentItems.remove(itemToRemove);
            }
            //And from the original database
            reduceDatabase(frequentItems.keySet());
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
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

        for (int i = beginning; i < integers.length; i++) {
            if (integers[i].codePointAt(0) == '<') {  // Timestamp
                String value = integers[i].substring(1, integers[i].length() - 1);
                timestamp = Long.parseLong(value);
                itemset.setTimestamp(timestamp);
            } else if (integers[i].equals("-1")) { // End of an Itemset
                long time = itemset.getTimestamp() + 1;
                sequence.addItemset(itemset);
                itemset = new Itemset();
                itemset.setTimestamp(time);
                timestamp++;
            } else if (integers[i].equals("-2")) { // End of a sequence
                sequences.add(sequence);
            } else {
                // extract the value for an item
                Item item = itemFactory.getItem(Integer.parseInt(integers[i]));
                EquivalenceClass clase = frequentItems.get(item);
                if (clase == null) {
                    IDList idlist = idListCreator.create();
                    clase = new EquivalenceClass(patternCreator.createPattern(pairCreator.getItemAbstractionPair(item, abstractionCreator.createDefaultAbstraction())), idlist);
                    frequentItems.put(item, clase);
                }
                IDList idlist = clase.getIdList();
                if (timestamp < 0) {
                    timestamp = 1;
                    itemset.setTimestamp(timestamp);
                }
                idListCreator.addAppearance(idlist, sequence.getId(), (int) timestamp);

                itemset.addItem(item);
            }
        }
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

    /**
     * It returns the final number of sequences
     * @return the number of sequences
     */
    public int size() {
        return sequences.size();
    }

    /**
     * It returns the sequences of the database in a list
     * @return the list of sequences.
     */
    public List<Sequence> getSequences() {
        return sequences;
    }

    /**
     * It returns the equivalence classes associated with the frequent items
     * that we have found
     * @return the list of equivalence classes
     */
    public List<EquivalenceClass> frequentItems() {
        List<EquivalenceClass> celdasDeItemsFrecuentes = new ArrayList<EquivalenceClass>(frequentItems.values());
        Collections.sort(celdasDeItemsFrecuentes);
        return celdasDeItemsFrecuentes;
    }

    /**
     * It returns the map that makes the matching between items and 
     * equivalence classes.
     * @return the map
     */
    public Map<Item, EquivalenceClass> getFrequentItems() {
        return frequentItems;
    }

    /**
     * It return the equivalence classes associated with the frequent 
     * 2-Patterns that we have found
     * @param minSupport Minimum absolute support
     * @return the list of equivalence classes
     */
    public List<EquivalenceClass> getSize2FrecuentSequences(double minSupport) {
        List<EquivalenceClass> patronesSize2 = abstractionCreator.getFrequentSize2Sequences(sequences, idListCreator);
        removeInfrequentItems(patronesSize2, minSupport);
        for (EquivalenceClass clase : patronesSize2) {
            clase.getIdList().setAppearingSequences(clase.getClassIdentifier());
        }
        return patronesSize2;
    }

    private void removeInfrequentItems(List<EquivalenceClass> size2Patterns, double minSupport) {
        if (size2Patterns.isEmpty()) {
            return;
        }
        Item currentItem = size2Patterns.get(0).getClassIdentifier().getElements().get(0).getItem();
        EquivalenceClass value = frequentItems.get(currentItem);
        List<Integer> infrequentItems = new ArrayList<Integer>();
        for (int i = 0; i < size2Patterns.size(); i++) {
            Item nuevoItem = size2Patterns.get(i).getClassIdentifier().getElements().get(0).getItem();
            if (!nuevoItem.equals(currentItem)) {
                currentItem = nuevoItem;
                value = frequentItems.get(currentItem);
            }
            if (size2Patterns.get(i).getIdList().getSupport() < minSupport) {
                infrequentItems.add(i);
            } else {
                value.addClassMember(size2Patterns.get(i));
            }
        }
        for (int i = infrequentItems.size() - 1; i >= 0; i--) {
            EquivalenceClass removedClass = size2Patterns.remove(infrequentItems.get(i).intValue());
        }
        Collections.sort(infrequentItems);
    }

    /**
     * It reduces the original database to just frequent items.
     * @param keySet the set of frequent items that should be kept.
     */
    private void reduceDatabase(Set<Item> keySet) {
        for (Sequence sequence : sequences) {
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
    }
}
