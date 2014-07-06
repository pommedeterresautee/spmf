package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.IDList;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.IDListStandard_Map;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.Position;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.tries.TrieNode;

/**
 * Creator of a IdList based on a hashmap of arraylists.
 *
 * Copyright Antonio Gomariz Peñalver 2013
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author agomariz
 */
public class IdListCreatorStandard_Map implements IdListCreator {

    /**
     * Static reference in order to make the class singleton.
     */
    private static IdListCreatorStandard_Map instance = null;

    /**
     * It removes the static fields.
     */
    public static void clear() {
        instance = null;
    }

    /**
     * Standard Constructor.
     */
    private IdListCreatorStandard_Map() {
    }

    /**
     * Get the static reference of the singleton IdList based on entries of 
     * arraylists.
     * @return the instance of this singleton
     */
    public static IdListCreator getInstance() {
        if (instance == null) {
            instance = new IdListCreatorStandard_Map();
        }
        return instance;
    }

    /**
     * It creates an empty IdList of entries of arraylists.
     * @return the idlist
     */
    public IDList create() {
        Map<Integer, List<Position>> sequencePositionsEntries = new HashMap<Integer, List<Position>>();
        return new IDListStandard_Map(sequencePositionsEntries);
    }

    /**
     * It adds to an Idlist of entries of arraylists an appearance <sid,<tid,item position>>
     */
    public void addAppearance(IDList idlist, Integer sequence, Integer timestamp, Integer item) {
        IDListStandard_Map id = (IDListStandard_Map) idlist;
        id.addAppearance(sequence, new Position(timestamp, item));
    }

    /**
     * It adds to an Idlist of entries of arraylists several appearances in a same
     * sequence <sid, {<tid_1,item1 position>,<tid_2, item2 position>,
     * ...,<tid_n, item2 position>}>
     */
    public void addAppearancesInSequence(IDList idlist, Integer sequence, List<Position> itemsets) {
        IDListStandard_Map id = (IDListStandard_Map) idlist;
        id.addAppearancesInSequence(sequence, itemsets);
    }

    @Override
    public void initializeMaps(Map<Item, TrieNode> frequentItems, Map<Item, Map<Integer, List<Integer>>> projectingDistance, Map<Integer, Integer> sequenceSize, Map<Integer, List<Integer>> sequenceItemsetsSize) {
        for (Item frecuente : frequentItems.keySet()) {
            TrieNode node = frequentItems.get(frecuente);
            Map<Integer, List<Integer>> sequenceElementsProjectingByItemMap = projectingDistance.get(frecuente);
            int totalNumberOfProjectedElements = 0;
            for (Integer s : sequenceElementsProjectingByItemMap.keySet()) {
                List<Integer> elementsProjectedByPatternInSequence = sequenceElementsProjectingByItemMap.get(s);
                for (int x : elementsProjectedByPatternInSequence) {
                    totalNumberOfProjectedElements += sequenceSize.get(s) - x;
                }
            }
            node.getChild().getIdList().setTotalElementsAfterPrefixes(totalNumberOfProjectedElements);
        }
        IDList id = new IDListStandard_Map();
        id.SetOriginalSequenceLengths(sequenceSize);
    }

    @Override
    public void updateProjectionDistance(Map<Item, Map<Integer, List<Integer>>> projectingDistance, Item item, int id, int itemsetCount, int itemsCount) {
        Map<Integer, List<Integer>> associatedMap = projectingDistance.get(item);
        if (associatedMap == null) {
            associatedMap = new HashMap<Integer, List<Integer>>();
            projectingDistance.put(item, associatedMap);
        }
        List<Integer> itemscount = associatedMap.get(id);
        if (itemscount == null) {
            itemscount=new ArrayList<Integer>();
            associatedMap.put(id, itemscount);
        }
        itemscount.add(itemsCount);
    }
}
