/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.patterns;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.ItemAbstractionPair;

/**
 * Implementation of pattern structure. We define it as a list of pairs <abstraction, item>.
 * Besides, a bitSet appearingIn denotes the sequences where the pattern appears.
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
public class Pattern implements Comparable<Pattern> {

    /**
     * List of pairs <abstraction, item> that define the pattern
     */
    private List<ItemAbstractionPair> elements;
    /**
     * Set of sequence IDs indicating where the pattern appears
     */
    private BitSet appearingIn;

    /**
     * Standard constructor
     */
    public Pattern() {
        this.elements = new ArrayList<ItemAbstractionPair>();
        this.appearingIn = new BitSet();
    }

    /**
     * New pattern from a list of pairs <abstraction, item>
     * @param elements 
     */
    public Pattern(List<ItemAbstractionPair> elements) {
        this.elements = elements;
        this.appearingIn = new BitSet();
    }

    /**
     * New pattern from a single pair <abstraction, item>
     * @param pair 
     */
    public Pattern(ItemAbstractionPair pair) {
        this.elements = new ArrayList<ItemAbstractionPair>();
        this.elements.add(pair);
        this.appearingIn = new BitSet();
    }
    
    /**
     * Method to obtain the support of a pattern
     * @return  the support as an integer.
     */
    public int getSupport(){
        return appearingIn.cardinality();
    }

    /**
     * Get the string representation of a pattern
     * @return a string.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            result.append(elements.get(i).toString());
        }
        result.append("\t(").append(getSupport()).append(')');
        result.append("\t[");
        for (int i = appearingIn.nextSetBit(0); i >=0; i=appearingIn.nextSetBit(i+1)) {
            result.append(i).append(", ");
        }
        result.deleteCharAt(result.length() - 1);
        result.deleteCharAt(result.length() - 1);
        result.append("]");
        return result.toString();
    }
    
    /**
     * Get the string representation of itemset. Adjusted to SPMF format.
     * @return the string representation.
     */
    public String toStringToFile() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            if(i==elements.size()-1){
                if(i!=0)
                    result.append(elements.get(i).toStringToFile());
                else
                    result.append(elements.get(i).getItem());
                result.append(" -1");
            }
            else if(i==0){
                result.append(elements.get(i).getItem());
            }else{
                result.append(elements.get(i).toStringToFile());
            }
            
        }
        result.append(" #SUP: ");
        result.append(appearingIn.cardinality());
        return result.toString();
    }

    /**
     * It clones this pattern.
     * @return a clone of the pattern.
     */
    public Pattern clonePattern() {
        PatternCreator patternCreator = new PatternCreator();
        List<ItemAbstractionPair> elementsCopy = new ArrayList<ItemAbstractionPair>(elements);
        Pattern clon = patternCreator.createPattern(elementsCopy);
        return clon;
    }

    /**
     * It obtains the elements of the pattern.
     * @return  the elements contained in the pattern as a list.
     */
    public List<ItemAbstractionPair> getElements() {
        return elements;
    }

    /**
     * It obtains the Ith element of the pattern
     * @param i  the position i
     * @return the element.
     */
    public ItemAbstractionPair getIthElement(int i) {
        return elements.get(i);
    }

    /**
     * It obtains the last element of the pattern.
     * @return the last element.
     */
    public ItemAbstractionPair getLastElement() {
        if (size() > 0) {
            return getIthElement(size() - 1);
        }
        return null;
    }

    /**
     * Setter method to set the elements of the pattern
     * @param elements 
     */
    public void setElements(List<ItemAbstractionPair> elements) {
        this.elements = elements;
    }

    /**
     * It adds an item with its abstraction in the pattern. The new pair is 
     * added in the last position of the pattern.
     * @param pair 
     */
    public void add(ItemAbstractionPair pair) {
        this.elements.add(pair);
    }

    /**
     * It returns the number of items contained in the pattern
     * @return the size (int).
     */
    public int size() {
        return elements.size();
    }

    /**
     * This methods compares this pattern with another pattern according to the lexical order.
     * We use a lexicographic order.
     * @param arg the other pattern
     * @return -1 if this pattern is smaller, 0 if equals, otherwise 1.
     */
    @Override
    public int compareTo(Pattern arg) {
        List<ItemAbstractionPair> elementsOfGreaterPattern, elementOfSmallerPattern;
        if (getElements().size() >= arg.getElements().size()) {
            elementsOfGreaterPattern = getElements();
            elementOfSmallerPattern = arg.getElements();
        } else {
            elementOfSmallerPattern = getElements();
            elementsOfGreaterPattern = arg.getElements();
        }
        for (int i = 0; i < elementOfSmallerPattern.size(); i++) {
            int comparison = elementOfSmallerPattern.get(i).compareTo(elementsOfGreaterPattern.get(i));
            if (comparison != 0) {
                return comparison;
            }
        }
        if (elementsOfGreaterPattern.size() == elementOfSmallerPattern.size()) {
            return 0;
        }
        if (getElements().size() < arg.getElements().size()) {
            return -1;
        }
        return 1;
    }

    @Override
    public boolean equals(Object arg) {
        if (arg instanceof Pattern) {
            Pattern pattern = (Pattern) arg;
            if (this.compareTo(pattern) == 0) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.elements != null ? this.elements.hashCode() : 0);
        return hash;
    }

    /**
     * It returns the list of sequence IDs where the pattern appears.
     * @return the list of sequence IDs.
     */
    public BitSet getAppearingIn() {
        return appearingIn;
    }

    /**
     * It sets the list of sequence IDs where the pattern appears.
     * @param appearingIn the list of sequence ids as a bitset.
     */
    public void setAppearingIn(BitSet appearingIn) {
        this.appearingIn = appearingIn;
    }
}
