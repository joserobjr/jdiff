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
 * Class to represent any (name, type) pair such as a parameter.
 * Analogous to ParamType in the Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this parameter.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class ParamAPI implements Comparable<ParamAPI> {
    /**
     * Name of the (name, type) pair.
     */
    public String name_;

    /**
     * Type of the (name, type) pair.
     */
    public String type_;

    public ParamAPI(String name, String type) {
        name_ = name;
        type_ = type;
    }

    /**
     * Compare two ParamAPI objects using both name and type.
     */
    public int compareTo(ParamAPI o) {
        int comp = name_.compareTo(o.name_);
        if (comp != 0)
            return comp;
        comp = type_.compareTo(o.type_);
        return comp;
    }

    /**
     * Tests two ParamAPI objects using just the name, used by indexOf().
     */
    public boolean equals(Object o) {
        return o == this || o instanceof ParamAPI && name_.compareTo(((ParamAPI) o).name_) == 0;
    }

    /**
     * Used to create signatures.
     */
    public String toString() {
        if (type_.compareTo("void") == 0)
            return "";
        return type_;
    }
}  
