package jdiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a class, analogous to ClassDoc in the
 * Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this class.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class ClassAPI implements Comparable<ClassAPI> {

    /**
     * Name of the class, not fully qualified.
     */
    public String name_;

    /**
     * Set if this class is an interface.
     */
    public boolean isInterface_;

    /**
     * Set if this class is abstract.
     */
    boolean isAbstract_;

    /**
     * Modifiers for this class.
     */
    public Modifiers modifiers_;

    /**
     * Name of the parent class, or null if there is no parent.
     */
    public String extends_; // Can only extend zero or one class or interface

    /**
     * Interfaces implemented by this class.
     */
    public List<String> implements_; // String[]

    /**
     * Constructors in this class.
     */
    public List<ConstructorAPI> ctors_; // ConstructorAPI[]

    /**
     * Methods in this class.
     */
    public List<MethodAPI> methods_; // MethodAPI[]

    /**
     * Fields in this class.
     */
    public List<FieldAPI> fields_; //FieldAPI[]

    /**
     * The doc block, default is null.
     */
    public String doc_;

    /**
     * Constructor.
     */
    public ClassAPI(String name, String parent, boolean isInterface,
                    boolean isAbstract, Modifiers modifiers) {
        name_ = name;
        extends_ = parent;
        isInterface_ = isInterface;
        isAbstract_ = isAbstract;
        modifiers_ = modifiers;

        implements_ = new ArrayList<>(); // String[]
        ctors_ = new ArrayList<>(); // ConstructorAPI[]
        methods_ = new ArrayList<>(); // MethodAPI[]
        fields_ = new ArrayList<>(); // FieldAPI[]
    }

    /**
     * Compare two ClassAPI objects by all the known information.
     */
    public int compareTo(ClassAPI o) {
        int comp = name_.compareTo(o.name_);
        if (comp != 0)
            return comp;
        if (isInterface_ != o.isInterface_)
            return -1;
        if (isAbstract_ != o.isAbstract_)
            return -1;
        comp = modifiers_.compareTo(o.modifiers_);
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, o.doc_))
            return -1;
        return 0;
    }

    /**
     * Tests two methods for equality using just the class name,
     * used by indexOf().
     */
    public boolean equals(Object o) {
        return this == o || o instanceof ClassAPI && name_.compareTo(((ClassAPI) o).name_) == 0;
    }

}
