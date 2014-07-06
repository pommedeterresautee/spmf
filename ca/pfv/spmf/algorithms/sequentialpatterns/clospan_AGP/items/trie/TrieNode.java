package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.trie;

import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.abstractions.ItemAbstractionPair;


/**
 * Class that implement a bucked for the nodes that are pointed by a Trie.
 * Each node has a pair <abstraction,item> and a child Trie where its children appear.
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
public class TrieNode implements Comparable {
    /**
     * Element of the pattern kept by the Node
     */
    private ItemAbstractionPair pair;
    /**
     * Trie where the children of this node as well as the informative data 
     * related to the pattern referred by this node appear
     */
    private Trie child;
    /**
     * Flag indicating if this node has already been traversed
     */
    private boolean alreadyExplored;

    /**
     * Standard Constructor
     */
    public TrieNode() {
    }

    /**
     * Creation of a TrieNode by means of only the pair component
     * @param pair ItemAbstractionPair to insert in the node
     */
    public TrieNode(ItemAbstractionPair pair) {
        this.pair = pair;
    }

    /**
     * Creation of a TrieNode by means of both pair and Trie components
     * @param pair ItemAbstractionPair to insert in the node
     * @param child Trie to insert in the node
     */
    public TrieNode(ItemAbstractionPair pair, Trie child) {
        this.pair = pair;
        this.child = child;
        this.alreadyExplored=false;
    }
    
    /**
     * Creation of a TrieNode by means of both pair and Trie components and
     * indicating if the node has already been traversed
     * @param pair ItemAbstractionPair to insert in the node
     * @param child Trie to insert in the node
     * @param alreadyExplored 
     */
    public TrieNode(ItemAbstractionPair pair, Trie child, boolean alreadyExplored) {
        this.pair = pair;
        this.child = child;
        this.alreadyExplored=alreadyExplored;
    }

    /**
     * Get the Trie kept in the TrieNode
     * @return the trie
     */
    public Trie getChild() {
        return child;
    }

    /**
     * It updates the Trie component of the TrieNode by the Trie given 
     * as parameter
     * @param child Trie to insert in the node
     */
    public void setChild(Trie child) {
        this.child = child;
    }

    /**
     * Get the ItemAbstractionPair object kept in the TrieNode
     * @return the object
     */
    public ItemAbstractionPair getPair() {
        return pair;
    }

    /**
     * Update the ItemAbstractionPair component of the TrieNode by 
     * the pair given as parameter
     * @param par the pair to be inserted in the node
     */
    public void setPair(ItemAbstractionPair par) {
        this.pair = par;
    }

    /**
     * It removes all the descendants tries appearing below this NodeTrie
     */
    public void clear() {
        child.removeAll();
        child = null;
        pair = null;
    }

    /**
     * Compare this TrieNode with either another TrieNode or a 
     * ItemAbstractionPair object. They are compared by their pair components
     * @param o another TrieNode or ItemAbstractionPair
     * @return 0 if equal, -1 if smaller, otherwise 1
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof TrieNode) {
            TrieNode t = (TrieNode) o;
            return this.getPair().compareTo(t.getPair());
        } else if (o instanceof ItemAbstractionPair) {
            ItemAbstractionPair pair = (ItemAbstractionPair)o;
            return this.getPair().compareTo(pair);
        }else if (o instanceof Item){
            return this.getPair().getItem().compareTo((Item)o);
        }else{
            throw new RuntimeException("Error comparing a TrieNode with an object"
                    + " different from a TrieNode or an ItemAbstractionPair");
        }
    }

    /**
     * It answers to the question if the nodeTrie has already been traversed
     * @return true if already traversed, otherwise false.
     */
    public boolean isAlreadyExplored() {
        return alreadyExplored;
    }

    /**
     * It updates the flag that indicates if the NodeTrie has already 
     * been traversed
     * @param alreadyExplored the value (true/false) to be set
     */
    public void setAlreadyExplored(boolean alreadyExplored) {
        this.alreadyExplored = alreadyExplored;
    }
    
    /**
     * Get the string representation of a NodeTrie
     * @return the string representation
     */
    @Override
    public String toString(){
        StringBuilder s= new StringBuilder("{").append(pair.toString()).append("}, [");
        if(child==null)
            s.append("NULL");
        else
            s.append(child.toString());
        s.append("]");
        return s.toString();
    }
}
