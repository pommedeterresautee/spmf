package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
     * Item list that composes the itemset
     */
    private List<Item> items = new ArrayList<Item>();
    /**
     * Temporal instant when the itemset occurs
     */
    private long timestamp = 0;

    /**
     * Constructor from an item and a timestamp
     * @param item
     * @param timestamp 
     */
    public Itemset(Item item, long timestamp) {
        addItem(item);
        setTimestamp(timestamp);
    }

    /**
     * Constructor from a collection of items and a timestamp
     * @param collection
     * @param timestamp 
     */
    public Itemset(Collection<Item> collection, long timestamp) {
        for (Item item : collection) {
            addItem(item);
        }
        setTimestamp(timestamp);
    }

    /**
     * Constructor from an array of items and a timestamp
     * @param collection
     * @param timestamp 
     */
    public Itemset(Item[] collection, long timestamp) {
        for (Item item : collection) {
            addItem(item);
        }
        setTimestamp(timestamp);
    }

    /**
     * Standard constructor. The itemlist is initialized to empty and the 
     * timestamp set to 0
     */
    public Itemset() {
    }

    /**
     * Adds an item in the last position of the itemset
     * @param value item to add
     */
    public void addItem(Item value) {
        items.add(value);
    }

    /**
     * Adds an item in the specified position of the itemset
     * @param index index in the itemset where we want to insert the item
     * @param value item to add
     */
    public void addItem(int index, Item value) {
        items.set(index, value);
    }

    /**
     * It removes the specified item
     * @param item
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    /**
     * It removes the item that is in the specified index
     * @param index
     */
    public Item removeItem(int index) {
        return items.remove(index);
    }

    /**
     * It returns the item list for this itemset
     * @return the list of items in this itemset
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * It returns the item that can be found at the specified index in this itemset
     * @param index the item index 
     * @return the item
     */
    public Item get(int index) {
        return items.get(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append(item.toString());
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * It clones the current itemset.
     * @return the clone of the current itemset.
     */
    public Itemset cloneItemset() {
        Itemset itemset = new Itemset();
        itemset.setTimestamp(timestamp);
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
     * It gives the number of items that compose the itemset
     * @return  the number of items.
     */
    public int size() {
        return items.size();
    }

    /**
     * It searchs for the given item by means of a binary search
     * @param item item to search for
     * @return the index where the item is, null otherwise 
     */
    int binarySearch(Item item) {
        return Collections.binarySearch(items, item,new itemComparator());
    }

    /**
     * It searchs for the given item by means of a lineal search
     * @param item item to search for
     * @return the index where the item is, null otherwise
     */
    int linealSearch(Item item){
        //for each item of the itemset
        for(int i=0;i<items.size();i++){
            Item currentItem=items.get(i);
            int compareOutput = currentItem.compareTo(item);
            //if it is equal to the given item
            if(compareOutput==0){
                //we return the position where the item is
                return i;
            }else if(compareOutput<0){
                break;
            }
        }
        return -1;
    }
}