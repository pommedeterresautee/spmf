
package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.ItemAbstractionPair;

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

    public static void sclear() {
        patternPool.clear();
    }

    private static Map<Pattern, Pattern> patternPool = new HashMap<Pattern, Pattern>();

    /**
     * It creates a pattern from a list of pair <abstraction, item>.
     * @param elements a list of elements.
     * @return the pattern created.
     */
    public Pattern createPattern(List<ItemAbstractionPair> elements){
        Pattern newPattern = new Pattern(elements);
        /*Pattern existingPattern = patternPool.get(newPattern);
        if(existingPattern==null){
            existingPattern=newPattern;
            patternPool.put(newPattern);
        }
        return existingPattern;*/
        return newPattern;
    }

    /**
     * It creates a pattern of only one item.
     * @param pair a pair
     * @return the pattern created
     */
    public Pattern createPattern(ItemAbstractionPair pair){
        List<ItemAbstractionPair> elements = new ArrayList<ItemAbstractionPair>();
        elements.add(pair);
        return createPattern(elements);
    }
    
    /**
     * It creates an empty pattern.
     * @return the pattern just created.
     */
    public Pattern createPattern(){
        return new Pattern();
    }
}
