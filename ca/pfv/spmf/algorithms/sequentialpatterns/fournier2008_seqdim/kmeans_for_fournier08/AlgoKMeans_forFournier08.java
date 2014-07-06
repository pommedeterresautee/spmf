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
import java.util.Random;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.ItemValued;

/**
 * This is the implementation of the K-means algorithm used by the Fournier-Viger-2008 algorithm 
 * for sequential pattern mining. 
 * <br/><br/>
 * This implementation should only be used for the Fournier-Viger 2008 as it is specially designed
 * for it (e.g. it clusters valued items  and it keep the
 * min max and avg values of each clusters). For other purposes, one should use the general KMeans implementation
 * in the package "clustering". This latter implementation is more general (uses vector of doubles instead
 * of items) and is more optimized.
 * <br/><br/>
 *
 * @see AlgoFournierViger08
 * @see ItemValued
* @author Philippe Fournier-Viger
 */

public class AlgoKMeans_forFournier08 {

	// the parameter k indicates the number of clusters to be found
	private int k;
	// a random number generator because k-means is a randomized algorithm
	private final static Random random = new Random(System.currentTimeMillis());

	/**
	 * Contructor
	 * @param k  the parameter k of K-Means, which represents the desired 
	 * number of clusters.
	 */
	public AlgoKMeans_forFournier08(int k) {
		this.k = k;
	}

	/**
	 * Run the algorithm
	 * @param input   a list of items to be clustered
	 * number of clusters.
	 * @return a list of clusters
	 */
	public List<Cluster> runAlgorithm(List<ItemValued> input) {
		// Create a list of clusters
		List<Cluster> clusters = new ArrayList<Cluster>(k);

		// If onely 1 item
		if (input.size() == 1) {
			// create a cluster with that item
			ItemValued item = input.get(0);
			Cluster cluster = new Cluster(item);
			cluster.addItem(item);
			clusters.add(cluster);
			// return that cluster
			return clusters;
		}

		// (1) Randomly generate k empty clusters with a random average (cluster
		// center)

		// (1.1) Find the smallest value and largest value
		double higher = input.get(0).getId();
		double lower = input.get(0).getId();
		// for each item
		for (ItemValued item : input) {
			// if the largest item until now, remember it
			if (item.getValue() > higher) {
				higher = item.getValue();
			}
			// if the smallest item until now, remember it
			if (item.getValue() < lower) {
				lower = item.getValue();
			}
		}

		// If all items have the same values,  we return only one
		// cluster.
		if (higher == lower) {
			// Create a cluster with all items and return it
			Cluster cluster = new Cluster(input);
			clusters.add(cluster);
			return clusters;
		}

		// (1.2) Generate the k empty clusters with a random average
		// between the smallest and largest values.
		for (int i = 0; i < k; i++) {
			// generate random average
			double average = random.nextInt((int) (higher - lower)) + lower;
			// create the cluster
			Cluster cluster = new Cluster(average);
			clusters.add(cluster);
		}

		// (2) Repeat the two next steps until the assignment hasn't changed
		boolean changed;

		do {
			changed = false;
			// (2.1) Assign each point to the nearest cluster center.

			// / for each item
			for (ItemValued item : input) {
				// find the nearest cluster and the cluster containing the item
				Cluster nearestCluster = null;
				Cluster containingCluster = null;
				double distanceToNearestCluster = Double.MAX_VALUE;

				// for each cluster
				for (Cluster cluster : clusters) {
					// calculate the distance to the current item
					double distance = averageDistance(cluster, item);
					// if the smallest distance until now, remember
					// that cluster
					if (distance < distanceToNearestCluster) {
						nearestCluster = cluster;
						distanceToNearestCluster = distance;
					}
					// if the cluster contains that item,
					// then note that this is the cluster
					// containing the item.
					if (cluster.containsItem(item)) {
						containingCluster = cluster;
					}
				}

				// if the closest cluster to the current item
				// is not the cluster containing the item
				if (containingCluster != nearestCluster) {
					// if the item is in a cluster
					if (containingCluster != null) {
						// remove item from the cluster
						removeItem(containingCluster.getItems(), item);
					}
					// add the item to the nearest cluster
					nearestCluster.addItem(item);
					changed = true;
				}
			}

			// (2.2) For each cluster, recompute the new cluster average
			for (Cluster cluster : clusters) {
				cluster.recomputeClusterAverage();
			}

		} while (changed);

		// Computer min and max for all clusters
		for (Cluster cluster : clusters) {
			cluster.computeHigherAndLower();
		}

		// return the set of clusters
		return clusters;
	}

	/**
	 * Remove an item from a list of items
	 * @param items  a list of items
	 * @param item  the item to be removed
	 */
	private void removeItem(List<ItemValued> items, ItemValued item) {
		// for each item in the list
		for (int i = 0; i < items.size(); i++) {
			// if the item to be removed is found
			if (items.get(i) == item) {
				// then, remove it
				items.remove(i);
			}
		}
	}

	/**
	 * Calculate the distance between the average of a cluster and
	 * a given item
	 * @param cluster1 the cluster
	 * @param item  the item
	 * @return  the distance as a double
	 */
	private double averageDistance(Cluster cluster1, ItemValued item) {
		return Math.abs(cluster1.getaverage() - item.getValue());
	}

	/**
	 * Set the parameter k for the k-means algorithm.
	 * @param k  an integer.
	 */
	public void setK(int k) {
		this.k = k;
	}

}
