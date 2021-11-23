package jdiff;

/**
 * The changes between two class constructor, method or field members.
 * <p>
 * See the file LICENSE.txt for copyright details.
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
