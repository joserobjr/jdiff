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

/**
 * Class to represent a single documentation difference.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class DiffOutput implements Comparable<DiffOutput> {

    /**
     * The package name for this difference.
     */
    public String pkgName_;

    /**
     * The class name for this difference, may be null.
     */
    public String className_;

    /**
     * The HTML named anchor identifier for this difference.
     */
    public String id_;

    /**
     * The title for this difference.
     */
    public String title_;

    /**
     * The text for this difference, with deleted and added words marked.
     */
    public String text_;

    /**
     * Constructor.
     */
    public DiffOutput(String pkgName, String className, String id,
                      String title, String text) {
        pkgName_ = pkgName;
        className_ = className;
        id_ = id;
        title_ = title;
        text_ = text;
    }

    /**
     * Compare two DiffOutput objects, so they will appear in the correct
     * package.
     */
    public int compareTo(DiffOutput o) {
        int comp = pkgName_.compareTo(o.pkgName_);
        if (comp != 0)
            return comp;
        // Always put the package-level output at the top - not yet working
//        if (id_.compareTo("package") == 0)
//            return -1;
        return id_.compareTo(o.id_);
    }

}  
