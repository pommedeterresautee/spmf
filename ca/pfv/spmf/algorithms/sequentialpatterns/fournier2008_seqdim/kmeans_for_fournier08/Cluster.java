package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.kmeans_for_fournier08;
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
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.ItemValued;


/**
* This class represents a Cluster for the Kmeans version used
* by the Fournier-Viger-2008 algorithm.
* 
* It is different from the general K-Means implementation in the package "clustering" in that
* it is designed to use valued item (an item with a value) rather than clustering vectors of 
* double values. It also keep the average, higher and lower values of each clusters.
* 
* @see AlgoKMeans_forFournier08
* @see AlgoKMeansWithSupport
*@see AlgoFournierViger08
* @author Philippe Fournier-Viger
*/

public class Cluster {
	// the list of items in this cluster
	private List<ItemValued> items;
	// the average value of this cluster
	private double average;
	// the highest value of this cluster
	private double higher =0;
	// the lowest value of this cluster
	private double lower = Double.MAX_VALUE;
	
	// the sum of the value in this cluster (used
	// to calculate the mean efficiently)
	private double sum = 0; 
	
	// the list of sequence IDs
	private Set<Integer> sequenceIDs = null;
	
	/**
	 * Constructor of an empty cluster with a specified average (for  K-means)
	 * @param average the average
	 */
	public Cluster(double average){
		this.items = new ArrayList<ItemValued>();
		this.average = average;
	}
	
	/**
	 * Constructor of a cluster with a list of items
	 * @param newItems  a list of items
	 */
	public Cluster(List<ItemValued> newItems){
		this.items = new ArrayList<ItemValued>(newItems);
		// calculate the average
		recomputeClusterAverage();
	}
	
	/**
	 * Constructor of a cluster by adding two list of items.
	 * @param newItems a first list of items.
	 * @param newItems2 a second list of items.
	 */
	public Cluster(List<ItemValued> newItems, List<ItemValued> newItems2){
		// add the first list
		this.items = new ArrayList<ItemValued>(newItems);
		// add the second list
		items.addAll(newItems2);
		// calculate the average
		recomputeClusterAverage();
	}
	
	/**
	 * Constructor of a cluster with a single item
	 * @param item the item
	 */
	public Cluster(ItemValued item){
		// add the item
		this.items = new ArrayList<ItemValued>();
		this.items.add(item);
		// calculate the sum
		sum+= item.getValue();
		// calculate average
		this.average = item.getValue();
	}
	
	
	/**
	 * Add items from another cluster to this cluster.
	 * @param cluster2 the other cluster.
	 */
	public void addItemsFromCluster(Cluster cluster2){
		// for each item in the other cluster
		for(ItemValued item : cluster2.getItems()){
			// add it to this cluster
			getItems().add(item);
			// update the sum
			sum+= item.getValue();
		}
	}
	
	/**
	 * Add an item to this cluster
	 * @param item the item
	 */
	public void addItem(ItemValued item) {
		// add the item
		getItems().add(item);
		// update the sum
		sum += item.getValue();
	}
	
	/**
	 * Add a list of items to this cluster
	 * @param newItems a list of items
	 */
	public void addItems(List<ItemValued> newItems) {
		// for each item
		for(ItemValued item : newItems){
			// add it
			this.getItems().add(item);
			// update the sum
			sum += item.getValue();
		}
	}

	/**
	 * Get the list of items in this cluster.
	 * @return  a list of items
	 */
	public List<ItemValued> getItems() {
		return items;
	}
	
	/**
	 * Get the number of items stored in this cluster.
	 * @return an integer.
	 */
	public int size(){
		return getItems().size();
	}

	/**
	 * Get the average of this cluster.
	 * @return the average
	 */
	public double getaverage() {
		return average;
	}
	
	/**
	 * Get a string representation of this cluster
	 * @return a string
	 */
	public String toString(){
		// for each item, print it
		StringBuffer buffer = new StringBuffer("(");
		for(ItemValued item : getItems()){
			buffer.append(item.getValue());
			buffer.append(" ");
		}
		// append the average, the minimum item and the maximum item
		// in the cluster
		buffer.append(")      <");
		buffer.append(average);
		buffer.append(", min=");
		buffer.append(getLower());
		buffer.append(" max=");
		buffer.append(getHigher());
		buffer.append(">");
		return buffer.toString();
	}
	
	/**
	 * Calculate the average of items in the cluster.
	 */
	public void recomputeClusterAverage() {
		// if no item, don't do anything
		if(getItems().isEmpty()){
			return;
		}
		
		// if one item, then it is the average..
		if(getItems().size() ==1){
			average = getItems().get(0).getValue();
			return;
		}
		// otherwise, calculate the average as the sum
		// divided by the number of items.
		average = sum /((double)items.size());

	}
	
	/**
	 *  Compute the smallest and largest values of this cluster
	 */
	public void computeHigherAndLower(){
		// for each item
		for(ItemValued item : getItems()){
			// if the largest until now, remember it
			if(item.getValue() > higher){
				higher = item.getValue();
			}
			// if the smallest until now, remember it
			if(item.getValue() < lower){
				lower = item.getValue();
			}
		}
	}

	/**
	 * Check if this cluster contains a given item.
	 * @param item2 the given item
	 * @return true if the item is contained, otherwise, false.
	 */
	public boolean containsItem(ItemValued item2) {
		// for each item
		for(ItemValued item : getItems()){
			// if it is the item, return true
			if(item == item2){
				return true;
			}
		}
		// the item was not found, so return false
		return false;
	}

	/**
	 * Get the largest item in this cluster.
	 * @return a double
	 */
	public double getHigher() {
		return higher;
	}

	/**
	 * Get the smallest item in this cluster
	 * @return a double
	 */
	public double getLower() {
		return lower;
	}

	/**
	 * Get the item ID associated to this cluster  (for use with
	 * the Fournier-Viger 08 algorithm).
	 * @return the item ID
	 */
	public int getItemId() {
		// all items store the item ID, so we just take
		// the ID from the first one
		return getItems().get(0).getId();
	}

	/**
	 * Get the set of sequence IDs corresponding to this cluster
	 * @return  a set of sequence IDs.
	 */
	public Set<Integer> getSequenceIDs() {
		return sequenceIDs;
	}

	/**
	 * Set the set of sequence IDs corresponding to this cluster
	 * param sequenceIDs  a set of sequence IDs.
	 */
	public void setSequenceIDs(Set<Integer> sequenceIDs) {
		this.sequenceIDs = sequenceIDs;
	}
}
