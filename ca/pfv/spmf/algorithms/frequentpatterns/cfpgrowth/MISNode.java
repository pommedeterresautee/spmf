package ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth;
/* This file is copyright (c) 2008-2013 Azadeh Soltani
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
import java.util.List;

/**
 * This is an implementation of a MISTree node used by the CFPGrowth algorithm.
 * <br/><br/>
 *
 * This implementation was made by Azadeh Soltani based on the FPGrowth
 * implementation by Philippe Fournier-Viger
 * 
 * @see AlgoCFPGrowth
 * @see MISTree
 * @author Azadeh Soltani
 */
public class MISNode {
	int itemID = -1;  // item represented by this node
	int counter = 1;  // frequency counter
	
	// link to parent node
	MISNode parent = null; 
	
	// links to child nodes
	List<MISNode> childs = new ArrayList<MISNode>();
	
	 // link to next node with the same item id (for the header table).
	MISNode nodeLink = null;
	
	/**
	 * constructor
	 */
	MISNode(){
		
	}

	/**
	 * Return the immmediate child of this node having a given ID.
	 * If there is no such child, return null;
	 */
	MISNode getChildWithID(int id) {
		// for each child
		for(MISNode child : childs){
			// if the id is found, return the node
			if(child.itemID == id){
				return child;
			}
		}
		return null; // if not found, return null
	}
	
	/**
	 * Return the index of the immmediate child of this node having a given ID.
	 * If there is no such child, return -1;
	 */
	int getChildIndexWithID(int id) {
		int i=0;
		// for each child
		for(MISNode child : childs){
			// if the id is found, return the index
			if(child.itemID == id){
				return i;
			}
			i++;
		}
		return -1; // if not found, return -1
	}
}
