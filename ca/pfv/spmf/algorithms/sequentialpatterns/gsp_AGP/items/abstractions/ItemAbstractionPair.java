/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Item;

/**
 * Class that represents a pair <item,abstraction>. 
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
public class ItemAbstractionPair implements Comparable<ItemAbstractionPair> {

    /**
     * Item of the pair
     */
    Item item;
    /**
     * Abstraction of the pair
     */
    Abstraction_Generic abstraction;

    /**
     * Standard constructor
     * @param item
     * @param abstraction 
     */
    public ItemAbstractionPair(Item item, Abstraction_Generic abstraction) {
        this.item = item;
        this.abstraction = abstraction;
    }

    /**
     * Check if this item is equal to another by checking if
     * both item and abstraction are identical.
     * @param arg the other item
     * @return  true if equal, otherwise false
     */
    @Override
    public boolean equals(Object arg) {
        if (arg == null) {
            return false;
        } else if (this == arg) {
            return true;
        } else if (!(arg instanceof ItemAbstractionPair)) {
            return false;
        }
        ItemAbstractionPair itemAbstractionPair = (ItemAbstractionPair) arg;
        return (this.getItem().equals(itemAbstractionPair.getItem()) && this.getAbstraction().equals(itemAbstractionPair.getAbstraction()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.item != null ? this.item.hashCode() : 0);
        hash = 53 * hash + (this.abstraction != null ? this.abstraction.hashCode() : 0);
        return hash;
        //return item.hashCode() + abstraction.hashCode();
    }

    public Abstraction_Generic getAbstraction() {
        return abstraction;
    }

    public Item getItem() {
        return item;
    }

    /**
     * Get the string representation of this object
     * @return the string representation.
     */
    @Override
    public String toString() {
        if (abstraction instanceof Abstraction_Qualitative) {
            return (getAbstraction().toString() + " " + getItem().toString());
        }
        return (getItem().toString() + getAbstraction().toString() + " ");
    }
    
    /**
     * Get the string representation of this item. Adjusted to SPMF format.
     * @return the string representation
     */
    public String toStringToFile() {
        if (abstraction instanceof Abstraction_Qualitative) {
            return (getAbstraction().toStringToFile() + " " + getItem().toString());
        }
        return (getItem().toString() + getAbstraction().toString() + " ");
    }

    /**
     * It compares this item with another and then, if they are equals, their abstractions.
     * @param arg the other item
     * @return true if equal, otherwise false
     */
    @Override
    public int compareTo(ItemAbstractionPair arg) {
        int itemComparison = getItem().compareTo(arg.getItem());
        if (itemComparison == 0) {
            return getAbstraction().compareTo(arg.getAbstraction());
        } else {
            return itemComparison;
        }
    }
}
