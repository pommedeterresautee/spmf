package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.patterns.Pattern;

/**
 * Inspired in SPMF This class implements a list of frequent sequence lists (or
 * frequent pattern lists) that it is organized by levels. That level contains
 * all of sequences that have a concrete number of items. Therefore, we allocate
 * 1-sequences in level 1, 2-sequences in level 2, and so forth...
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
public class Sequences {

    public List<List<Pattern>> levels = new ArrayList<List<Pattern>>();
    public int numberOfFrequentSequences = 0;
    private final String string;

    public Sequences(String name) {
        this.string = name;
        levels.add(new ArrayList<Pattern>());
    }

    public void printFrequentSequences() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder(200);
        int levelCount = 0;
        if (levels != null) {
            for (List<Pattern> level : levels) {
                r.append("\n***Level ").append(levelCount).append("***\n\n");
                for (Pattern sequence : level) {
                    r.append(sequence.toString());
                    r.append('\n');
                }
                levelCount++;
            }
        }
        return r.toString();
    }

    public String toStringToFile() {
        StringBuilder r = new StringBuilder(200);
        int levelCount = 0;
        if (levels != null) {
            for (List<Pattern> level : levels) {
                r.append("\n***Level ").append(levelCount).append("***\n\n");
                for (Pattern sequence : level) {
                    r.append(sequence.toStringToFile());
                    r.append('\n');
                }
                levelCount++;
            }
        }
        return r.toString();
    }

    public void addSequence(Pattern sequence, int level) {
        while (levels.size() <= level) {
            levels.add(new ArrayList<Pattern>());
        }
        levels.get(level).add(sequence);
        numberOfFrequentSequences++;
    }

    public List<Pattern> getLevel(int level) {
        return levels.get(level);
    }

    public int getLevelCount() {
        return levels.size();
    }

    public List<List<Pattern>> getLevels() {
        return levels;
    }

    public int size() {
        int total = 0;
        for (List<Pattern> level : levels) {
            total = total + level.size();
        }
        return total;
    }

    public void sort() {
        for (List<Pattern> nivel : levels) {
            Collections.sort(nivel);
        }
    }

    public void clear() {
        if (levels != null) {
            for (List<Pattern> nivel : levels) {
                nivel.clear();
            }
            levels.clear();
            levels = null;
        }
    }
}
