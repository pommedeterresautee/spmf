package ca.pfv.spmf.algorithms.frequentpatterns.estDec;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a estTree node. <br\>
 * <br\>
 * 
 * This implementation was made by Azadeh Soltani <br\>
 * <br\>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).<br\>
 * <br\>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.<br\>
 * <br\>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br\>
 * <br\>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see Algo_estDec
 * @see estTree
 * @author Azadeh Soltani
 */
public class estNode {
	Integer itemID; // item id
	double counter; // frequency counter
	int tid; // last tid

	List<estNode> children; // children nodes

	/**
	 * constructor
	 * @param item  the item
	 * @param count the count 
	 * @param k  the last transaction id
	 */
	estNode(Integer item, double count, int k) {
		itemID = item;
		counter = count;
		tid = k;
		children = new ArrayList<estNode>();
	}

	/**
	 * Default constructor
	 */
	estNode() {
		itemID = -1;
		counter = 0;
		tid = 0;
		children = new ArrayList<estNode>();
	}

	/**
	 * Return the immediate child of this node having a given ID. If there is no
	 * such child, return null;
	 * 
	 * @param id the id
	 * @return the node or null
	 */
	public estNode getChildWithID(int id) {
		if (children == null)
			return null;
		for (estNode child : children) {
			if (child.itemID == id) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Return the immediate index of the node having a given ID. If there is no
	 * such child, return -1;
	 * 
	 * @param id
	 */
	public int getChildIndexWithID(int id) {
		if (children == null)
			return -1;
		int i = 0;
		for (estNode child : children) {
			if (child.itemID == id) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Update the count of a node
	 * 
	 * @param k the current transaction id
	 * @param value the value to be added to the count
	 * @param d  the decay rate
	 */
	public void update(int k, int value, double d) {
		counter = counter * Math.pow(d, k - tid) + value;
		tid = k;
	}

	/**
	 * Compute the support of this node as a percentage.
	 * 
	 * @param N an integer representing a transaction count.
	 */
	public double computeSupport(double N) {
		return counter / N;
	}
}
