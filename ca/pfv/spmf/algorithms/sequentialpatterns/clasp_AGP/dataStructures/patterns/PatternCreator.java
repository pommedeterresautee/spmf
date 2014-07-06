package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.patterns;

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.abstracciones.ItemAbstractionPair;

/**
 * This class is the implementation of a creator of patterns.
 * By means this class, different kind of patterns can be used for this algorithm.
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
public class PatternCreator {

    private static PatternCreator instance = null;

    public static void sclear() {
        instance=null;
    }

    private PatternCreator() {
    }

    public static PatternCreator getInstance() {
        if (instance == null) {
            instance = new PatternCreator();
        }
        return instance;
    }

    /**
     * It creates a pattern from a list of pairs of the form <abstraction, item>.
     * @param elements a list of pairs
     * @return  the resulting pattern
     */
    public Pattern createPattern(List<ItemAbstractionPair> elements) {
        Pattern newPattern;
        if (elements == null) {
            newPattern = new Pattern();
        } else {
            newPattern = new Pattern(elements);
        }
        return newPattern;
    }

    /**
     * It creates a pattern of only one item.
     * @param pair the pair to be used to create the pattern
     * @return the resulting pattern
     */
    public Pattern createPattern(ItemAbstractionPair pair) {
        List<ItemAbstractionPair> elements = new ArrayList<ItemAbstractionPair>();
        elements.add(pair);
        return createPattern(elements);
    }

    /**
     * It creates an empty pattern
     * @return the resulting pattern
     */
    public Pattern createPattern() {
        List<ItemAbstractionPair> pairList = null;
        return createPattern(pairList);
    }

    /**
     * It concatenates a pair to the given pattern
     * @param p1 pattern where the pair has to be concatenated
     * @param pair pair to concatenate with the pattern given as parameter
     * @return the resulting pattern
     */
    public Pattern concatenate(Pattern p1, ItemAbstractionPair pair) {
        if (p1 == null) {
            if (pair == null) {
                return null;
            }
            return createPattern(pair);
        }
        if (pair == null) {
            return p1;
        }
        return p1.concatenate(pair);
    }
}
