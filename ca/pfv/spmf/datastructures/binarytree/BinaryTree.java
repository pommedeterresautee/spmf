package ca.pfv.spmf.datastructures.binarytree;
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

/**
 * This is an implementation of a "binary tree" based on the chapter 12 of the
 * book: "Introductions to algorithms" by Cormen et al. (2001).
 * Most of the code is based on the pseudo-code from this book.
 * <br/><br/>
 * 
 * Elements inserted in the tree have to extend the Comparable class.
 * <br/><br/>
 * 
 * The tree provides these operations: add, remove, size, contains, minimum,
 * maximum, popMinimum, popMaximum, higher, lower.
 *
 * @author Philippe Fournier-Viger
 */
public class BinaryTree<T extends Comparable<T>> {

	// number of elements currently in the tree
	private int size = 0; 
	// the tree root
	private Node root = null; 
	
	// allow the same element to appear multiple times in the tree
	// or not.
	boolean allowSameElementMultipleTimes = true;

	/**
	 * Constructor
	 * 
	 * @param allowSameElementMultipleTimes
	 *            if set to true, this allows the tree to contains the same
	 *            element multiple times. To check if an element is the same,
	 *            this class use the compareTo method
	 */
	public BinaryTree(boolean allowSameElementMultipleTimes) {
		this.allowSameElementMultipleTimes = allowSameElementMultipleTimes;
	}

	/**
	 * Default constructor
	 */
	public BinaryTree() {
		
	}

	/**
	 * Return the number of elements stored in the tree
	 * @return an integer
	 */
	public int size() {
		return size;
	}

	/**
	 * Add an element to the tree.
	 * @param element the element to be added
	 */
	public void add(T element) {
		// create a node for that element
		Node z = new Node();
		z.key = element;

		Node y = null;
		Node x = root; // the root

		// loop while x is not null
		while (x != null) {
			// set y to x
			y = x;
			// compare the key of the element to be inserted with x
			int compare = z.key.compareTo(x.key);
			// if smaller we go in the left subtree
			if (compare < 0) {
				x = x.left;
			} else {
				// otherwise, it it is the same and we don,t allow
				// multiple instances of the same item
				if (compare == 0 && !allowSameElementMultipleTimes) {
					return; // we don't add it
				}
				// otherwise go the right subtree
				x = x.right;
			}
		}
		// after the previous loop has terminated, we have found
		// the place where the new node z should be inserted (as a child of y) so
		// we will insert it there.
		
		// we set the parent of z as y
		z.parent = y;
		if (y == null) { // case of an empty tree
			root = z;  // set z as the root
		}// if z is small than y 
		else if (z.key.compareTo(y.key) < 0) {
			// append as left child
			y.left = z;
		} else {
			// otherwise append as right child
			y.right = z;
		}
		// increase the number of elements in the tree
		size++;
	}
	
	/**
	 * Check if the tree is empty.
	 * @return true if empty
	 */
	public boolean isEmpty(){
		return root == null;
	}

	/**
	 * Remove an element from the tree
	 * 
	 * @param element
	 *            the element to be removed
	 */
	public void remove(T element) {
		// First find the node containing the element.
		Node z = search(root, element);
		if (z == null) { // if the element is not in the tree
			return;
		}
		// if found, delete it
		performDelete(z);
	}

	/**
	 * This method delete a given node from the tree
	 * @param z a node.
	 */
	private void performDelete(Node z) {
		// create a node pointer y
		Node y;
		// if z has no left or right subtree
		if (z.left == null || z.right == null) {
			// set y to z
			y = z;
		} else {
			// set y as the successor of z
			y = successor(z);
		}

		// create a node pointer x
		Node x;
		// if y has a left subtree
		if (y.left != null) {
			// set x as that subtree
			x = y.left;
		} else {
			//otherwise set x as the right subtree of y
			x = y.right;
		}
		// if x is not null
		if (x != null) {
			// set the parent of x as the parent of y.
			x.parent = y.parent;
		}
		// if the parent of y is null
		if (y.parent == null) {
			// set x as the root
			root = x;
		} else if (y.equals(y.parent.left)) {
			// otherwise if y is the parent of the left subtree of y
			// set it to x
			y.parent.left = x;
		} else {
			// otherwise set the right subtree of the parent of y to x
			y.parent.right = x;
		}

		// if y is not z
		if (y != z) {
			// set the element of z to y
			z.key = y.key;
		}
		// decrease the size by 1
		size--;
	}

