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
 * This is a class for testing the binary tree from code and it is intended for developers only.
 * 
 * @author Philippe Fournier-Viger
 */
class MainBinaryTree {

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		BinaryTree tree = new BinaryTree(true);
		tree.add(1);
		tree.add(2);
		tree.add(5);
		tree.add(6);
		tree.add(7);
		tree.add(9);
		tree.add(3);
		tree.add(4);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will try to add 5 another time ...");
		tree.add(5); 
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		tree.add(500);
		tree.add(501);
		tree.add(100);
		tree.add(101);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("minimum: " + tree.minimum());
		System.out.println("maximum: " + tree.maximum());
		System.out.println("... will remove 7 ");
		tree.remove(7);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 2 ");
		tree.remove(2);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 5 ");
		tree.remove(5);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 5 ");
		tree.remove(5);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will add 2 ");
		tree.add(2);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 999 ");
		tree.remove(999);
		

		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println(" lower than 5  = " + tree.lower(5));
		System.out.println(" lower than 1 = " + tree.lower(1));
		System.out.println(" lower than 10 = " + tree.lower(10));
		System.out.println(" lower than 8 = " + tree.lower(8));
		System.out.println(" lower than 200 = " + tree.lower(200));
		
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println(" higher than 5  = " + tree.higher(5));
		System.out.println(" higher than 1 = " + tree.higher(1));
		System.out.println(" higher than 10 = " + tree.higher(10));
		System.out.println(" higher than 8 = " + tree.higher(8));
		System.out.println(" higher than 200 = " + tree.higher(200));
		
		
		int [] array = {50, 60, 57, 58, 61, 62,  56, 51, 53, 54, 52,  55, 59};
		for(Integer in : array){
			tree.add(in);
		}

//		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
//		tree.removeLowerThan(6);  //  4 ,  102, 500
//		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());

//		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
//		System.out.println("... will pop maximum ...");
//		System.out.println(" maximum " + tree.popMaximum());
//		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
//		System.out.println("... will pop maximum ...");
//		System.out.println(" maximum " + tree.popMaximum());
//		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
//		System.out.println("... will pop minimum ...");
//		System.out.println(" minimum " + tree.popMinimum());
//		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
	}

}
