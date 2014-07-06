package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators;

import java.util.HashMap;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.abstractions.ItemAbstractionPair;

/**
 * Class that implements a creator for pairs <item,abstraction> that are used in a pattern implementation.
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
     * Static reference to make the class singleton
     */
    private static ItemAbstractionPairCreator instance = null;

    private ItemAbstractionPairCreator() {
    }

    public static void sclear() {
        poolPairs.clear();
    }

    private static Map<Item, Map<Abstraction_Generic, ItemAbstractionPair>> poolPairs = new HashMap<Item, Map<Abstraction_Generic, ItemAbstractionPair>>();

    /**
     * Get the reference of the singleton creator object
     * @return the reference
     */
    public static ItemAbstractionPairCreator getInstance() {
        if(instance==null){
            instance = new ItemAbstractionPairCreator();
        }
        return instance;
    }

    /**
     * Create an ItemAbstractionPair from an item and an abstraction
     * @param item an item
     * @param abstraction an abstraction
     * @return the ItemAbstractionPair created
     */
    public ItemAbstractionPair getItemAbstractionPair(Item item, Abstraction_Generic abstraction) {
        return new ItemAbstractionPair(item, abstraction);
    }
}
