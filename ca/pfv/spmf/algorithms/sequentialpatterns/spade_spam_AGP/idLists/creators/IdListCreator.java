package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators;

import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;

/**
 * Interface for a IdList Creator. If we are interested in adding any other kind
 * of IdList, we will have to define a creator which implements these three methods.
 *
 * Copyright Antonio Gomariz Peñalver 2013
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
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author agomariz
 */
public interface IdListCreator {

    /**
     * It creates an empty IdList.
     * @return the idlist
     */
    public IDList create();

    /**
     * It adds an appearance <sid,tid> in a sequence to an Idlist 
     * @param idlist the idlist
     * @param sequence the sequence
     * @param timestamp the timestamp
     */
    public void addAppearance(IDList idlist, Integer sequence, Integer timestamp);

    /**
     * It adds several appearances <sid, {tid_1,tid_2,...,tid_n}> in a sequence to an Idlist
     * @param idlist the idlist
     * @param sequence the sequence
     * @param items a list of items
     */
    public void addAppearancesInSequence(IDList idlist, Integer sequence, List<Integer> items);
}
