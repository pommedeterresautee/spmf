package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP;

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;

/**
 * This is the definition of an equivalence class.
 * Each one is composed by a pattern that its the class identifier and the 
 * IdList associated with that pattern. Besides, it also contains all the
 * equivalence class children that are supersequences (by means i-extension 
 * or s-extension) of the class identifier.
 * In this way a complete structure of classes can be created from the root class
 * to the longest ones.
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
public class EquivalenceClass implements Comparable<EquivalenceClass> {

    /**
     * Pattern that is the identifier of the equivalence class, the main representant.
     */
    private Pattern classIdentifier;    
    /**
     * IdList which corresponds to classIdentifier pattern.
     */
    private IDList idList;
    /**
     * All the superpatterns of classIdentifier that are obtained by means of 
     * making either an i-extension or s-extension
     */
    private List<EquivalenceClass> classMembers;

    /**
     * Constructor from a pattern.
     * @param classIdentifier The pattern to be use as the equivalence class identifier
     */
    public EquivalenceClass(Pattern classIdentifier) {
        this.classIdentifier = classIdentifier;
        this.classMembers = new ArrayList<EquivalenceClass>();
    }

    /**
     * Constructor from a pattern and an IdList.
     * @param classIdentifier The pattern to be use as the equivalence class identifier
     * @param idList The idList that is associated to that the argument pattern
     */
    public EquivalenceClass(Pattern classIdentifier, IDList idList) {
        this.classIdentifier = classIdentifier;
        this.classMembers = new ArrayList<EquivalenceClass>();
        this.idList = idList;
    }

    /**
     * Constructor from a pattern and a set of equivalence classes
     * @param classIdentifier The pattern to be use as the equivalence class identifier
     * @param classMembers The equivalence classes that are to be the members of 
     * the equivalence class (superpatterns of the pattern given as an argument)
     */
    public EquivalenceClass(Pattern classIdentifier, List<EquivalenceClass> classMembers) {
        this.classIdentifier = classIdentifier;
        this.classMembers = classMembers;
    }

    /**
     * Constructor from a pattern, an IdList and a set of equivvalence classes
     * @param classIdentifier The pattern to be use as the equivalence class identifier
     * @param classMembers The equivalence classes that are to be the members of 
     * the equivalence class (superpatterns of the pattern given as an argument)
     * @param idList The IdList associated with the pattern given as an argument
     */
    public EquivalenceClass(Pattern classIdentifier, List<EquivalenceClass> classMembers, IDList idList) {
        this.classIdentifier = classIdentifier;
        this.classMembers = classMembers;
        this.idList = idList;
    }

    /**
     * Get the members of the equivalence class
     * @return a list of equivalence classes
     */
    public List<EquivalenceClass> getClassMembers() {
        return classMembers;
    }

    /**
     * Set the members of the equivalence class
     * @param classMembers Set of equivalence classes that are going to be the members
     */
    public void setClassMembers(List<EquivalenceClass> classMembers) {
        this.classMembers = classMembers;
    }

    /**
     * Method to add an equivalence class as a new class' member.
     * @param classMember 
     */
    public void addClassMember(EquivalenceClass classMember) {
        this.classMembers.add(classMember);
    }

    /**
     * It gets the ith member of the class.
     * @param i The index of the member in which we are interested
     * @return the ith member
     */
    public EquivalenceClass getIthMember(int i) {
        return this.classMembers.get(i);
    }

    /**
     * It gets the IdList of the Equivalence class' identifier
     * @return the idlist
     */
    public IDList getIdList() {
        return idList;
    }

    /**
     * It sets the IdList of the Equivalence class' identifier
     * @param idList the idlist
     */
    public void setIdList(IDList idList) {
        this.idList = idList;
    }

    /**
     * It gets the equivalence class identifier
     * @return the identifier
     */
    public Pattern getClassIdentifier() {
        return classIdentifier;
    }

    /**
     * It sets the equivalence class identifier
     * @param classIdentifier the identifier
     */
    public void setClassIdentifier(Pattern classIdentifier) {
        this.classIdentifier = classIdentifier;
    }

    /**
     * It makes the comparison between two classes. That comparison is exactly
     * as that one of their identifiers.
     * @param arg the other class
     * @return 0 if equals, -1 if this one is smaller, otherwise 1
     */
    @Override
    public int compareTo(EquivalenceClass arg) {
        return getClassIdentifier().compareTo(arg.getClassIdentifier());
    }

    /**
     * Get the string representation of this equivalence class.
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (int i = 0; i < classIdentifier.size(); i++) {
            result.append(classIdentifier.getIthElement(i).toString()).append(' ');
        }
        result.deleteCharAt(result.length() - 1);
        result.append(']');
        return result.toString();
    }

    /**
     * It cleans all the information for an equivalence class.
     */
    public void clear() {
        /*classMembers.clear();
        idList.clear();
        classIdentifier.clear();*/
        classMembers=null;
        idList=null;
        classIdentifier=null;
    }
}
