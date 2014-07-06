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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.ItemValued;

/**
 * This algorithm is a modified K-Means algorithm to be used with the Fournier-Viger-2008 algorithm, which.
 * adds the constraint of a minimum number of items per clusters (minimum support). 
 * <br/><br/> 
 * This implementation should only be used for the Fournier-Viger 2008 as it is specially designed
 * for it (e.g. it clusters valued items  and it keep the
 * min max and avg values of each clusters). For other purposes, one should use the general KMeans implementation
 * in the package "clustering". This latter implementation is more general (uses vector of doubles instead
 * of items) and is more optimized.
 * <br/><br/> 
 * This algorithm works as follows  <br/>
 * We  specify a maximum K.<br/>
 * The algorithmm executes K-Means from K=1 to K=Kmax and try to find
 * the largest number of clusters such that each cluster has a size that is larger
 * than the minimum support (as an integer).<br/>
 * The algorithm returns this set of clusters.<br/>
 * The algorithm stops at k=k+1 or when the number of clusters does not increase for
 * two successives K.
 *
 * @see AlgoFournierViger08
 * @see ItemValued
 * @author Philippe Fournier-Viger
 */

public class AlgoKMeansWithSupport{
	
	// the maximum number of clusters to be found
	// 
	private int maxK;
	// the minimum support threshold as an integer value. It indicates
	// the minimum size that a cluster should have.
	private int minsuppRelative;
	// the number of times that K-Means should be executed for each value 
	// of k
	private final int numberOfTriesForEachK;
	// an implementation of the regular k-means.
	private final AlgoKMeans_forFournier08 algoKMeans;

	/**
	 * Constructor
	 * @param maxK  the maximum number of cluster to be found
	 * @param relativeMinsup  a relative minimum support threshold
	 * @param algoKMeans an implementation of the regular K-Means
	 * @param numberOfTriesForEachK the number of times that K-Means should
	 *    be executed for each value of k.
	 */
	public AlgoKMeansWithSupport(int maxK, int relativeMinsup, AlgoKMeans_forFournier08 algoKMeans, int numberOfTriesForEachK){
		// save the parameters
		this.maxK = maxK;
		this.minsuppRelative = relativeMinsup;
		this.algoKMeans = algoKMeans;
		this.numberOfTriesForEachK = numberOfTriesForEachK;
		
		// if the minimum support is 0, we set it to 1
		// so that no empty cluster is found.
		if(minsuppRelative <= 0){
			minsuppRelative = 1;
		}
	}
	
	/**
	 * Constructor
	 * @param maxK  the maximum number of cluster to be found
	 * @param minsup  minimum support threshold as a percentage (double)
	 * @param algoKMeans an implementation of the regular K-Means
	 * @param numberOfTriesForEachK the number of times that K-Means should
	 *    be executed for each value of k.
	 */
	public AlgoKMeansWithSupport(int maxK, double minsup, int transactioncount,  AlgoKMeans_forFournier08 algoKMeans, int numberOfTriesForEachK){
		this.maxK = maxK;
		// convert to a relative minimum support by multiplying
		// by the database size.
		this.minsuppRelative = (int) Math.ceil(minsup * transactioncount);
		this.algoKMeans = algoKMeans;
		this.numberOfTriesForEachK = numberOfTriesForEachK;
		
		// if the minimum support is 0, we set it to 1
		// so that no empty cluster is found.
		if(minsuppRelative <= 0){
			minsuppRelative = 1;
		}
	}
	
	/**
	 * Run the algorithm
	 * @param items  the values to be clustered
	 * @return a list of clusters found.
	 */
	public List<Cluster> runAlgorithm(List<ItemValued> items){
		// if the maximum number of clusters is larger than
		// the number of items, then set it to the number of items.
		if(maxK > items.size()){ 
			maxK = items.size();
		}
		
		// The number of clusters that will be found
		int nbClustersFound = -1; 
		// The list of clusters that will be found
		List<Cluster> clustersFound = null;
		
		// For each K.
		for(int k=1; k <= maxK; k++){
			// we try numberOfTriesForEachK times.
			for(int j=0; j<numberOfTriesForEachK; j++){
				
				// We execute K-Means with k
				algoKMeans.setK(k);
				// K-means return a set of clusters
				List<Cluster> clusters = algoKMeans.runAlgorithm(items);
				
				// We count the numbers of clusters with size >= minsupp
				// and we remove clusters with size < minsupp
				int frequentClustersCount = 0;
				// for each cluster
				for(int i=0; i< clusters.size();){
					// if the cluster has a size >= minsup
					if(isAFrequentCluster(clusters.get(i))){
						// increase the count of frequent clusters
						frequentClustersCount++;
						i++;  // go to next cluster
					}else{
						// if size < minsup, we delete the cluster
						clusters.remove(i);  
					}
				}
				// If the number of clusters found is higher than
				// the number of clusters found by other execution
				// of k-means, we keep the clusters from this execution.
				if(frequentClustersCount > nbClustersFound){
					nbClustersFound = frequentClustersCount; 
					clustersFound = clusters;
				}
			}
		}
		
		// We associate the items to their respective clusters because we called
		// K-Means many times with different K and it is possible
		// that items are not associated to the last set of clusters that was found.
		for(ItemValued item : items){
			// for each cluster
			for(Cluster cluster : clustersFound){
				// if the current item is contained in this 
				// cluster
				for(ItemValued item2 : cluster.getItems()){
					if(item == item2){
						// we re-associate the item to the cluster.
						item.setCluster(cluster);
					}
				}
			}
		}
	
		// We return the list of clusters found.
		return clustersFound;
	}

	/**
	 * Check if the support of a cluster is higher than minsupp.
	 * To do this, we should not count two times the items that have 
	 * the same SequenceID.
	 * @param cluster
	 * @return
	 */
	private boolean isAFrequentCluster(Cluster cluster) {
		// Create a set to store the sequence IDs where
		// each item appears
		Set<Integer> sequenceIds = new HashSet<Integer>();
		// for ea item
		for(ItemValued item : cluster.getItems()){
			// store the sequence IDs in the set
			sequenceIds.add(item.getSequenceID());
		}
		// if the set of sequence IDs is >= minsup
		return sequenceIds.size() >= minsuppRelative;
	}
}
