package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class represents an itemset from a sequence.
 * The itemset consists of a list of items and an timestamp that denotes when the itemset occurs. An itemset with timestamp is also called "transaction"
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
public final class Itemset{

    /**
     * List of items that compose the itemset.
     */
    private List<Item> items = new ArrayList<Item>();

    /**
     * Timestamp of the itemset.
     */
    private long timestamp = 0;

    /**
     * Constructor of an itemset from an item and the timestamp.
     * @param item the item that compose the itemset.
     * @param timestamp the timestamp associated with the itemset.
     */
    public Itemset(Item item, long timestamp) {
        addItem(item);
        setTimestamp(timestamp);
    }

    /**
     * Constructor of an itemset from a collection of items and the timestamp
     * associated with the itemset.
     * @param collection Items that compose the itemset.
     * @param timestamp the timestamp associated with the itemset
     */
    public Itemset(Collection<Item> collection, long timestamp) {
        for (Item item : collection) {
            addItem(item);
        }
        setTimestamp(timestamp);
    }

    /**
     * Constructor of an itemset from an array of items and the timestamp
     * associated with the itemset.
     * @param collection Items that compose the itemset.
     * @param timestamp the timestamp associated with the itemset
     */
    public Itemset(Item[] collection, long timestamp) {
        for (Item item : collection) {
            addItem(item);
        }
        setTimestamp(timestamp);
    }

    /**
     * Standard Itemset constructor
     */
    public Itemset() {
    }

    /**
     * It adds an item to the itemset. The item is inserted in the last position.
     * @param value The item to add
     */
    public void addItem(Item value) {
        items.add(value);
    }

    /**
     * It adds an item to the itemset. The item is inserted in the specified
     * position.
     * @param i index where we want to add the item
     * @param value The item to add
     */
    public void addItem(int i, Item value) {
        items.set(i, value);
    }

    /**
     * It removes the specified item.
     * @param value The item to remove
     */
    public void removeItem(Item value) {
        items.remove(value);
    }

    /**
     * It removes the item which appears in the specified index.
     * @param i the index
     * @return the removed item
     */
    public Item removeItem(int i) {
        return items.remove(i);
    }

    /**
     * It returns the items that compose the itemset.
     * @return the list of items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * It returns the item from the specified position.
     * @param index The index where is the item in which we are interested.
     * @return the item
     */
    public Item get(int index) {
        return items.get(index);
    }

    /**
     * Get the string representation of this itemset.
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Item attribute : items) {
            sb.append(attribute.toString());
            sb.append(' ');
        }
        return sb.toString();
    }
    
    /**
     * It clones the itemset.
     * @return The clone itemset.
     */
    public Itemset cloneItemset() {
        Itemset itemset = new Itemset();
        itemset.timestamp = timestamp;
        itemset.getItems().addAll(items);
        return itemset;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * It returns the number of items that compose the itemset.
     * @return the number of items 
     */
    public int size() {
        return items.size();
    }

    /**
     * It clones the itemset ignoring the items that are non-frequent.
     * @param mapSequenceID the map of items with their associated bitsets representing their sequence IDs
     * @return the clone
     */
    Itemset cloneItemSetMinusItems(Map<Item, BitSet> mapSequenceID) {
        Itemset itemset = new Itemset();
		itemset.timestamp = timestamp;
		for(Item item : items){
			if(mapSequenceID.get(item)!=null){
				itemset.addItem(item);
			}
		}
		return itemset;
    }
    
    /**
     * Check if this itemset is equal to another
     * Two itemsets are equals if in each position they have the same item.
     * @param arg the other itemset
     * @return true if equal, otherwise false.
     */
    @Override
    public boolean equals(Object arg){
        Itemset param=(Itemset)arg;
        if((param.size())!=(this.size())) {
            return false;
        }
        for(int i=0;i<items.size();i++){
            if(!items.get(i).equals(param.items.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.items != null ? this.items.hashCode() : 0);
        //hash = 79 * hash + this.beginning;
        return hash;
    }
}
