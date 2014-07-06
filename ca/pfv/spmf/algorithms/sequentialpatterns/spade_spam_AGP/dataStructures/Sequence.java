package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**Inspired in SPMF Implementation of a sequence.
 * A sequence is defined as a list of itemsets and can have an identifier.
 * 
 * Copyright Antonio Gomariz Peñalver 2013
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author agomariz
 */
public class Sequence {

    /**
     * Counter that has the total items contained in the sequence.
     */
    private int numberOfItems = 0;
    /**
     * Itemsets that compose the sequence
     */
    private final List<Itemset> itemsets = new ArrayList<Itemset>();
    /**
     * Sequence identifier
     */
    private int id;

    /**
     * Standard constructor for a sequence
     * @param id The sequence identifier
     */
    public Sequence(int id) {
        this.id = id;
    }

    /**
     * It adds an itemset in the last position of the sequence
     * @param itemset the itemset to add
     */
    public void addItemset(Itemset itemset) {
        itemsets.add(itemset);
        numberOfItems += itemset.size();
    }

    /**
     * It adds an item in the specified itemset of the sequence
     * @param indexItemset Itemset index where we want to insert the item
     * @param item The item that we want to insert
     */
    public void addItem(int indexItemset, Item item) {
        itemsets.get(indexItemset).addItem(item);
        numberOfItems++;
    }

    /**
     * It removes the specified itemset from the sequence
     * @param indexItemset Itemset index of itemset that we want to remove
     * @return the removed itemset
     */
    public Itemset remove(int indexItemset) {
        Itemset itemset = itemsets.remove(indexItemset);
        numberOfItems -= itemset.size();
        return itemset;
    }

    /**
     * It removes the specified item from the specified itemset in the sequence
     * @param indexItemset Itemset index from where we want to remove the item
     * @param indexItem Item index that we want to remove
     * @return the removed item
     */
    public Item remove(int indexItemset, int indexItem) {
        numberOfItems--;
        return itemsets.get(indexItemset).removeItem(indexItem);
    }

    /**
     * It clones a sequence
     * @return the clone sequence
     */
    public Sequence cloneSequence() {
        Sequence sequence = new Sequence(getId());
        for (Itemset itemset : itemsets) {
            sequence.addItemset(itemset.cloneItemSet());
        }
        return sequence;
    }

    /**
     * Get the string representation of this sequence
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder r = new StringBuilder("");
        for (Itemset itemset : itemsets) {
            r.append("{t=");
            r.append(itemset.getTimestamp());
            r.append(", ");
            for (Item item : itemset.getItems()) {
                String string = item.toString();
                r.append(string);
                r.append(' ');
            }
            r.append('}');
        }
        return r.append("    ").toString();
    }

    /**
     * It returns the sequence ID
     * @return the sequence ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the list of itemsets of this sequence
     * @return the list of itemsets
     */
    public List<Itemset> getItemsets() {
        return itemsets;
    }

    /**
     * Get the itemset of this sequence at a given position.
     * @param index the index of the itemset
     * @return the itemset
     */
    public Itemset get(int index) {
        return itemsets.get(index);
    }

    /**
     * It returns the number of itemsets that the sequence has
     * @return the number of itemsets
     */
    public int size() {
        return itemsets.size();
    }

    /**
     * It returns the number of items that the sequence has
     * @return the number of items
     */
    public int length() {
        return numberOfItems;
    }

    /**
     * It returns the time length of the sequence, i.e. the timestamp of the 
     * last itemset minus the timestamp of the first itemset
     * @return the time length
     */
    public long getTimeLength() {
        return itemsets.get(itemsets.size() - 1).getTimestamp() - itemsets.get(0).getTimestamp();
    }
    
    /**
     * Set the sequence ID of this sequence
     * @param id  the sequence ID
     */
    public void setID(int id) {
        this.id = id;
    }
}

class comparatorTimestamps implements Comparator<Itemset> {
    /**
     * Comparator class that compares two itemsets by timestamp.
     * @param o1
     * @param o2
     * @return 
     */
    @Override
    public int compare(Itemset o1, Itemset o2) {
        long time1 = o1.getTimestamp();
        long time2 = o2.getTimestamp();
        if (time1 < time2) {
            return -1;
        }
        return 1;
    }
}
