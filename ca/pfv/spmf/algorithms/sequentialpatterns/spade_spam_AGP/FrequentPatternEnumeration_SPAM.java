package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP;

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.ItemAbstractionPairCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.PatternCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.Saver;

/**
 * This is an implementation of the main methods of SPAM algorithm. We keep open
 * the decision of which IdList to use. In the original paper, the authors use a
 * bitmap implementation. We have such implementation (IDListFatBitmap) but we 
 * also have other two ones (both based on hash maps, one with bitsets (IDListBitmap)
 * and another with arraylists (IDListStandard_Map)).
 *
 * NOTE: This implementation saves the pattern to a file as soon as they are
 * found or can keep the pattern into memory, depending on what the user choose.
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
public class FrequentPatternEnumeration_SPAM{
    /**
     * The absolute minimum support threshold, i.e. the minimum number of
     * sequences where the patterns have to be
     */
    private double minSupportAbsolute;
    /**
     * Number of frequent patterns found by the algorithm. Initially set to zero.
     */
    private int frequentPatterns = 0;
    /**
     * The pattern creator.
     */
    private final PatternCreator patternCreator;
    /**
     * Saver variable to decide where the user want to save the results, if it
     * the case
     */
    private Saver saver;

    /**
     * Standard constructor of the class.
     * @param minSupportAbsolute The absolute minimum support  threshold
     * @param saver Saver object to decide where the user want to save the results, if it
     * the case
     */
    public FrequentPatternEnumeration_SPAM(double minSupportAbsolute, Saver saver) {
        this.minSupportAbsolute = minSupportAbsolute;
        this.patternCreator = PatternCreator.getInstance();
        this.saver=saver;
    }

    /**
     * Execution of the search of frequent patterns.
     * @param equivalenceClass The equivalence class from we start to search for.
     * @param keepPatterns Flag to indicate if we want to keep the patterns found.
     * @param verbose Flag for debugging purposes
     */
    public void execute(EquivalenceClass equivalenceClass, boolean keepPatterns, boolean verbose) {
        int numberOfMembersInEC = equivalenceClass.getClassMembers().size();
        for (int i = 0; i < numberOfMembersInEC; i++) {
            //For each member of the given equivalence class
            EquivalenceClass ec = equivalenceClass.getIthMember(i);
            //We call to the main method of the algorithm for that equivalence class
            dfs_pruning(ec, equivalenceClass.getClassMembers(), equivalenceClass.getClassMembers(),i+1, keepPatterns);
        }
    }

    /**
     * Main method of SPAM algorithm. For each processed patterns, the algorithm
     * tries to make a s-extension first, and then, once it found all the possible
     * s-extension, it tries to make an i-extension. The method receives two set
     * of equivalence classes, one with the frequent items that can be used for
     * making s-extensions and another one for making i-extensions.
     * @param currentClass The current class whose identifier we are trying to extend.
     * @param sequenceExtensions The set of equivalence classes, with frequent 
     * items as identifiers, that we use as possible s-extensions of the current 
     * pattern that we are processing.
     * @param itemsetsExtensions The set of equivalence classes, with frequent 
     * items as identifiers, that we use as possible i-extensions of the current 
     * pattern that we are processing.
     * @param beginning The beginning index from where we can use the elements of
     * itemsetsExtensions set
     * @param keepPatterns Flag indicating if we are interesting in saving the 
     * frequent patterns that we find.
     */
    private void dfs_pruning(EquivalenceClass currentClass, List<EquivalenceClass> sequenceExtensions, List<EquivalenceClass> itemsetsExtensions,int beginning, boolean keepPatterns) {
        //We start increasing the number of frequent patterns
        frequentPatterns++;
        
        //We get the class identifier
        Pattern classIdentifier = currentClass.getClassIdentifier();

        //Initialization of new sets
        List<EquivalenceClass> new_sequenceExtension = new ArrayList<EquivalenceClass>();
        List<EquivalenceClass> new_itemsetExtension = new ArrayList<EquivalenceClass>();
        List<EquivalenceClass> new_classes = new ArrayList<EquivalenceClass>();

        //Clone for the class identifier
        Pattern clone = classIdentifier.clonePattern();
        //For all the elements of sequenceExtensions
        for (EquivalenceClass eq : sequenceExtensions) {
            //We create a new pattern based in the elements of the clone
            Pattern extension = patternCreator.createPattern(new ArrayList<ItemAbstractionPair>(clone.getElements()));
            //And we extend it with the only element of the eq class identifier
            ItemAbstractionPair newPair = eq.getClassIdentifier().getLastElement();
            extension.add(newPair);

            /*
             * We make the join operation between both patterns in order to know 
             * the appearances of the new pattern and its support.
             */
            IDList newIdList = currentClass.getIdList().join(eq.getIdList(), false, (int) minSupportAbsolute);
            //If the new pattern is frequent
            if (newIdList.getSupport() >= minSupportAbsolute) {
                //We insert it its appearances
                newIdList.setAppearingSequences(extension);
                // and we keep the pattern if the flag is activated
                if (keepPatterns) {
                    saver.savePattern(extension);                    
                }

                //We create a new class for the new pattern
                EquivalenceClass newClass = new EquivalenceClass(extension);
                //we inserted the IdList that we computed
                newClass.setIdList(newIdList);
                //And we insert the new class in the set of new classes
                new_classes.add(newClass);
                
                /*Normally, in the original algorithm we would insert in the new
                 *set of sequence extensions the same eq class that we used for
                 * obtaining the new pattern. The problem with this is that if we
                 * do it, the Idlist it remains the same. Since a IdList is denser
                 * (with more values) with shorter patterns (because is easier to
                 * find an appearance in a sequence), we can put to the pattern
                 * of the equivalence class of eq, the new idlist generated. Note
                 * that for the items in future s-extensions of this new pattern,
                 * we can directly pass the IdList recently computed since all 
                 * the appearances of eq identifier will be after an appearance of
                 * the current class. 
                 * Therefore, in order to shrink the IdLists and their computations
                 * we create a new equivalence class with the same eq identifier,
                 * and the idlist recently created.
                 */
                EquivalenceClass newEq = new EquivalenceClass(eq.getClassIdentifier(), newIdList);
                /* And we add this new class as a possible future s-extension of 
                 * the new pattern.
                 */
                new_sequenceExtension.add(newEq);
            }
        }

        int sequenceExtensionSize = new_sequenceExtension.size();
        //For all the elements valuables as future s-extensions
        for (int i = 0; i < sequenceExtensionSize; i++) {
            //we get the new pattern
            EquivalenceClass newClass = new_classes.get(i);

            /* And we make a recursive call to dfs_pruning with the new sequence 
             * extension. Besides we establish the same set as the set which we will
             * make the i-extensions, but beginning from the (i+1)-th element
             */
            dfs_pruning(newClass, new_sequenceExtension, new_sequenceExtension,i+1, keepPatterns);
            /* Once we had finished the search for this patterns and their children,
             * we can to remove that class (and its desdendants) from the memory
             */
            newClass.clear();
        }

        /* We clear the set of the new classes discovered since we need to store
         * those that we will find making i-extensions
         */
        new_classes.clear();

        /*
         * From the beginning index to the last equivalence class appearing in 
         * the itemset extension set
         */
        for (int k = beginning; k < itemsetsExtensions.size(); k++) {
            EquivalenceClass eq = itemsetsExtensions.get(k);
            //We create a new pattern with the elements of the current class identifier
            Pattern extension = patternCreator.createPattern(new ArrayList<ItemAbstractionPair>(clone.getElements()));
            //And we add it the current item of itemset extension set
            ItemAbstractionPair newPair = ItemAbstractionPairCreator.getInstance().getItemAbstractionPair(eq.getClassIdentifier().getLastElement().getItem(), AbstractionCreator_Qualitative.getInstance().createAbstraction(true));
            extension.add(newPair);

            /*
             * We make the join operation between both patterns in order to know 
             * the appearances of the new pattern and its support.
             */
            IDList newIdList = currentClass.getIdList().join(eq.getIdList(), true, (int) minSupportAbsolute);
             //If the new pattern is frequent
            if (newIdList.getSupport() >= minSupportAbsolute) {
                //We insert it its appearances
                newIdList.setAppearingSequences(extension);
                // and we keep the pattern if the flag is activated
                if (keepPatterns) {                    
                    saver.savePattern(extension);
                }

                //We create a new class for the new pattern
                EquivalenceClass newClass = new EquivalenceClass(extension);
                //we inserted the IdList that we computed
                newClass.setIdList(newIdList);
                //And we insert the new class in the set of new classes
                new_classes.add(newClass);
                
                /*Normally, in the original algorithm we would insert in the new
                 *set of itemset extensions the same eq class that we used for
                 * obtaining the new pattern. The problem with this is that if we
                 * do it, the Idlist it remains the same. Since a IdList is denser
                 * (with more values) with shorter patterns (because is easier to
                 * find an appearance in a sequence), we can put to the pattern
                 * of the equivalence class of eq, the new idlist generated. Note
                 * that for the items in future i-extensions of this new pattern,
                 * we can directly pass the IdList recently computed since all 
                 * the appearances of eq identifier will be after an appearance of
                 * the current class. 
                 * Therefore, in order to shrink the IdLists and their computations
                 * we create a new equivalence class with the same eq identifier,
                 * and the idlist recently created.
                 */
                EquivalenceClass newEq = new EquivalenceClass(eq.getClassIdentifier(), newIdList);
                /* And we add this new class as a possible future s-extension of 
                 * the new pattern.
                 */
                new_itemsetExtension.add(newEq);
            }
        }

        int itemsetExtensionSize = new_itemsetExtension.size();
        //For all the elements valuables as future i-extensions
        for (int i = 0; i < itemsetExtensionSize; i++) {
            //we get the new pattern
            EquivalenceClass newClass = new_classes.get(i);
            /* And we make a recursive call to dfs_pruning with the new itemset 
             * extension. The beginning of the itemset extension set will be 
             * starting from the (i+1)-th element.
             */
           dfs_pruning(newClass, new_sequenceExtension, new_itemsetExtension,i+1, keepPatterns);
           
           newClass.clear();
        }
    }

    /**
     * It returns the number of frequent patterns found by the last execution of
     * the algorithm.
     * @return the number of frequent patterns found.
     */
    public int getFrequentPatterns() {
        return frequentPatterns;
    }
    
    public void setFrequentPatterns(int frequentPatterns) {
        this.frequentPatterns = frequentPatterns;
    }
}
