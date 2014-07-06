/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns;

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.ItemAbstractionPair;

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

    /**
     * Static reference for make this class singleton
     */
    private static PatternCreator instance = null;
    //private static Map<Pattern, Pattern> patternPool = new HashMap<Pattern, Pattern>();

    private PatternCreator() {
    }

    /**
     * Get the only instance of PatternCreator is a singleton class
     * @return the instance
     */
    public static PatternCreator getInstance() {
        if (instance == null) {
            instance = new PatternCreator();
        }
        return instance;
    }

    /**
     * It creates a pattern given a set of elements
     * @param elements the list of element
     * @return the created pattern
     */
    public Pattern createPattern(List<ItemAbstractionPair> elements) {
        Pattern newPattern = null;
        if (elements == null) {
            newPattern = new Pattern();
        } else {
            newPattern = new Pattern(elements);
        }
        /*Pattern existingPattern = patternPool.get(newPattern);
        if (existingPattern == null) {
            existingPattern = newPattern;
            patternPool.put(newPattern, newPattern);
        }
        return existingPattern;*/
        return newPattern;
    }

    /**
     * It creates a pattern just composed of only one element
     * @param pair the pair to be used to create the pattern
     * @return the created pattern
     */
    public Pattern createPattern(ItemAbstractionPair pair) {
        List<ItemAbstractionPair> elements = new ArrayList<ItemAbstractionPair>();
        elements.add(pair);
        return createPattern(elements);
    }

    /**
     * Standard method for creating a pattern
     * @return the pattern created
     */
    public Pattern createPattern() {
        List<ItemAbstractionPair> list = null;
        return createPattern(list);
    }

    public void clear() {
        //patternPool.clear();
    }

    public static void sclear() {
        //patternPool.clear();
    }
}