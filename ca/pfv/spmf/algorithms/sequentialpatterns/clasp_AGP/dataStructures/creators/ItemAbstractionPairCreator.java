package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators;

import java.util.HashMap;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.abstracciones.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.abstracciones.ItemAbstractionPair;

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

    private static ItemAbstractionPairCreator instance = null;
    private static Map<Item, Map<Abstraction_Generic, ItemAbstractionPair>> pailPoors = new HashMap<Item, Map<Abstraction_Generic, ItemAbstractionPair>>();

    private ItemAbstractionPairCreator() {
    }

    /**
     * Get the reference of the singleton creator object
     * @return the singleton
     */
    public static ItemAbstractionPairCreator getInstance() {
        if (instance == null) {
            instance = new ItemAbstractionPairCreator();
        }
        return instance;
    }

    public ItemAbstractionPair getItemAbstractionPair(Item item, Abstraction_Generic abstraction) {
        //Map<AbstraccionItemGenerica, ItemAbstractionPair> itemPair = poolPares.get(item);
        ItemAbstractionPair pair;
        /*if (itemPair != null) {
            pair = itemPair.get(abstraction);
            if (pair == null) {*/
                pair = new ItemAbstractionPair(item, abstraction);
                /*itemPair.put(abstraction, pair);
            }
        } else {
            itemPair = new FastMap<AbstraccionItemGenerica, ItemAbstractionPair>();
            pair = new ItemAbstractionPair(item, abstraction);
            itemPair.put(abstraction, pair);
            poolPares.put(item, itemPair);
        }*/
        return pair;
    }

    public void clear() {
        pailPoors.clear();
    }
    public static void sclear() {
        pailPoors.clear();
        instance=null;
    }
}
