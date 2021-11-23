package jdiff;

/**
 * Class to represent any (name, type) pair such as a parameter.
 * Analogous to ParamType in the Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this parameter.
 * <p>
 * See the file LICENSE.txt for copyright details.
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
