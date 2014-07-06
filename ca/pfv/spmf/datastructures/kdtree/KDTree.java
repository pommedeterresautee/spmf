package ca.pfv.spmf.datastructures.kdtree;
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

import java.util.Random;

import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;

/**
 * This is an implementation of a "KD tree" based on the description in the 
 * book: "Algorithms in a Nutshell" by Heineman et al. (2008).
 * <br/><br/>
 * 
 * This implementation uses the  Randomized-Select algorithm described in the 
 *  book "Introduction to algorithms" book by Cormen et al. (2001) as suggested
 *  by Heineman.
 * <br/><br/>
 * 
 * Elements that are inserted into the tree have to be arrays of double.
 *  <br/><br/>
 * 
 * The class provide methods for 
 *  - building the tree by inserting points, 
 *  - using the tree to find the nearest neighbor to a given point 
 *  - using the tree to find the k nearest neighbors to a given point 
 *  <br/><br/>
 * 
 * To find the k-nearest neighboors, the closest points are stored in a red black tree.
*
 * @author Philippe Fournier-Viger
 */
public class KDTree {

	private int nodeCount = 0; // number of nodes in the tree
	private KDNode root = null; // the tree root
	int dimensionCount = 0; // number of dimensions

	// random number generator used by the Randomized-select algorithm
	private static Random random = new Random(System.currentTimeMillis());  

	/**
	 * Default constructor
	 */
	public KDTree() {

	}

	/**
	 * Get the number of nodes in the KD-TREE
	 * @return the number of nodes
	 */
	public int size() {
		return nodeCount;
	}

	/**
	 * This method build the KDtree from a set of points.
	 * This method should be called only once.
	 * @param points an array of points, where each point is a double[]
	 */
	public void buildtree(double[][] points) {
		if (points.length == 0) {
			return;
		}
		dimensionCount = points[0].length;

		root = generateNode(0, points, 0, points.length - 1); 
	}

	/**
	 * Generate a node for the d-dimension for points (left, right).
	 * @param currentD the current dimension
	 * @param points arrays of points
	 * @param left left
	 * @param right right
	 * @return a node
	 */
	private KDNode generateNode(int currentD, double[][] points, int left, int right) {
		// if there is no point 
		if (right < left) {
			return null;
		}

		nodeCount++;
		// if there is only a single point 
		if (right == left) {
			return new KDNode(points[left], currentD);
		}
		// else if there is more than one point

		// We calculate the desired rank that correspond to the median.
		int m = (right - left) / 2;

		// we select the median point
		double[] medianNode = randomizedSelect(points, m, left, right, currentD);

		// we will use this point to separate the two lower branches of the tree.
		KDNode node = new KDNode(medianNode, currentD);
		currentD++;
		if (currentD == dimensionCount) {
			currentD = 0;
		}

		// recursively create subnodes for the two branches of the three
		node.below = generateNode(currentD, points, left, left + m -1);
		node.above = generateNode(currentD, points, left + m +1, right);
		return node;
	}

	/**
	 * Method to select the ith smallest integer of an array in average linear
	 * time. It is  based on the pseudo-code of the Randomized-Select algorithm in
	 * the book "Introduction to algorithms" by Cormen et al. (2001), with some
	 * modifications such as using a while loop instead of recursive calls.
	 * 
	 * @param a: array of integers
	 * @param i: the rank i of the desired integer.
	 * @param currentD: the dimension that is used
	 * @return the element in the array "a" that is larger than i elements.
	 */
	private double[] randomizedSelect(double[][] points, int i, int left,
			int right, int currentD) {
		int p = left;
		int r = right;

		while (true) {
			if (p == r) {
				return points[p];
			} 
			int q = randomizedPartition(points, p, r, currentD);
			int k = q - p + 1;

			if (i == k - 1) {
				return points[q];
			} else if (i < k) {
				r = q - 1;
			} else {
				i = i - k;
				p = q + 1;
			}
		}
	}

	/**
	 * Private method used by the randomized-select method
	 * (see the book for details).
	 */
	private int randomizedPartition(double[][] points, int p, int r, int currentD) {
		int i = 0;
		if (p < r) {
			i = p + random.nextInt(r - p);
		} else {
			i = r + random.nextInt(p - r);
		}
		swap(points, r, i);
		return partition(points, p, r, currentD); // call the partition method of
												// quicksort.
	}

