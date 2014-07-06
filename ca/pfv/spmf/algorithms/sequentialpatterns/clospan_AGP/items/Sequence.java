package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

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
     * Itemsets that compose the sequence.
     */
    private final List<Itemset> itemsets = new ArrayList<Itemset>();
    /**
     * Sequence identifier.
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
     * It adds an item to the last itemset of the sequence
     * @param item the item to add
     */
    public void addItem(Item item){
        itemsets.get(size()-1).addItem(item);
        numberOfItems++;
    }

    /**
     * It adds an item in the specified itemset of the sequence
     * @param indexItemset Itemset index where we want to insert the item
     * @param item The item that we want to insert
     */
    public void addItem(int indexItemset, Item item){
        itemsets.get(indexItemset).addItem(item);
        numberOfItems++;
    }

    /**
     * It adds an item in the specified item position of the specified itemset of the sequence
     * @param indexItemset Itemset index where we want to insert the item
     * @param indexItem Item index where we want to insert the item
     * @param item The item that we want to insert
     */
    public void addItem(int indexItemset, int indexItem, Item item){
        itemsets.get(indexItemset).addItem(indexItem,item);
        numberOfItems++;
    }

    /**
     * It removes the specified itemset from the sequence
     * @param indexItemset Itemset index of itemset that we want to remove
     * @return the itemset that was rmoved
     */
    public Itemset remove(int indexItemset){
        Itemset itemset= itemsets.remove(indexItemset);
        numberOfItems-=itemset.size();
        return itemset;
    }

    /**
     * It removes the specified item from the specified itemset in the sequence
     * @param indexItemset Itemset index from where we want to remove the item
     * @param indexItem Item index that we want to remove
     * @return the item that was removed
     */
    public Item remove(int indexItemset, int indexItem){
        numberOfItems--;
        return itemsets.get(indexItem).removeItem(indexItem);
    }

    /**
     * It removes the specified item from the specified itemset in the sequence
     * @param indexItemset Itemset index from where we want to remove the item
     * @param item Item that we want to remove
     */
    public void remove(int indexItemset, Item item){
        itemsets.get(indexItemset).removeItem(item);
        numberOfItems--;
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
     * Get a string representation of this sequence.
     * @return the string representation.
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
     * It gets the list of itemsets in this sequence.
     * @return the list of itemsets
     */
    public List<Itemset> getItemsets() {
        return itemsets;
    }

    /**
     * It gets a particular itemset from the sequence
     * @param index The itemset index in which we are interested in
     * @return the itemset
     */
    public Itemset get(int index) {
        return itemsets.get(index);
    }

    /**
     * It returns the number of itemsets that the sequence has
     * @return the size (int)
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
     * @return the time length (long)
     */
    public long getTimeLength() {
        return itemsets.get(itemsets.size() - 1).getTimestamp() - itemsets.get(0).getTimestamp();
    }

    /**
     * It clones a sequence without copying the non-frequent items
     * @param mapSequenceID Association between an item and their appearances in the database
     * @param relativeMinSup Minimum relative support
     * @return the clone
     */
    public Sequence cloneSequenceMinusItems(Map<Item, BitSet> mapSequenceID, double relativeMinSup) {
        Sequence sequence = new Sequence(getId());
        for (Itemset itemset : itemsets) {
            Itemset newItemset = itemset.cloneItemSetMinusItems(mapSequenceID, relativeMinSup);
            if (newItemset.size() != 0) {
                sequence.addItemset(newItemset);
            }
        }
        return sequence;
    }

    /**
     * Sequence ID setter
     * @param id 
     */
    public void setID(int id) {
        this.id = id;
    }
}
