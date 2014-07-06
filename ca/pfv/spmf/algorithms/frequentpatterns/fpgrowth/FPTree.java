package ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth;

/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;


/**
 * This is an implementation of a FPTree as used by the FPGrowth algorithm.
 *
 * @see FPNode
 * @see Itemset
 * @see AlgoFPGrowth
 * @author Philippe Fournier-Viger
 */
public class FPTree {
	// List of items in the header table
	List<Integer> headerList = null;
	
	// List of pairs (item, frequency) of the header table
	Map<Integer, FPNode> mapItemNodes = new HashMap<Integer, FPNode>();
	
	// Map that indicates the last node for each item using the node links
	// key: item   value: an fp tree node
	Map<Integer, FPNode> mapItemLastNode = new HashMap<Integer, FPNode>();
	
	// flag that indicate if the tree has more than one path
	boolean hasMoreThanOnePath = false;
	
	// root of the tree
	FPNode root = new FPNode(); // null node

	/**
	 * Constructor
	 */
	FPTree(){	
		
	}

	/**
	 * Method for adding a transaction to the fp-tree (for the initial construction
	 * of the FP-Tree).
	 * @param transaction
	 */
	public void addTransaction(List<Integer> transaction) {
		FPNode currentNode = root;
		// For each item in the transaction
		for(Integer item : transaction){
			// look if there is a node already in the FP-Tree
			FPNode child = currentNode.getChildWithID(item);
			if(child == null){ 
				// there is no node, we create a new one
				FPNode newNode = new FPNode();
				newNode.itemID = item;
				newNode.parent = currentNode;
				// we link the new node to its parrent
				currentNode.childs.add(newNode);
				
				// check if more than one path
				if(!hasMoreThanOnePath && currentNode.childs.size() > 1) {
					hasMoreThanOnePath = true;
				}
				
				// we take this node as the current node for the next for loop iteration 
				currentNode = newNode;
				
				// We update the header table.
				// We check if there is already a node with this id in the header table
				fixNodeLinks(item, newNode);	
			}else{ 
				// there is a node already, we update it
				child.counter++;
				currentNode = child;
			}
		}
	}

	/**
	 * Method to fix the node link for an item after inserting a new node.
	 * @param item  the item of the new node
	 * @param newNode the new node thas has been inserted.
	 */
	private void fixNodeLinks(Integer item, FPNode newNode) {
		// get the latest node in the tree with this item
		FPNode lastNode = mapItemLastNode.get(item);
		if(lastNode != null) {
			// if not null, then we add the new node to the node link of the last node
			lastNode.nodeLink = newNode;
		}
		// Finally, we set the new node as the last node 
		mapItemLastNode.put(item, newNode); 
		
		FPNode headernode = mapItemNodes.get(item);
		if(headernode == null){  // there is not
			mapItemNodes.put(item, newNode);
		}
	}
	
	/**
	 * Method for adding a prefixpath to a fp-tree.
	 * @param prefixPath  The prefix path
	 * @param mapSupportBeta  The frequencies of items in the prefixpaths
	 * @param relativeMinsupp
	 */
	void addPrefixPath(List<FPNode> prefixPath, Map<Integer, Integer> mapSupportBeta, int relativeMinsupp) {
		// the first element of the prefix path contains the path support
		int pathCount = prefixPath.get(0).counter;  
		
		FPNode currentNode = root;
		// For each item in the transaction  (in backward order)
		// (and we ignore the first element of the prefix path)
		for(int i= prefixPath.size()-1; i >=1; i--){ 
			FPNode pathItem = prefixPath.get(i);
			// if the item is not frequent we skip it
			if(mapSupportBeta.get(pathItem.itemID) < relativeMinsupp){
				continue;
			}
			
			// look if there is a node already in the FP-Tree
			FPNode child = currentNode.getChildWithID(pathItem.itemID);
			if(child == null){ 
				// there is no node, we create a new one
				FPNode newNode = new FPNode();
				newNode.itemID = pathItem.itemID;
				newNode.parent = currentNode;
				newNode.counter = pathCount;  // set its support
				currentNode.childs.add(newNode);
				// check if more than one path
				if(!hasMoreThanOnePath && currentNode.childs.size() > 1) {
					hasMoreThanOnePath = true;
				}
				
				currentNode = newNode;
				// We update the header table.
				// and the node links
				fixNodeLinks(pathItem.itemID, newNode);		
			}else{ 
				// there is a node already, we update it
				child.counter += pathCount;
				currentNode = child;
			}
		}
	}

	/**
	 * Method for creating the list of items in the header table, 
	 *  in descending order of support.
	 * @param mapSupport the frequencies of each item (key: item  value: support)
	 */
	void createHeaderList(final Map<Integer, Integer> mapSupport) {
		// create an array to store the header list with
		// all the items stored in the map received as parameter
		headerList =  new ArrayList<Integer>(mapItemNodes.keySet());
		
		// sort the header table by decreasing order of support
		Collections.sort(headerList, new Comparator<Integer>(){
			public int compare(Integer id1, Integer id2){
				// compare the support
				int compare = mapSupport.get(id2) - mapSupport.get(id1);
				// if the same frequency, we check the lexical ordering!
				if(compare ==0){ 
					return (id1 - id2);
				}
				// otherwise we use the support
				return compare;
			}
		});
	}
}
