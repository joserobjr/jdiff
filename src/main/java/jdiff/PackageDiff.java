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

import java.util.ArrayList;
import java.util.List;

/**
 * Changes between two packages.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class PackageDiff {

    public String name_;

    /**
     * Classes added in the new API.
     */
    public List<ClassAPI> classesAdded = new ArrayList<>();

    /**
     * Classes removed in the new API.
     */
    public List<ClassAPI> classesRemoved = new ArrayList<>();

    /**
     * Classes changed in the new API.
     */
    public List<ClassDiff> classesChanged = new ArrayList<>();

    /**
     * A string describing the changes in documentation.
     */
    public String documentationChange_;

    /* The percentage difference for this package. */
    public double pdiff;

    /**
     * Default constructor.
     */
    public PackageDiff(String name) {
        name_ = name;
    }
}
