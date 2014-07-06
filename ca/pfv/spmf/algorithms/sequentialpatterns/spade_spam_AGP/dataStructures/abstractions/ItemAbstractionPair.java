package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Item;

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
     * Item.
     */
    Item item;
    /**
     * Abstraction associated with the item.
     */
    Abstraction_Generic abstraction;

    public ItemAbstractionPair(Item item, Abstraction_Generic abstraction) {
        this.item = item;
        this.abstraction = abstraction;
    }

    @Override
    public boolean equals(Object arg) {
        if (arg == null) {
            return false;
        } else if (this == arg) {
            return true;
        } else if (!(arg instanceof ItemAbstractionPair)) {
            return false;
        }
        ItemAbstractionPair pItemAbs = (ItemAbstractionPair) arg;
        return (this.getItem().equals(pItemAbs.getItem()) && this.getAbstraction().equals(pItemAbs.getAbstraction()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.item != null ? this.item.hashCode() : 0);
        hash = 53 * hash + (this.abstraction != null ? this.abstraction.hashCode() : 0);
        return hash;
    }

    public Abstraction_Generic getAbstraction() {
        return abstraction;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public String toString() {
        if (abstraction instanceof Abstraction_Qualitative) {
            return (getAbstraction().toString() + " " + getItem().toString());
        }
        return (getItem().toString() + getAbstraction().toString() + " ");
    }
    
    /**
     * Get the string representation of this object in SPMF format.
     * @return the string representation
     */
    public String toStringToFile() {
        if (abstraction instanceof Abstraction_Qualitative) {
            return (getAbstraction().toStringToFile() + " " + getItem().toString());
        }
        return (getItem().toString() + getAbstraction().toString() + " ");
    }

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
