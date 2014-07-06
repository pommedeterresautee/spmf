/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that implements a qualitative abstraction. Two different values are
 * possible: to be with an equal relation with respect to a previous pair (if
 * occurs at the same time), or to be with an after relation with respect to that
 * previous pair (the previous pair have a before relation with respect to this one)
 * 
 * Copyright Antonio Gomariz Peñalver 2013
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
 * 
 * @author agomariz
 */
public class Abstraction_Qualitative extends Abstraction_Generic {

    /**
     * Two options: to have and equal relation respect the previous one, or not to have it
     */
    private boolean equalRelation;
    private static Map<Boolean, Abstraction_Qualitative> pool = new HashMap<Boolean, Abstraction_Qualitative>(4);
    static{
        Abstraction_Qualitative trueValue = new Abstraction_Qualitative(true);
            Abstraction_Qualitative falseValue = new Abstraction_Qualitative(false);
            pool.put(Boolean.TRUE, trueValue);
            pool.put(Boolean.FALSE, falseValue);
    }

    public Abstraction_Qualitative(boolean equalRelation) {
        super();
        this.equalRelation = equalRelation;
    }

    public static Abstraction_Qualitative create(boolean equalRelation) {
        return pool.get(equalRelation);
        //return new Abstraction_Qualitative(mismoItemset);
    }

    @Override
    public boolean equals(Object arg) {
        Abstraction_Qualitative aq = (Abstraction_Qualitative) arg;
        if (equalRelation == aq.hasEqualRelation()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.equalRelation ? 1 : 0);
        return hash;
    }

    public int compareTo(Abstraction_Generic o) {
        Abstraction_Qualitative aq = (Abstraction_Qualitative) o;
        if (equalRelation == aq.hasEqualRelation()) {
            return 0;
        } else if (!equalRelation) {
            return -1;
        }

        return 1;
    }

    public boolean hasEqualRelation() {
        return equalRelation;
    }

    /**
     * Get the string representation of this object.
     * @return the string representation.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (!hasEqualRelation()) {
            result.append(" ->");
        }
        return result.toString();
    }

    public static void clear() {
        //pool.clear();
    }

    /**
     * Get a string representation in SPMF format.
     * @return the string representation
     */
    @Override
    public String toStringToFile() {
        StringBuilder result = new StringBuilder();
        if (!hasEqualRelation()) {
            result.append(" -1");
        }
        return result.toString();
    }
}
