
package ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp;

import java.util.ArrayList;


/**
 * This is an implementation of a "pattern" as used by the GoKrimp and SedKrimp algorithms
 * <br/><br/>
 * For more information please refer to the paper Mining Compressing Sequential Patterns in the Journal Statistical Analysis and Data Mining
 * <br/><br/>
 * 
 * Copyright (c) 2014  Hoang Thanh Lam (TU Eindhoven and IBM Research)
 * Toon Calders (Université Libre de Bruxelles), Fabian Moerchen (Amazon.com inc)
 * and Dmitriy Fradkin (Siemens Corporate Research)
 * <br/><br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * <br/><br/>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <br/><br/>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <br/><br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoGoKrimp
 * @see Event
 * @see MyPattern
 * @see SignTest
*  @author  Hoang Thanh Lam (TU Eindhoven and IBM Research)
 */
public class MyPattern implements Comparable<MyPattern> {
 ArrayList<Integer> ids;
 double ben; // the compression benefit of using this pattern
 int freq; // the number of time this pattern is used
 int g_cost; // the cost of encoding the gaps  
 
 /**
  * print the set of patterns
  */
 void print(){
     System.out.print(ids);
     System.out.println(" " + ben);
 }
 
 /**
  * constructor
  */
 MyPattern(){
     ids=new ArrayList<Integer>();
 }
 
 /**
  * copy constructor 
  * @param p 
  */
 MyPattern(MyPattern p){
     ben=p.ben;
     freq=p.freq;
     g_cost=p.g_cost;
     ids=new ArrayList<Integer>(p.ids);
 }
 
  @Override
  /**
   * compare two patterns by benefits
   */
   public int compareTo(MyPattern o) {
        return (int)(o.ben-this.ben);
   }
}
