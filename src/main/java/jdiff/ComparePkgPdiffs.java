/*
 * JDiff - HTML report of API differences
 * Copyright (C) 2021  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package jdiff;

import java.util.Comparator;

/**
 * Class to compare two PackageDiff objects.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class ComparePkgPdiffs implements Comparator<PackageDiff> {
    /**
     * Compare two package diffs by their percentage difference,
     * and then by name.
     */
    public int compare(PackageDiff obj1, PackageDiff obj2) {
        if (obj1.pdiff < obj2.pdiff)
            return 1;
        if (obj1.pdiff > obj2.pdiff)
            return -1;
        return obj1.name_.compareTo(obj2.name_);
    }
}
