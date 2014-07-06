package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators;

import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.abstracciones.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.patterns.Pattern;

/**
 * Abstract class that is thought to make it possible the creation of any kind
 * of abstractions.
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
public abstract class AbstractionCreator {

    public abstract Abstraction_Generic createDefaultAbstraction();

    public abstract Pattern getSubpattern(Pattern extension, int i);
    
    public abstract void clear();

    public abstract boolean isSubpattern(Pattern aThis, Pattern p, int i, List<Integer> posiciones);
}