	/**
	 * Get the smallest value greater than the one stored in a node x.
	 * @param x a node
	 * @return the node containing the smallest value greater than the one in x.
	 */
	private Node successor(Node x) {
		// if there is a right subtree
		if (x.right != null) {
			// return the minimum of the right subtree
			return minimum(x.right);
		}
		// otherwise, go to the parent
		Node y = x.parent;
		//  while y is not the root and x is not equal to the right subtree of y
		while (y != null && x.equals(y.right)) {
			// set x to y
			x = y;
			// explore the parent of y
			y = y.parent;
		}
		// return y
		return y;
	}

	/**
	 * Get the largest value smaller than the one stored in node X.
	 * @param x the node X.
	 * @return the largest value smaller than the one stored in node X.
	 */
	private Node predecessor(Node x) {
		// if there is a left subtree
		if (x.left != null) {
			// return the maximum of the right subtree
			return maximum(x.left);
		}
		// otherwise, go to the parent
		Node y = x.parent;
	//  while y is not the root and x is not equal to the left subtree of y
		while (y != null && x.equals(y.left)) {
			// set x to y
			x = y;
			// explore the parent of y
			y = y.parent;
		}
		// return y
		return y;
	}

	/**
	 * Get the minimum element in the tree and remove it from the tree
	 * 
	 * @return the minimum element in the tree
	 */
	public T popMinimum() {
		// if the tree is empty, return null
		if (root == null) {
			return null;
		}
		// From the root, go to the left until a leaf is reached
		Node x = root;
		while (x.left != null) {
			x = x.left;
		}
		// get the value of the leaf
		T value = x.key;
		// delete the node
		performDelete(x);
		// return the value
		return value;
	}

	/**
	 * Return the largest element having a value lower than a given element k.
	 */
	public T lower(T k) {
		// call the method lowerNode who do the main job
		Node result = lowerNode(k);
		// if no result, return null
		if (result == null) {
			return null;
		} else {
			// otherwise return the value contained in the node found
			return result.key;
		}
	}

	/**
	 * Return the node having the largest element having a value lower than a
	 * given element k.
	 */
	private Node lowerNode(T k) {
		Node x = root;
		while (x != null) {
			if (k.compareTo(x.key) > 0) {
				if (x.right != null) {
					x = x.right;
				} else {
					return x;
				}
			} else {
				if (x.left != null) {
					x = x.left;
				} else {
					Node current = x;
					while (current.parent != null
							&& current.parent.left == current) {
						current = current.parent;
					}
					return current.parent;
				}
			}
		}
		return null;
	}

	/**
	 * Return the largest element having a value lower than a given element k.
	 */
	public T higher(T k) {
		// call the method higherNode to locate the node meeting the criterion
		Node result = higherNode(k);
		// if no result, return null
		if (result == null) {
			return null;
		} else {
			// otherwise return the value contained in the node found
			return result.key;
		}
	}

	/**
	 * Return the node having the largest element having a value higher than a
	 * given element k.
	 */
	private Node higherNode(T k) {
		Node x = root;
		while (x != null) {
			if (k.compareTo(x.key) < 0) {
				if (x.left != null) {
					x = x.left;
				} else {
					return x;
				}
			} else {
				if (x.right != null) {
					x = x.right;
				} else {
					Node current = x;
					while (current.parent != null
							&& current.parent.right == current) {
						current = current.parent;
					}
					return current.parent;
				}
			}
		}
		return null;
	}

	/**
	 * Get the minimum element in the tree
	 * 
	 * @return the minimum element in the tree
	 */
	public T minimum() {
		// go down the left links until reaching a leaf
		if (root == null) {
			return null;
		}
		// return the leaf value
		return minimum(root).key;
	}

