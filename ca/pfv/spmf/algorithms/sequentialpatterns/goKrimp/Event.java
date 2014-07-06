
package ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp;

/**
 * This is an implementation of an "event" for the GoKrimp and SedKrimp algorithms
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
 * @see DataReader
 * @see Event
 * @see MyPattern
 * @see SignTest
*  @author  Hoang Thanh Lam (TU Eindhoven and IBM Research)
 */
class Event implements Comparable<Event> {
    int id; // id of the event
    int ts; // timestamp
    int gap; //gap to previous timestamp
    
    @Override
    /**
     * compare two events by timestamp
     */
    public int compareTo(Event o) {
        return this.ts - o.ts ;
    }
}

