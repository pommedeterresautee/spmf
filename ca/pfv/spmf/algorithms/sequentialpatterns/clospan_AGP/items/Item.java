package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items;

/**
 * Implementation of an item.
 * This implementation is generic in order to be able to manage any kind of item (string, integer, ...)
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
public class Item<T extends Comparable> implements Comparable<Item> {

    /**
     * Content of the item. Is implemented in a generic way in order to manage
     * several types of items.
     */
    private T id;

    /**
     * General constructor.
     * @param id Content of the item.
     */
    public Item(T id) {
        this.id = id;
    }

    /**
     * It gets the content of the item.
     * @return the content T
     */
    public T getId() {
        return id;
    }

    /**
     * Get the string representation of this item.
     * @return the string 
     */
    @Override
    public String toString() {
        return "" + getId();
    }

    /**
     * Check if this item is equal to another
     * @param object the other item
     * @return true if equal.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Item) {
            Item item = (Item) object;
            if ((item.getId().equals(this.getId()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Item i) {
        //return -id.compareTo(i.getId());
        return id.compareTo(i.getId());
    }
}
