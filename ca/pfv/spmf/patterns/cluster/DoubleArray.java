package ca.pfv.spmf.patterns.cluster;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * This class represents a vector of double values, used mainly by clustering algorithms
 * such as KMeans.
* 
 * @author Philippe Fournier-Viger
 */
public class DoubleArray {

	// the vector
	public double[] data;
	
	/**
	 * Constructor
	 * @param data an array of double values
	 */
	public DoubleArray(double [] data){
		this.data = data;
	}
	
	/**
	 * Get a string representation of this double array.
	 * @return a string
	 */
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<data.length; i++){
			buffer.append(data[i]);
			if(i < data.length -1){
				buffer.append(",");
			}
		}
		return buffer.toString();
	}
	
	/**
	 * Return a copy of this double array
	 */
	public DoubleArray clone(){
		return new DoubleArray(data.clone());
	}
}
