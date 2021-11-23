/*
 * JDiff - HTML report of API differences
 * Copyright (C) 2004  Matthew Doar <mdoar@pobox.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jdiff;

import java.util.Comparator;

/**
 * Class to compare two ClassDiff objects.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class CompareClassPdiffs implements Comparator<ClassDiff> {
    /**
     * Compare two class diffs by their percentage difference,
     * and then by name.
     */
    public int compare(ClassDiff obj1, ClassDiff obj2) {
        if (obj1.pdiff < obj2.pdiff)
            return 1;
        if (obj1.pdiff > obj2.pdiff)
            return -1;
        return obj1.name_.compareTo(obj2.name_);
    }
}