	/**
	 * Private method used by the randomized-select method
	 * (see the book for details).
	 */
	private int partition(double[][] points, int p, int r, int currentD) {
		double[] x = points[r];
		int i = p - 1;
		for (int j = p; j <= r - 1; j++) {
			if (points[j][currentD] <= x[currentD]) {
				i = i + 1;
				swap(points, i, j);
			}
		}
		swap(points, i + 1, r);
		return i + 1;
	}

	/**
	 * swapping two points in an array.
	 * @param array the array
	 * @param i  the first point
	 * @param j  the second point
	 */
	private void swap(double[][] array, int i, int j) {
		double[] valueI = array[i];
		array[i] = array[j];
		array[j] = valueI;
	}
	
	//=====================================================================================
	//======================= To find the first nearest neighboor =========================
	//=====================================================================================
	double [] nearestNeighboor = null;  // the current nearest neighboor.
	double minDist = 0;  // the distance of the current nearest neighbor with the target point.
	
	/**
	 * Method to get the nearest neighbor
	 */
	public double[] nearest(double[] targetPoint) {
		if (root == null){
			return null;
		}
		
		// Find the node where the point would be inserted and calculate the distance
		findParent(targetPoint, root, 0);
		
		// After that, start from the root and check all rectangles that overlap the
		// distance with the parent.  If a point with a distance smaller than the parent is found,
		// then return it.
		nearest(root, targetPoint);
		
		return nearestNeighboor;
	}

	/**
	 * This method find the node where this point would be inserted in the kdd-tree.
	 * @param target :  the point
	 * @param node : the current node in the tree
	 * @param d  : the dimension used at this level of the tree.
	 * @return  :  the node where the point would be inserted.
	 */
	private void findParent(double[] target, KDNode node, int d) {		
		// IF the node would be inserted in the branch "below" this node.
		if(target[d] < node.values[d]){
			if (++d == dimensionCount) {
				d = 0;
			}
			if(node.below == null){
				nearestNeighboor = node.values;
				minDist = distance(node.values, target);
				return;
			}
			findParent(target, node.below, d);
		}
		
		//  IF the node would be inserted in the branch "above" this node.
		if(++d == dimensionCount) {
			d = 0;
		}
		
		if(node.above == null){
			nearestNeighboor = node.values;
			minDist = distance(node.values, target);
			return;
		}
		findParent(target, node.above, d);
	}

	private void nearest(KDNode node, double[] targetPoint) {
		// If shorter, update minimum
		double d = distance(node.values, targetPoint);
		if (d < minDist) {
			minDist = d;
			nearestNeighboor = node.values;
		}
	
		int dMinus1 = node.d-1;
		if(dMinus1 <0){
			dMinus1 = dimensionCount - 1;
		}
		
		// calculate perpendiculary distance with preceding dimensions.
		double perpendicularyDistance = Math.abs(node.values[node.d] - targetPoint[dMinus1]);
		if (perpendicularyDistance < minDist) { 
			// explore both side of the tree
			if (node.above != null) {
				nearest(node.above, targetPoint);
			}
			if (node.below != null) {
				nearest(node.below, targetPoint);
			}
		} else {
			// only explore one side of the three
			if (targetPoint[dMinus1] < node.values[node.d]) {
				if (node.below != null) {
					nearest(node.below, targetPoint);
				}
			} else if (node.above != null) {
				nearest(node.above, targetPoint);
			}
		}
	}

