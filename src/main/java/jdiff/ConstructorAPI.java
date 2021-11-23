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
 * Class to represent a constructor, analogous to ConstructorDoc in the
 * Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this constructor.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class ConstructorAPI implements Comparable<ConstructorAPI> {
    /**
     * The type of the constructor, being all the parameter types
     * separated by commas.
     */
    public String type_;

    /**
     * The exceptions thrown by this constructor, being all the exception types
     * separated by commas. "no exceptions" if no exceptions are thrown.
     */
    public String exceptions_ = "no exceptions";

    /**
     * Modifiers for this class.
     */
    public Modifiers modifiers_;

    /**
     * The doc block, default is null.
     */
    public String doc_;

    /**
     * Constructor.
     */
    public ConstructorAPI(String type, Modifiers modifiers) {
        type_ = type;
        modifiers_ = modifiers;
    }

    /**
     * Compare two ConstructorAPI objects by type and modifiers.
     */
    public int compareTo(ConstructorAPI o) {
        int comp = type_.compareTo(o.type_);
        if (comp != 0)
            return comp;
        comp = exceptions_.compareTo(o.exceptions_);
        if (comp != 0)
            return comp;
        comp = modifiers_.compareTo(o.modifiers_);
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, o.doc_))
            return -1;
        return 0;
    }

    /**
     * Tests two constructors, using just the type, used by indexOf().
     */
    public boolean equals(Object o) {
        return this == o || o instanceof ConstructorAPI && type_.compareTo(((ConstructorAPI) o).type_) == 0;
    }
}  
