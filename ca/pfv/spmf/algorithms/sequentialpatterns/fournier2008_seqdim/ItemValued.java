package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim;
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

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.kmeans_for_fournier08.Cluster;

/**
 * This class represents a valued item (an item with a double value) as used by the Fournier-Viger et al. (2008) algorithm.
 * This class extend the Item class in the same package.
 * 
 * @see ItemSimple
 * @see ItemValued
 * @see AlgoFournierViger08
 */
public class ItemValued extends ItemSimple{
	/** the value associated to this Item*/
	private double value;
	/** Used by Fournier08 algorithm to indicate from which sequence this
	/** item come from. It contains a sequence ID.*/
	private int sequenceID =-1; 
	
	/**Variable to indicate a minimum and maximum value.
	It is used by the Fournier08 algorithm to indicate the minimum 
	 and max value of an item obtained by clustering*/
	private double min;
	private double max;

	/** Used by the Fournier08 algorithm to indicate  the cluster that contains*/
	/** this item.*/
	private Cluster cluster = null; 
	
	/**
	 * Constructor
	 * @param id an item ID.
	 */
	public ItemValued(int id){
		this(id, 0);
	}
	
	/**
	 * Constructor
	 * @param id  an item ID.
	 * @param value a value to be associated with this item
	 */
	public ItemValued(int id, double value){
		super(id);
		this.value = value;
		this.min = value;
		this.max = value;
	}
	
	/**
	 * Constructor
	 * @param id  an item ID.
	 * @param value a value to be associated with this item
	 * @param min  a minimum value for this item.
	 * @param max  a maximum value for this item.
	 */
	public ItemValued(int id, double value, double min, double max){
		super(id);
		this.value = value;
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Constructor
	 * @param id  an item ID.
	 * @param value a value to be associated with this item
	 * @param sequenceID an ID of a sequence containing this item
	 */
	public ItemValued(int id, double value, int sequenceID){
		super(id);
		this.value = value;
		min = value;
		max = value;
		this.sequenceID = sequenceID;
	}

	/**
	 * Get the value associated with this item
	 * @return a double value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Get a String representation of this item
	 * @return a String
	 */
	public String toString(){
		// create a string buffer
		StringBuffer temp = new StringBuffer();
		// append item id
		temp.append(getId());
		// append the value
		temp.append(" (");
		temp.append(getValue());
		// append min and max if there is one
		if(min !=0 && max !=0){
			temp.append(", min=");
			temp.append(getMin());
			temp.append(" max=" );
			temp.append(getMax());
		}
		temp.append(')');
		// append the cluster if there is a cluster associated to this item
		if(getCluster() != null){
			temp.append('[');
			temp.append(getCluster().getaverage());
			temp.append(']');
		}
		// return the String
		return temp.toString();
	}
	
//	public int hashCode()
//	{
//		String string = getId() + " " + getValeur(); !
//		return string.hashCode();
//	}

	
	/**
	 * Get the sequence ID associated to this item.
	 * @return an interger value.
	 */
	public int getSequenceID() {
		return sequenceID;
	}

	/**
	 * Set the sequence ID associated to this item.
	 * param sequenceID an interger value.
	 */
	public void setSequenceID(int sequenceID) {
		this.sequenceID = sequenceID;
	}
	
	/**
	 * Set the value for this item
	 * @param value a double value
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
/**
 * Get the cluster associated to this item.
 * @return a Cluster
 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * Set the cluster associated to this item.
	 * param cluster a Cluster
	 */
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * Get the minimum value associated to this item.
	 * @return a double value
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Get the maximum value associated to this item.
	 * @return a double value
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Set the minimum value associated to this item.
	 * @param min a double value
	 */
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * Set the maximum value associated to this item.
	 * @param max a double value
	 */
	public void setMax(double max) {
		this.max = max;
	}
	
}