	/**
	 * Calcule the euclidian distance between two points
	 * @param node1  the first point.
	 * @param node2  the second point.
	 * @return
	 */
	private double distance(double[] node1, double[] node2) {
		double sum = 0;
		for(int i=0; i< node1.length; i++){
			sum +=  Math.pow(node1[i] - node2[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	//=====================================================================================
	//======================= Method to find the k nearest neighboor =========================
	//=====================================================================================
	
	RedBlackTree<KNNPoint> resultKNN = null; // field to store the current k nearest neighboor with the target point
	int k =0; // the parameter k.
	
	/**
	 * Method to get the k nearest neighboors
	 */
	public RedBlackTree<KNNPoint> knearest(double[] targetPoint, int k) {
		this.k = k;
		this.resultKNN = new RedBlackTree<KNNPoint>();
		
		if (root == null){
			return null;
		}
		// First traverse the tree to find the place where the node would be inserted.
		findParent_knn(targetPoint, root, 0);
		
		// Now start back at the root, and check all rectangles that have a perpendicular distance
		// smaller than the k best points found until now.
		nearest_knn(root, targetPoint);
		// return the k nearest neighbors.
		return resultKNN;
	}

	/**
	 * traverse the tree to find the place where the node would be inserted.
	 * @param target the vector
	 * @param node  the current node
	 * @param d the current dimension
	 */
	private void findParent_knn(double[] target, KDNode node, int d) {		
		// If the node would be inserted in the branch "below"
		if(target[d]  < node.values[d]){
			if (++d == dimensionCount) {
				d = 0;
			}
			if(node.below == null){
				tryToSave(node, target);
				return;
			}
			tryToSave(node.below, target);
			findParent_knn(target, node.below, d);
		}
		
		// If the node would be inserted in the branch "above".
		if(++d == dimensionCount) {
			d = 0;
		}
		
		if(node.above == null){
			tryToSave(node, target);
			return;
		}
		tryToSave(node.above, target);
		findParent_knn(target, node.above, d);
	}

	/**
	 * Method to try to save a node in the set of the current closest k neighbors. 
	 * @param node  the node to be added.
	 * @param target the target node.
	 */
	private void tryToSave(KDNode node, double[] target) {
		if(node == null){
			return;
		}
		double distance = distance(target, node.values);
		if(resultKNN.size() == k  && resultKNN.maximum().distance < distance){ 
			return;
		}
		KNNPoint point = new KNNPoint(node.values, distance);
		
		if(resultKNN.contains(point)){
			return;
		}
		
		resultKNN.add(point);
		
		if(resultKNN.size() > k){
			resultKNN.popMaximum();
		}
	}

	/**
	 * Start back at the root, and check all rectangles that have a perpendicular distance
     *  smaller than the k best points found until now.
	 * @param node  the current node
	 * @param targetPoint the vector
	 */
	private void nearest_knn(KDNode node, double[] targetPoint) {
		tryToSave(node, targetPoint); 
		
		int dMinus1 = node.d-1;
		if(dMinus1 < 0){
			dMinus1 = dimensionCount - 1;
		}
		
		double perpendicularDistance = Math.abs(node.values[node.d] - targetPoint[dMinus1]);
		if (perpendicularDistance < resultKNN.maximum().distance) { 
			// explore the "above" and "below" branches.
			if (node.above != null) {
				nearest_knn(node.above, targetPoint);
			}
			if (node.below != null) {
				nearest_knn(node.below, targetPoint);
			}
		} else {
			// explore one side of the tree.
			if (targetPoint[dMinus1] < node.values[node.d]) {
				if (node.below != null) {
					nearest_knn(node.below, targetPoint);
				}
			} else {
				if (node.above != null) {
					nearest_knn(node.above, targetPoint);
				}
			}
		}
	}

	// =======================================================================================
		
	/**
	 * Convert a vector of double to a string representation
	 * @param values  the vector
	 * @return a string
	 */
	private String toString(double [] values){
		StringBuffer buffer = new StringBuffer();
		for(Double element : values ){
			buffer.append(" " + element);
		}
		return buffer.toString();
	}
	
	/**
	 * Convert this tree to a string representation
	 */
	public String toString(){
		return toString(root, " ");
	}
	
	/**
	 * Convert a substree to a string while using some indentation.
	 * @param node the node
	 * @param indent the current indentation
	 * @return a string
	 */
	private String toString(KDNode node, String indent){
		if(node == null){
			return "";
		}
		String newIndent1 =  indent + "   |";
		String newIndent2 =  indent + "   |";
		return toString(node.values) + " (" + node.d +") \n" 
				+ indent + toString(node.above, newIndent1) + "\n" 
		        + indent + toString(node.below, newIndent2);
	}
}
