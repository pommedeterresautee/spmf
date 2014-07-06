package ca.pfv.spmf.algorithms.frequentpatterns.itemsettree;

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
 
/**
 * This class represents a hash table for storing itemsets.
 * 
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */
public class HashTableIT {
	
	// internal array of the hash table
	public List<Itemset>[] table;
	
	/**
	 * Constructor
	 * @param size the size of the internal array
	 */
	public HashTableIT(int size){
		table = new ArrayList[size];
	}
	

	/**
	 * Add an itemset to the hash table.
	 * @param items an itemset represented as a int array.
	 * @param support the support of the itemset
	 */
	public void put(int[] items, int support) {
		// calculate the hashcode of the itemset to know
		// where to insert it
		int hashcode = hashCode(items);
		// if there is no list at that position
		if(table[hashcode] ==  null){
			// create a new list for storing this itemset and future colisions
			table[hashcode] = new ArrayList<Itemset>();
			// create an itemset object
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			//add it to the list
			table[hashcode].add(itemset);
		}else{
			// Otherwise, it means that there is a collision.
			// We will first check if the itemset is already there.
			
			// For each itemset already at that position in the hash table
			for(Itemset existingItemset : table[hashcode]){
				// if the itemset is the one that we want to add
				if(same(items, existingItemset.itemset)){
					//update its support count and then stop
					existingItemset.support += support; 
					return;
				}
			}
			// otherwise, it is not there already, so
			// create a new itemset
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			// and add it to the collision list for that position
			table[hashcode].add(itemset);
		}
	}

	/**
	 * Calculate the hashcode of an itemset.
	 * @param items an itemset
	 * @return the hashcode as a int
	 */
	public int hashCode(int[] items){
		// the hashcode is the sum of each item i multiplied by i * 10, modulo
		// the size of the internal array of the hash table.
		int hashcode =0;
		for (int i=0; i< items.length; i++) {
			hashcode += (items[i] + (i*10));
	    }
		// to fix the bug of overflowing the size of an integer 
		if(hashcode < 0){
			hashcode = 0 - hashcode;
		}
		return (hashcode % items.length);
	}
	
	/**
	 * This method checks if two itemsets are the same
	 * @param itemset1 a first itemset
	 * @param itemset2 a second itemset
	 * @return true if they are the same, otherwise false
	 */
	private boolean same(int[] itemset1, int[] itemset2) {
		// if one of them is null, return false
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		// if the size is not the same return false
		if(itemset1.length != itemset2.length){
			return false;
		}
		// loop over items and if one is not the same, return false
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		// they are the same, return true
		return true;
	}

}
