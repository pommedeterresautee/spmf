package ca.pfv.spmf.algorithms.sort;
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
 * Class for testing and debugging the sorting algorithms (for developers only).
 * @author Philippe Fournier-Viger
 *
 */
class MainTestSort {
	public static void main(String[] args) {
		int []arrayInt = new int[]{5,2,6,7,9, 4,2, 1};
//		Sort.mergeSort(arrayInt);
		Sort.mergeSort(arrayInt);
		
//		QuickSelect.quicksort(arrayInt);
		
		System.out.println(arrayToString(arrayInt));
//		insertionSort(arrayInt);
		System.out.println(arrayToString(arrayInt));
	}

	 
	 public static String arrayToString(int[] a) {
		    StringBuffer result = new StringBuffer();
		    if (a.length > 0) {
		        result.append(a[0]);
		        for (int i=1; i<a.length; i++) {
		            result.append(" ");
		            result.append(a[i]);
		        }
		    }
		    return result.toString();
		}
}
