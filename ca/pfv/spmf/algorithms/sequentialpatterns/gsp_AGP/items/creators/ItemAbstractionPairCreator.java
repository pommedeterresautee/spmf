/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.ItemAbstractionPair;

/**
 * Class that implements the pair <item,abstraction> that is used in a pattern implementation.
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
public class ItemAbstractionPairCreator {

    /**
     * Static reference for make this class Singleton
     */
    private static ItemAbstractionPairCreator instance = null;
    //private static Map<Item, Map<Abstraction_Generic, ItemAbstractionPair>> pairPool = new HashMap<Item, Map<Abstraction_Generic, ItemAbstractionPair>>();

    private ItemAbstractionPairCreator() {
    }

    public static void clear(){
        instance=null;
        //pairPool.clear();
    }

    /**
     * Get the static instance of the class (a singleton).
     * @return the instance
     */
    public static ItemAbstractionPairCreator getInstance() {
        if (instance == null) {
            instance = new ItemAbstractionPairCreator();
        }
        return instance;
    }

    /**
     * It returns a pair composed by an item and an abstraction
     * @param item the given item
     * @param abstraction the given abstraction
     * @return the duple <item, abstraction>
     */
    public ItemAbstractionPair getItemAbstractionPair(Item item, Abstraction_Generic abstraction) {
        /*Map<Abstraction_Generic, ItemAbstractionPair> itemPair = pairPool.get(item);*/
        ItemAbstractionPair pair;
        /*if (itemPair != null) {
            pair = itemPair.get(abstraction);
            if (pair == null) {*/
                pair = new ItemAbstractionPair(item, abstraction);
                /*itemPair.put(abstraction, pair);
            }
        } else {
            itemPair = new HashMap<Abstraction_Generic, ItemAbstractionPair>();
            pair = new ItemAbstractionPair(item, abstraction);
            itemPair.put(abstraction, pair);
            pairPool.put(item, itemPair);
        }*/
        return pair;
    }
}