	/**
	 * Return node with the smallest value in the tree
	 * @param x the node where the search should start
	 * @return the node meeting the criterion
	 */
	private Node minimum(Node x) {
		// go down the left links until reaching a leaf
		while (x.left != null) {
			x = x.left;
		}
		// return the leaf
		return x;
	}

	/**
	 * Get the maximum element in the tree and remove it from the tree
	 * 
	 * @return the maximum element in the tree
	 */
	public T popMaximum() {
		// if the tree is empty, return null
		if (root == null) {
			return null;
		}
		// start from the root and go down the right subtrees until
		// a leaf is reached
		Node x = root;
		while (x.right != null) {
			x = x.right;
		}
		// get the value of the leaf
		T value = x.key;
		// then delete the leaf and return the value
		performDelete(x);
		return value;
	}

	/**
	 * Get the maximum element in the tree
	 * 
	 * @return the maximum element in the tree
	 */
	public T maximum() {
		// if the tree is empty return nothing
		if (root == null) {
			return null;
		}
		// otherwise go down the right links until reaching a leaf and return the key
		return maximum(root).key;
	}

	/**
	 * Get the maximum element in the tree
	 * @return the node containing the maximum element in the tree
	 */
	private Node maximum(Node x) {
		// go down the right links until reaching a leaf
		while (x.right != null) {
			x = x.right;
		}
		// return the node
		return x;
	}

	/**
	 * Check if an element is contained in the tree
	 * 
	 * @param k
	 *            the element.
	 * @return true if the element is in the tree. Otherwise, false.
	 */
	public boolean contains(T k) {
		// call the search function and if not null it means
		// that the element is in the tree
		return search(root, k) != null;
	}

	/**
	 * Method that search for an element and return the node that contains this
	 * element.
	 * 
	 * @param x     The node where the search will start.
	 * @param k     The element to search
	 * @return The node containing the element or null if the element is not in
	 *         the tree.
	 */
	private Node search(Node x, T k) {
		// while a leaf is not reach and the key has not been found
		while (x != null && !k.equals(x.key)) {
			// compare the key with the current node
			// if smaller, go left
			if (k.compareTo(x.key) < 0) {
				x = x.left;
			} else {
				// otherwise go right
				x = x.right;
			}
		}
		// return the node or null if nothing is found
		return x;
	}

	/**
	 * Method toString that returns a string with all the elements in the tree
	 * according to the ascending order. NOTE : could be transformed into a non
	 * recursive algorithm.
	 */
	public String toString() {
		// if the tree is empty then return an empty string
		if (root == null) {
			return "";
		}
		// call the recursive helper method to print the tree
		return print(root, new StringBuffer()).toString();
	}

	/**
	 * Print a subtree to a stringbuffer.
	 * @param x the root of the subtree
	 * @param buffer the stringbuffer  
	 * @return the stringbuffer
	 */
	private StringBuffer print(Node x, StringBuffer buffer) {
		// if the subtree is not empty and there is a key stored in that node
		if (x != null && x.key != null) {
			// recursive call
			print(x.left, buffer);
			// append the x value 
			buffer.append(x.key + " ");
			// recursive call
			print(x.right, buffer);
		}
		return buffer;
	}

	/**
	 * Internal class that represents a node of the binary tree
	 * @author Philippe Fournier-Viger
	 */
	public class Node {
		T key = null;  // the value stored in this node
		Node left = null; // pointer to left child
		Node right = null; // pointer to right child
		Node parent = null; // pointer to parent

		/**
		 * Get a string representatin of this node.
		 * @return a string
		 */
		public String toString() {
			// create a string buffer
			StringBuffer buffer = new StringBuffer();
			// append the key stored in this node
			buffer.append(key.toString());
			// if there is a left subtree, then append the key of the left child
			if (left != null) {
				buffer.append(" L= " + left.key);
			}
			// if there is a right subtree, then append the key of the right child
			if (right != null) {
				buffer.append(" R= " + right.key);
			}
			return buffer.toString();
		}
	}

}
