package ca.pfv.spmf.algorithms.classifiers.decisiontree.id3;


/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
* This class represents a decision tree created by the ID3 algorithm.
 *
 * @see AlgoID3
 * @see Node
 * @see DecisionNode
 * @see ClassNode
 * @author Philippe Fournier-Viger
 */
public class DecisionTree {
	// the list of attributes that was used to create the decision tree
	String []allAttributes;
	
	// a reference to the root node of the tree
	Node root = null;

	/**
	 * Print the tree to System.out.
	 */
	public void print() {
		System.out.println("DECISION TREE");
		String indent = " ";
		print(root, indent, "");
	}

	/**
	 * Print a sub-tree to System.out
	 * @param nodeToPrint the root note
	 * @param indent the current indentation
	 * @param value  a string that should be used to increase the indentation
	 */
	private void print(Node nodeToPrint, String indent, String value) {
		String newIndent = indent + "  ";
		// if it is a class node
		if(nodeToPrint instanceof ClassNode){
			// cast to a class node and print it
			ClassNode node = (ClassNode) nodeToPrint;
			System.out.println(indent + value + "="+ node.className);
		}else{
			// if it is a decision node, cast it to a decision node
			// and print it.
			DecisionNode node = (DecisionNode) nodeToPrint;
			System.out.println(indent + allAttributes[node.attribute] + "->");
			// then recursively call the method for subtrees
			for(int i=0; i< node.nodes.length; i++){
				print(node.nodes[i], newIndent, node.attributeValues[i]);
			}
		}
		
	}
	/**
	 * This method predict the class of an instance.
	 * @param newInstance  an instance for which to perform the prediction.
	 * @return Return the class name or null if the tree cannot predict the class, for example because
	 *  some value does not appear in the tree.
	 */
	public String predictTargetAttributeValue(String[] newInstance) {
		return predict(root, newInstance);
	}


	/**
	 * Helper method to perform a prediction.
	 * @param currentNode the current node from the decision tree that is considered
	 * @param newInstance an instance for which to perform the prediction.
	 * @return Return the class name or null if the tree cannot predict the class, for example because
	 *  some value does not appear in the tree.
	 */
	private String predict(Node currentNode, String[] newInstance) {
		// If this node is a class node, then return the class name
		if(currentNode instanceof ClassNode){
			ClassNode node = (ClassNode) currentNode;
			return node.className;
		}else{
			// otherwise, check which subtree we should follow
			// by comparing the attribute of the instance
			// with the one in the tree
			DecisionNode node = (DecisionNode) currentNode;
			String value = newInstance[node.attribute];
			for(int i=0; i< node.attributeValues.length; i++){
				if(node.attributeValues[i].equals(value)){
					return predict(node.nodes[i], newInstance);
				}
			}
		}
		return null; // null if no subtree correspond to the attribute value for the instance
	}
	
	

}
