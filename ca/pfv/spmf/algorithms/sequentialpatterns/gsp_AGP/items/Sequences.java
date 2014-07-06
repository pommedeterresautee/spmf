
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.Pattern;

/** Inspired in SPMF
 * This class implements a list of frequent sequence lists (or frequent 
 * pattern lists) that it is organized by levels.
 * That level contains all of sequences that have a concrete number of items.
 * Therefore, we allocate 1-sequences in level 1, 2-sequences in level 2,
 * and so forth...
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
public class Sequences {

    /**
     * List of lists, where each list contains the frequent patterns of a concrete
     * length
     */
    public List<List<Pattern>> levels = new ArrayList<List<Pattern>>();
    /**
     * Number of frequent sequences
     */
    public int numberOfFrequentSequences = 0;
    /**
     * String header that can be add with informative purposes
     */
    private String string;

    public Sequences(String string) {
        levels.add(new ArrayList<Pattern>());
        this.string = string;
    }

    /**
     * Print all the levels of the structure in the standard output
     */
    public void printFrequentSequences() {
        System.out.println(toString());
    }

    /**
     * Method that give us in a string the content of all Sequences structure
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(string);
        int levelCount = 0;
        for (List<Pattern> level : levels) {
            sb.append("\n***Level ").append(levelCount).append("***\n\n");
            for (Pattern sequence : level) {
                sb.append(sequence.toString());
                sb.append('\n');
            }
            levelCount++;
        }
        return sb.toString();
    }
    
    /**
     * Method that give us in a string the content of all Sequences structure in
     * a SPMF format
     * @return the string
     */
    public String toStringToFile() {
        StringBuilder sb = new StringBuilder();
        int levelCount = 0;
        for (List<Pattern> level : levels) {
            for (Pattern sequence : level) {
                sb.append(sequence.toStringToFile());
                sb.append('\n');
            }
            levelCount++;
        }
        return sb.toString();
    }

    /**
     * Method that adds a sequence in a given level
     * @param sequence a sequence to add
     * @param levelIndex the level where the sequence must be
     */
    public void addSequence(Pattern sequence, int levelIndex) {
        //while our maximum level is less than the given one, we keep adding new levels
        while (levels.size() <= levelIndex) {
            levels.add(new ArrayList<Pattern>());
        }
        levels.get(levelIndex).add(sequence);
        //We update the number of frequent sequences
        numberOfFrequentSequences++;
    }

    /**
     * Method that adds a sequence set in a given level
     * @param sequences a sequence list to add
     * @param levelIndex the level where the sequence must be
     */
    public void addSequences(List<Pattern> sequences, int levelIndex){
        for(Pattern pattern:sequences)
            addSequence(pattern, levelIndex);
    }

    /**
     * Get the frequent sequences that appear in a particular level
     * @param index the frequent sequences of that level
     * @return  the list of pattern of this level
     */
    public List<Pattern> getLevel(int index) {
        return levels.get(index);
    }

    /**
     * It gives us the total number of levels. Notice that we discount the level 0
     * @return the number of levels.
     */
    public int getLevelCount() {
        return levels.size()-1;
    }

    /**
     * It obtains a list of pattern lists, being each pattern list the frequent sequences with a concrete length
     * For example, at position 1, it is the list of patterns of size 1.
     *              at position 2, it is the list of patterns of size 2
     *              ...
     *              etc.
     * @return the list of pattern lists.
     */
    public List<List<Pattern>> getLevels() {
        return levels;
    }

    /**
     * It returns the total number of sequences
     * @return  the total number of sequences
     */
    public int size() {
        /*int total = 0;
        for (List<Pattern> level : levels) {
            total = total + level.size();
        }
        return total;*/
        return numberOfFrequentSequences;
    }

    /**
     * Method to sort each level of the structure
     */
    public void sort() {
        for (List<Pattern> level : levels) {
            Collections.sort(level);
        }
    }

    /**
     * It completely deletes a concrete level of sequences
     * @param i 
     */
    public void delete(int i) {
        numberOfFrequentSequences-=levels.get(i).size();
        levels.get(i).clear();
        
    }

    /**
     * It clears the structure
     */
    public void clear() {
        for(List<Pattern> level:levels){
            level.clear();
        }
        levels.clear();
        levels=null;
        numberOfFrequentSequences=0;
    }
}
