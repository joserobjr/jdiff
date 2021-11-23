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

/**
 * The changes between two class constructor, method or field members.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class MemberDiff {

    /**
     * The name of the member.
     */
    public String name_;

    /**
     * The old member type. For methods, this is the return type.
     */
    public String oldType_;

    /**
     * The new member type. For methods, this is the return type.
     */
    public String newType_;

    /**
     * The old signature. Null except for methods.
     */
    public String oldSignature_;

    /**
     * The new signature. Null except for methods.
     */
    public String newSignature_;

    /**
     * The old list of exceptions. Null except for methods and constructors.
     */
    public String oldExceptions_;

    /**
     * The new list of exceptions. Null except for methods and constructors.
     */
    public String newExceptions_;

    /**
     * A string describing the changes in documentation.
     */
    public String documentationChange_;

    /**
     * A string describing the changes in modifiers.
     * Changes can be in whether this is abstract, static, final, and in
     * its visibility.
     * Null if no change.
     */
    public String modifiersChange_;

    /**
     * The class name where the new member is defined.
     * Null if no change in inheritance.
     */
    public String inheritedFrom_;

    /**
     * Default constructor.
     */
    public MemberDiff(String name) {
        name_ = name;
    }

    /**
     * Add a change in the modifiers.
     */
    public void addModifiersChange(String commonModifierChanges) {
        if (commonModifierChanges != null) {
            if (modifiersChange_ == null)
                modifiersChange_ = commonModifierChanges;
            else
                modifiersChange_ += " " + commonModifierChanges;
        }
    }
}
