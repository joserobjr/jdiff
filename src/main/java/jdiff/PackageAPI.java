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
 * Class to represent a package, analogous to PackageDoc in the
 * Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this package.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class PackageAPI implements Comparable<PackageAPI> {

    /**
     * Full qualified name of the package.
     */
    public String name_;

    /**
     * Classes within this package.
     */
    public List<ClassAPI> classes_ = new ArrayList<>();

    /**
     * The doc block, default is null.
     */
    public String doc_;

    /**
     * Constructor.
     */
    public PackageAPI(String name) {
        name_ = name;
    }

    /**
     * Compare two PackageAPI objects by name.
     */
    public int compareTo(PackageAPI o) {
        if (APIComparator.docChanged(doc_, o.doc_))
            return -1;
        return name_.compareTo(o.name_);
    }

    /**
     * Tests two packages, using just the package name, used by indexOf().
     */
    public boolean equals(Object o) {
        return o instanceof PackageAPI && name_.compareTo(((PackageAPI) o).name_) == 0;
    }
}
