package ca.pfv.spmf.algorithms.clustering.hierarchical_clustering;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of generic Hierarchical Clustering Algorithm as described
 * in this webpage:
 * http://home.dei.polimi.it/matteucc/Clustering/tutorial_html/hierarchical.html
 * <br/><br/>
 * 
 * This is a Hierarchical Clustering with a constant "threshold" that indicate
 * the maximal distance between two clusters to group them. The algorithm stops
 * when no cluster can be merged.
 * <br/><br/>
 * 
 * The distance between two clusters is calculated as the distance between the
 * medians of the two clusters.
 * 
 * @author Philippe Fournier-Viger
 */

public class AlgoHierarchicalClustering {
	
	// parameter
	private double maxDistance =0;  // maximum distance allowed for merging two clusters
	
	// list of clusters
	List<Cluster> clusters = null;
	
	// for statistics
	private long startTimestamp;  // start time of latest execution
	private long endTimestamp;    // end time of latest execution
	private long iterationCount; // number of iterations performed

	/**
	 * Default constructor
	 */
	public AlgoHierarchicalClustering() {
	}

	/**
	 * Run the algorithm.
	 * @param inputFile an input file containing vectors of doubles
	 * @param maxDistance  the maximum distance allowed for merging two clusters
	 * @return a list of Clusters
	 * @throws IOException exception if error while reading the file
	 */
	public List<Cluster> runAlgorithm(String inputFile, double maxDistance) throws NumberFormatException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// save the parameter
		this.maxDistance = maxDistance;
		
		// create an empty list of clusters
		clusters = new ArrayList<Cluster>();
		
		// Read the vectors from the input file
		// and add each vector to an individual cluster.
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line;
		// for each line until the end of file
		while (((line = reader.readLine()) != null)) { 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			// split the line by spaces
			String[] lineSplited = line.split(" ");
			// convert the values to double values and put them in 
			// a vector of doubles
			double [] vector = new double[lineSplited.length];
			for (int i=0; i< lineSplited.length; i++) { 
				double value = Double.parseDouble(lineSplited[i]);
				vector[i] = value;
//				System.out.println("val");
			}
			// create a DoubleArray object with the vector
			DoubleArray theVector = new DoubleArray(vector);
			
			// Initiallly we create a cluster for each vector
			Cluster cluster = new Cluster(vector.length);
			cluster.addVector(theVector);
			cluster.setMean(theVector.clone());
			clusters.add(cluster);
		}
		reader.close(); // close the input file

		// (2) Loop to combine the two closest clusters into a bigger cluster
		// until no clusters can be combined.
		boolean changed = false;
		do {
			// merge the two closest clusters
			changed = mergeTheClosestCluster();
			// record memory usage
			MemoryLogger.getInstance().checkMemory();
		} while (changed);

		// record end time
		endTimestamp = System.currentTimeMillis();
		
		// return the clusters
		return clusters;
	}

	/**
	 * Merge the two closest clusters in terms of distance.
	 * @return true if a merge was done, otherwise false.
	 */
	private boolean mergeTheClosestCluster() {
		// These variables will contain the two closest clusters that
		// can be merged
		Cluster clusterToMerge1 = null;
		Cluster clusterToMerge2 = null;
		double minClusterDistance = Integer.MAX_VALUE;

		// find the two closest clusters with distance > threshold
		// by comparing all pairs of clusters i and j
		for (int i = 0; i < clusters.size(); i++) {
			for (int j = i + 1; j < clusters.size(); j++) {
				// calculate the distance between i and j
				double distance = euclideanDistance(clusters.get(i).getmean(), clusters.get(j).getmean());
				// if the distance is less than the max distance allowed
				// and if it is the smallest distance until now
				if (distance < minClusterDistance && distance <= maxDistance) {
					// record this pair of clusters
					minClusterDistance = distance;
					clusterToMerge1 = clusters.get(i);
					clusterToMerge2 = clusters.get(j);
				}
			}
		}

		// if no close clusters were found, return false
		if (clusterToMerge1 == null) {
			return false;
		}

		// else, merge the two closest clusters
		for(DoubleArray vector : clusterToMerge2.getVectors()){
			clusterToMerge1.addVector(vector);
		}
		// after mergint, we need to recompute the mean of the resulting cluster
		clusterToMerge1.recomputeClusterMean();
		// we delete the cluster that was merged
		clusters.remove(clusterToMerge2);

		// increase iteration count for statistics
		iterationCount++;
		return true;
	}

	/**
	 * Calculate the eucledian distance between two vectors of doubles.
	 * @param vector1 the first vector
	 * @param vector2 the second vector
	 * @return the distance
	 */
	private double euclideanDistance(DoubleArray vector1, DoubleArray vector2) {
		double sum =0;	
		for(int i=0; i< vector1.data.length; i++){
			sum += Math.pow(vector1.data[i] - vector2.data[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	/**
	 * Save the clusters to an output file
	 * @param output the output file path
	 * @throws IOException exception if there is some writing error.
	 */
	public void saveToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		// for each cluster
		for(int i=0; i< clusters.size(); i++){
			// if the cluster is not empty
			if(clusters.get(i).getVectors().size() >= 1){
				// write the cluster
				writer.write(clusters.get(i).toString());
				// if not the last cluster, add a line return
				if(i < clusters.size()-1){
					writer.newLine();
				}
			}
		}
		// close the file
		writer.close();
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStatistics() {
		System.out.println("========== HIERARCHICAL CLUSTERING - STATS ============");
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println(" Iteration count: " + iterationCount);
		System.out.println("=====================================");
	}

}
