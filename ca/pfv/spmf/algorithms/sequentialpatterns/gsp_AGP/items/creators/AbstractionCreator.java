/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators;


import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.CandidateInSequenceFinder;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.Pattern;

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
public interface AbstractionCreator {

    public Abstraction_Generic CreateDefaultAbstraction();

    public List<Pattern> createSize2Sequences(List<Sequence> sequences);

    public Pattern getSubpattern(Pattern extension, int i);

    public List<Pattern> createSize2Sequences(Map<Integer, Map<Item, List<Integer>>> bbddHorizontal, Map<Item, Pattern> itemsfrecuentes);

    public void clear();

    public Abstraction_Generic createAbstraction(long timeActual, long timeAnterior);

    public int[] findPositionOfItemInSequence(Sequence secuencia, Item itemPar, Abstraction_Generic absPar, Abstraction_Generic absAnterior, int indexItemset,int indexitem, int indexItemsetAnterior,int indexitemAnterior);

    public Pattern generateCandidates(AbstractionCreator creador, Pattern patron1, Pattern patron2,double minSupport);

    public void isCandidateInSequence(CandidateInSequenceFinder buscador, Pattern candidato, Sequence secuencia, int k, int i, List<int[]> posicion);

    public List<Pattern> generateSize2Candidates(AbstractionCreator creador, Pattern get, Pattern get0);
}
