package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.patterns;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.abstracciones.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;

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
     * Get the string representation of this pattern
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            result.append(elements.get(i).toString());
        }
        if (appearingIn.size() > 0) {
            result.append("\t(");
            result.append(appearingIn.size()).append(')');
            result.append("\t[");
            for (int i = 0; i < appearingIn.size(); i++) {
                result.append(getSupport()).append(", ");
            }
            result.deleteCharAt(result.length() - 1);
            result.deleteCharAt(result.length() - 1);
            result.append("]");
        }
        return result.toString();
    }
    
    /**
     * Get the string representation of this pattern. Adjusted to SPMF format.
     * @return the string representation
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
        result.append(getSupport());
        return result.toString();
    }
    
    public int getSupport(){
        return appearingIn.cardinality();
    }

    /**
     * It clones a pattern
     * @return the clone
     */
    public Pattern clonePatron() {
        List<ItemAbstractionPair> elementsCopy = new ArrayList<ItemAbstractionPair>(elements);
        Pattern clon = new Pattern(elementsCopy);
        return clon;
    }

    /**
     * It obtains the elements of the pattern
     * @return the list of elements
     */
    public List<ItemAbstractionPair> getElements() {
        return elements;
    }

    /**
     * It obtains the Ith element of the pattern
     * @param i the position
     * @return the ith element
     */
    public ItemAbstractionPair getIthElement(int i) {
        return elements.get(i);
    }

    public ItemAbstractionPair getPenultimoComponente() {
        if (size() > 1) {
            return getIthElement(size() - 2);
        }
        return null;
    }

    /**
     * It obtains the last but one element of the pattern
     * @return the last but one element
     */
    public ItemAbstractionPair getLastButOneElement() {
        if (size() > 0) {
            return getIthElement(size() - 1);
        }
        return null;
    }

    /**
     * Setter method to set the elements of the pattern
     * @param elements a list of elements
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
     * @return  the number of items
     */
    public int size() {
        return elements.size();
    }

    /**
     * Compare this pattern with another.
     * Since we always compare elements which belong to the same equivalence class
     * we only make the comparison of the last items of both patterns.
     * @param o the other pattern
     * @return 0 if equal, -1 if smaller, otherwise 1
     */
    @Override
    public int compareTo(Pattern o) {
        return getIthElement(size() - 1).compareTo(o.getIthElement(o.size() - 1)); 
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pattern) {
            Pattern p = (Pattern) o;
            if (this.compareTo(p) == 0) {
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
     * It informs us if this pattern is a prefix of the argument given pattern.
     * @param p the pattern
     * @return true if yes, otherwise false
     */
    public boolean isPrefix(Pattern p) {
        boolean output = false;
        List<ItemAbstractionPair> pElements = new ArrayList<ItemAbstractionPair>(p.getElements());
        pElements.remove(pElements.size() - 1);
        if (pElements.get(pElements.size() - 1).equals(getIthElement(size() - 1))) {
            output = true;
        }
        return output;
    }

    /**
     * It returns the list of sequence IDs where the pattern appears.
     * @return the list of sequence IDs
     */
    public BitSet getAppearingIn() {
        return appearingIn;
    }

    /**
     * it set the list of sequence IDs where the pattern appears.
     * @param appearingIn 
     */
    public void setAppearingIn(BitSet appearingIn) {
        this.appearingIn = appearingIn;
    }

    public void clear() {
        elements.clear();
        appearingIn.clear();
    }

    /**
     * It concatenates the given pair as a last element of the pattern
     * @param pair the pair
     * @return  the resulting pattern
     */
    public Pattern concatenate(ItemAbstractionPair pair) {
        Pattern result = clonePatron();
        result.add(pair);
        return result;
    }

    /**
     * It concatenates the given pattern to the current pattern
     * @param pattern
     * @return the resulting pattern
     */
    public Pattern concatenar(Pattern pattern) {
        Pattern result = clonePatron();
        result.getElements().addAll(pattern.getElements());
        return result;
    }

    /**
     * It answers if the current Pattern is a subpattern of another one
     * given as parameter.
     * @param abstractionCreator an abstraction creator
     * @param p pattern to check if it is a superpattern of the current one
     * @return true if it is a subpattern otherwise, false
     */
    public boolean isSubpattern(AbstractionCreator abstractionCreator, Pattern p) {
        //We initialize all the positions values to 0
        List<Integer> positions = new ArrayList<Integer>(p.size());
        for (int i = 0; i < size(); i++) {
            positions.add(0);
        }
        //And we call to the method of abstractionCreator
        return abstractionCreator.isSubpattern(this, p, 0, positions);
    }
}
