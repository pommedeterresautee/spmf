package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items;

import java.util.ArrayList;
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
     * Number of items that appear in the sequence, i.e. the addition of the itemset sizes.
     */
    private int numberOfItems = 0;
    /**
     * itemsets that compose the sequence.
     */
    private final List<Itemset> itemsets = new ArrayList<Itemset>();
    /**
     * Sequence identifier.
     */
    private int id;

    /**
     * Constructor for a Sequence.
     * @param id an integer identifier
     */
    public Sequence(int id) {
        this.id = id;
    }

    /**
     * Adds an itemset in the sequence. The itemset is inserte at the end of the sequence
     * @param itemset the itemset to add
     */
    public void addItemset(Itemset itemset) {
        itemsets.add(itemset);
        numberOfItems += itemset.size();
    }

    /**
     * Adds an item to the last itemset of de sequence.
     * @param item 
     */
    public void addItem(Item item) {
        itemsets.get(size() - 1).addItem(item);
        numberOfItems++;
    }

    /**
     * Adds an item to the specified itemset
     * @param indexItemset itemset where we want to add the item
     * @param item item to add
     */
    public void addItem(int indexItemset, Item item) {
        itemsets.get(indexItemset).addItem(item);
        numberOfItems++;
    }

    /**
     * Add an item in the specified position of the specified itemset
     * @param indexItemset Index of the itemset where we want to add the item
     * @param indexItem Position in the itemset where we want to add the item
     * @param item item to add
     */
    public void addItem(int indexItemset, int indexItem, Item item) {
        itemsets.get(indexItemset).addItem(indexItem, item);
        numberOfItems++;
    }

    /**
     * It removes a specified itemset
     * @param indexItemset index of the itemset that we want to remove
     * @return the removed itemset
     */
    public Itemset remove(int indexItemset) {
        Itemset itemset = itemsets.remove(indexItemset);
        numberOfItems -= itemset.size();
        return itemset;
    }

    /**
     * It removes a specified item in a specified itemset.
     * @param indexItemset index of the itemset where we want to remove an item
     * @param indexItem index of the item that we want to remove
     * @return the removed item
     */
    public Item remove(int indexItemset, int indexItem) {
        numberOfItems--;
        return itemsets.get(indexItemset).removeItem(indexItem);
    }

    /**
     * It removes a specified item in a specified itemset.
     * @param indexItemset index of the itemset where we want to remove an item
     * @param item item to remove
     */
    public void remove(int indexItemset, Item item) {
        itemsets.get(indexItemset).removeItem(item);
        numberOfItems--;
    }

    /**
     * It clones a sequence
     * @return the cloned sequence
     */
    public Sequence cloneSequence() {
        Sequence sequence = new Sequence(getId());
        for (Itemset itemset : itemsets) {
            sequence.addItemset(itemset.cloneItemset());
        }
        return sequence;
    }

    /**
     * The sequence as a string
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (Itemset itemset : itemsets) {
            sb.append("{t=");
            sb.append(itemset.getTimestamp());
            sb.append(", ");
            for (Item item : itemset.getItems()) {
                String string = item.toString();
                sb.append(string);
                sb.append(' ');
            }
            sb.append('}');
        }
        return sb.append("    ").toString();
    }

    /**
     * It returns the sequence identifier
     * @return the sequence id of this sequence
     */
    public int getId() {
        return id;
    }

    /**
     * Set the sequence identifier of this sequence
     * @param integer the sequence id.
     */
    void setId(Integer integer) {
        id=integer;
    }

    /**
     * Obtains a list  the itemsets that compose the sequence
     * @return the list of itemsets
     */
    public List<Itemset> getItemsets() {
        return itemsets;
    }

    /**
     * It gets the ith itemset of this sequence
     * @param index the index i
     * @return the itemset
     */
    public Itemset get(int index) {
        return itemsets.get(index);
    }

    /**
     * The number of itemsets that compose the sequence
     * @return  the number of itemsets
     */
    public int size() {
        return itemsets.size();
    }

    /**
     * The number of items that compose the sequence
     * @return the number of items
     */
    public int getLength() {
        return numberOfItems;
    }

    /**
     * Get the time length of the sequence. It is equal to the last itemset timestamp minus the first itemset timestamp
     * @return the time length
     */
    public long getTimeLength() {
        return itemsets.get(itemsets.size() - 1).getTimestamp() - itemsets.get(0).getTimestamp();
    }

    /**
     * It returns a pair <itemset index, item index> indicanting the first appearance of an item in the sequence.
     * The starting point where the method takes the search up is given as parameter.
     * @param itemsetIndex Index of the first itemset where we will start to search
     * @param itemIndex Index of the first item where we will start to search
     * @param item Item to find
     * @return the pair position if the item is found, otherwise null value
     */
    public int[] searchForTheFirstAppearance(int itemsetIndex, int itemIndex, Item item) {
        //We ensure that the itemset index is within the sequence
        if (itemsetIndex < size()) {
            //From that itemset
            for (int i = itemsetIndex; i < itemsets.size(); i++) {
                Itemset currentItemset = itemsets.get(i);
                //the beginning index is 0 or itemIndex if we are in the itemset referred by itemsetIndex
                int beginning = (i == (itemsetIndex)) ? (itemIndex) : 0;
                //We search for the item in our current itemset
                int pos = currentItemset.binarySearch(item);
                
                //uncomment the line of below if you'd rather run a lineal search
                //int pos = currentItemset.linealSearch(item);
                
                //If the index returned by the search method is positive and equal or greater than our beginning item index
                if (pos >= beginning) {
                    //We return the pair <i,pos>, where i is the  current itemset index and pos the current item index in that itemset
                    return new int[]{i, pos};
                }
            }
        }
        return null;
    }
    
    /**
     * 
     * @param item
     * @param itemsetIndex
     * @param ItemIndex
     * @return 
     */
    /*public int[] searchForAnItemInLaterItemset(Item item, int itemsetIndex, int ItemIndex) {
        if (itemsetIndex < size()) {
            int firstItemset = itemsetIndex;
            int firstItem = ItemIndex;
            for (int i = firstItemset; i < itemsets.size(); i++) {
                Itemset currentItemset = itemsets.get(i);
                int beginning = (i == firstItemset) ? (firstItem) : 0;
                
                //int pos = currentItemset.binarySearch(item);                
                //uncomment if you'd rather run a lineal search
                int pos = currentItemset.linealSearch(item);
                
                if (pos >= beginning) {
                    return new int[]{i,pos};
                }
            }
        }
        return null;
    }*/

    /**
     * It returns a pair <itemset index, item index> indicanting the appearance of an item in the itemset given as parameter.
     * The starting point where the method takes the search up is given as parameter
     * @param item Item to find
     * @param itemsetIndex Index of the first itemset where we will start to search
     * @param itemIndex Index of the first item where we will start to search
     * @return the pair position if the item is found, otherwise null value
     */
    public int[] SearchForItemAtTheSameItemset(Item item, int itemsetIndex, int itemIndex) {
        //We ensure that the itemset index is within the sequence
        if (itemsetIndex < size()) {
            //For the given itemset
            Itemset currentItemset = itemsets.get(itemsetIndex);
            
            //We search for the item in our current itemset
            int pos = currentItemset.binarySearch(item);            
            //uncomment the line of below if you'd rather run a lineal search
            //int pos = currentItemset.linealSearch(item);
            
            //If the index returned by the search method is positive and equal or greater than our beginning item index
            if (pos >= itemIndex) {
                //We return the pair <itemsetIndex,pos>, where itemsetIndex is the current itemset index and pos the current item index in that itemset
                return new int[]{itemsetIndex, pos};
            }
        }
        return null;
    }

    /**
     * It returns a pair <itemset index, item index> indicanting the first appearance
     * of an item in the sequence that appears in a itemset that has a temporal 
     * distance with respect to the current itemset, as it the given parameter.
     * The starting point where the method takes the search up is given as parameter.
     * @param item Item to find
     * @param itemsetIndex Index of the first itemset where we will start to search
     * @param itemIndex Index of the first item where we will start to search
     * @param temporalDistance Temporal distance at which the item to find should be
     * @return the pair position if the item is found, otherwise null value
     */
    public int[] searchForItemInAConcreteTemporalDistance(Item item, int itemsetIndex, int itemIndex, long temporalDistance) {
        //We ensure that the first itemset index is within the sequence
        if (itemsetIndex < size()) {

            //We keep the current timestamp
            long initialTimestamp = itemsets.get(itemsetIndex).getTimestamp();
            //And the timestamp of the itemset where the item should be, if it is the case
            long objectiveTimestamp = initialTimestamp + temporalDistance;
            //we establish the itemset index starting from the next itemset
            int itemset = itemsetIndex + 1;
            //while we still have itemsets in the sequence and they have a timestamp less than that in which we are interested, we skip them
            while (itemset < itemsets.size() && itemsets.get(itemset).getTimestamp() < objectiveTimestamp) {
                itemset++;
            }
            //When the loop is over, if the itemset timestamp is the same as that we are expecting
            if (itemset < itemsets.size() && itemsets.get(itemset).getTimestamp() == objectiveTimestamp) {
                Itemset currentItemset = itemsets.get(itemset);
                //We search for the item in our current itemset
                int pos = currentItemset.binarySearch(item);
                
                //uncomment the line of below if you'd rather run a lineal search
                //int pos = currentItemset.linealSearch(item);
                
                //If the index returned by the search method is positive and equal or greater than our beginning item index
                if (pos >= 0) {
                    //We return the pair <itemset,pos>, where itemset is the current itemset index and pos the current item index in that itemset
                    return new int[]{itemset, pos};
                }
            }
        }
        return null;
    }

    /**
     * It gives the number of items that appears after a concrete position of the sequence
     * @param itemsetIndex the itemset index in which we will start to look for
     * @param itemIndex the item index in which we will start to look for
     * @return the number of items
     */
    public int numberOfItemsAfterPositionIth(int itemsetIndex, int itemIndex){
        int size=0;
        if(itemsetIndex<itemsets.size()-1){
            int currentItemset=itemsetIndex+1;
            for(int i=currentItemset;i<itemsets.size();i++){
                size+=itemsets.get(currentItemset).size();
            }
        }
        size+=(itemsets.get(itemsetIndex).size()-itemIndex-1);
        return size;
    }
}
