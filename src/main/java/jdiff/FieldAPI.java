package jdiff;

/**
 * Class to represent a field, analogous to FieldDoc in the
 * Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this field.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class FieldAPI implements Comparable<FieldAPI> {
    /**
     * Name of the field.
     */
    public String name_;

    /**
     * Type of the field.
     */
    public String type_;

    /**
     * The fully qualified name of the class or interface this field is
     * inherited from. If this is null, then the field is defined locally
     * in this class or interface.
     */
    public String inheritedFrom_ = null;

    /**
     * Set if this field is transient.
     */
    public boolean isTransient_ = false;

    /**
     * Set if this field is volatile.
     */
    public boolean isVolatile_ = false;

    /**
     * If non-null, this is the value of this field.
     */
    public String value_ = null;

    /**
     * Modifiers for this class.
     */
    public Modifiers modifiers_;

    /**
     * The doc block, default is null.
     */
    public String doc_ = null;

    /**
     * Constructor.
     */
    public FieldAPI(String name, String type,
                    boolean isTransient, boolean isVolatile,
                    String value, Modifiers modifiers) {
        name_ = name;
        type_ = type;
        isTransient_ = isTransient;
        isVolatile_ = isVolatile;
        value_ = value;
        modifiers_ = modifiers;
    }

    /**
     * Copy constructor.
     */
    public FieldAPI(FieldAPI f) {
        name_ = f.name_;
        type_ = f.type_;
        inheritedFrom_ = f.inheritedFrom_;
        isTransient_ = f.isTransient_;
        isVolatile_ = f.isVolatile_;
        value_ = f.value_;
        modifiers_ = f.modifiers_; // Note: shallow copy
        doc_ = f.doc_;
    }

    /**
     * Compare two FieldAPI objects, including name, type and modifiers.
     */
    public int compareTo(FieldAPI o) {
        int comp = name_.compareTo(o.name_);
        if (comp != 0)
            return comp;
        comp = type_.compareTo(o.type_);
        if (comp != 0)
            return comp;
        if (APIComparator.changedInheritance(inheritedFrom_, o.inheritedFrom_) != 0)
            return -1;
        if (isTransient_ != o.isTransient_) {
            return -1;
        }
        if (isVolatile_ != o.isVolatile_) {
            return -1;
        }
        if (value_ != null && o.value_ != null) {
            comp = value_.compareTo(o.value_);
            if (comp != 0)
                return comp;
        }
        comp = modifiers_.compareTo(o.modifiers_);
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, o.doc_))
            return -1;
        return 0;
    }

    /**
     * Tests two fields, using just the field name, used by indexOf().
     */
    public boolean equals(Object o) {
        return name_.compareTo(((FieldAPI) o).name_) == 0;
    }
}  
