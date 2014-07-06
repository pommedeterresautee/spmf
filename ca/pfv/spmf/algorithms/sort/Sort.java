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
 * Implementation of a few sorting algorithms (merge sort, insertion sort, etc.).
 * based on description from the book "Introduction to Algorithms" from MIT Press.
 * 
 * Please refer to that book for details about the algorithms and proofs that
 * they are correct.
 */
public class Sort {
	
	/**
	 * Implementation of Insertion sort for integers.
	 * This has an average performance of O(n log n)
	 * @param a array of integers
	 */
	public static void insertionSort(int [] a){
		for(int j=1; j< a.length; j++){
			int key = a[j];
			int i = j - 1;
			for(; i>=0 && (a[i] > key); i--){
				a[i+1] = a[i];
			}
			a[i+1] = key;
		}
	}
	
	/**
	 * Implementation of Merge sort for integers. 
	 * @param a array of integers.
	 */
	public static void mergeSort(int [] a){
	      mergeSort(a, 0, a.length-1);
	}
	
	// helper method for the merge sort (see book for details)
	private static void mergeSort(int [] a, int p, int r){
	      if (p < r)
	      {
	    	 int q = (p+r) >> 1; // divide by 2
	         mergeSort(a, p, q);      
	         mergeSort(a, q+1, r); 
	         merge(a, p, q, r);
	      }
	}
	
	// helper method for the merge sort  (see book for details)
	private static void merge(int [] a, int p, int q, int r){
		int n1 = q-p+1;
		int n2 = r-q;
		
		int [] tabL = new int[n1+1];
		int [] tabR = new int[n2+1];
		
		for(int i=0; i<n1; i++){
			tabL[i] = a[p+i];  // -1
		}
		for(int j=0; j<n2; j++){
			tabR[j] = a[q+j+1];
		}
		tabL[n1]= Integer.MAX_VALUE;
		tabR[n2]= Integer.MAX_VALUE;
		
		int i =0;
		int j =0;
		for(int k=p; k<r+1;k++){
			if(tabL[i] <= tabR[j]){
				a[k] = tabL[i++];
			}else{
				a[k] = tabR[j++];
			}
		}
	}
	
	/**
	 * Implementation of Bubble sort for integers
	 * @param a array of integers
	 */
	// (see book for details)
	public static void bubbleSort(int [] a){
		for(int i=0; i < a.length; i++){
			for(int j= a.length -1; j>= i+1; j--){
				if(a[j] < a[j-1]){
					int temp = a[j];
					a[j] = a[j-1];
					a[j-1] = temp;
				}
			}
		}
	}
	
	/**
	 * Implementation of quick sort
	 * @param a array of integers
	 */
	// (see book for details)
	public static void quicksort( int[] a) {
		quicksort(a, 0, a.length-1);
	}
	
	// helper method for the quick sort  (see book for details)
	private static void quicksort( int[] a, int p, int r) {
		if(p < r){
			int q =  partition(a, p, r);
			quicksort(a, p, q-1);
			quicksort(a, q+1, r);
		}
  	}
	// helper method for the quick sort  (see book for details)
	 static int partition(int[] a, int p, int r) {
		int x = a[r];
		int i = p - 1;
		for(int j = p; j <= r-1; j++){
			if(a[j] <= x){
				i = i+1;
				swap(a, i, j);
			}
		}
		swap(a, i+1, r);
		return i+1;
	}

	/**
	 * Method to swap two elements in an array
	 * @param array the array
	 * @param i position of an element
	 * @param j position of another element
	 */
	private static void swap(int[] array, int i, int j) {
        int valueI = array[i];
        array[i] = array[j];
        array[j] = valueI;
    }
}
