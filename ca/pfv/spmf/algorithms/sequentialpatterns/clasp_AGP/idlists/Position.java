package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists;

/**
 * This class represents a position of an item in a sequence. Each item 
 * position is indicated by two values, an itemset index (the itemset where the 
 * item appears), and the position of the item within that itemset.
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
public class Position {
    /**
     * Itemset index where the item appears
     */
    private int itemsetIndex;
    
    /**
     * Item index where the item appears
     */
    private int itemIndex;
    
    /**
     * Standard constructor
     * @param itemset
     * @param item 
     */
    public Position(int itemset, int item){
        this.itemsetIndex=itemset;
        this.itemIndex=item;
    }

    public Integer getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int item) {
        this.itemIndex = item;
    }

    public Integer getItemsetIndex() {
        return itemsetIndex;
    }

    public void setItemsetIndex(int itemset) {
        this.itemsetIndex = itemset;
    }
    
    
}
