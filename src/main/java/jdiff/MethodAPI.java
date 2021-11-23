package jdiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a method, analogous to MethodDoc in the
 * Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this method.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class MethodAPI implements Comparable<MethodAPI> {

    /**
     * Name of the method.
     */
    public String name_;

    /**
     * Return type of the method.
     */
    public String returnType_;

    /**
     * The fully qualified name of the class or interface this method is
     * inherited from. If this is null, then the method is defined locally
     * in this class or interface.
     */
    public String inheritedFrom_;

    /**
     * The exceptions thrown by this method, being all the exception types
     * separated by commas. "no exceptions" if no exceptions are thrown.
     */
    public String exceptions_ = "no exceptions";

    /**
     * Set if this method is abstract.
     */
    public boolean isAbstract_;

    /**
     * Set if this method is native.
     */
    public boolean isNative_;

    /**
     * Set if this method is synchronized.
     */
    public boolean isSynchronized_;

    /**
     * Modifiers for this class.
     */
    public Modifiers modifiers_;

    public List<ParamAPI> params_; // ParamAPI[]

    /**
     * The doc block, default is null.
     */
    public String doc_;

    /**
     * Cached result of getSignature().
     */
    public String signature_ = null;

    /**
     * Constructor.
     */
    public MethodAPI(String name, String returnType, boolean isAbstract,
                     boolean isNative, boolean isSynchronized,
                     Modifiers modifiers) {
        name_ = name;
        returnType_ = returnType;
        isAbstract_ = isAbstract;
        isNative_ = isNative;
        isSynchronized_ = isSynchronized;
        modifiers_ = modifiers;
        params_ = new ArrayList<>(); // ParamAPI[]
    }

    /**
     * Copy constructor.
     */
    public MethodAPI(MethodAPI m) {
        name_ = m.name_;
        returnType_ = m.returnType_;
        inheritedFrom_ = m.inheritedFrom_;
        exceptions_ = m.exceptions_;
        isAbstract_ = m.isAbstract_;
        isNative_ = m.isNative_;
        isSynchronized_ = m.isSynchronized_;
        modifiers_ = m.modifiers_; // Note: shallow copy
        params_ = m.params_; // Note: shallow copy
        doc_ = m.doc_;
        signature_ = m.signature_; // Cached
    }

    /**
     * Compare two methods, including the return type, and parameter
     * names and types, and modifiers.
     */
    public int compareTo(MethodAPI o) {
        int comp = name_.compareTo(o.name_);
        if (comp != 0)
            return comp;
        comp = returnType_.compareTo(o.returnType_);
        if (comp != 0)
            return comp;
        if (APIComparator.changedInheritance(inheritedFrom_, o.inheritedFrom_) != 0)
            return -1;
        if (isAbstract_ != o.isAbstract_) {
            return -1;
        }
        if (Diff.showAllChanges &&
                isNative_ != o.isNative_) {
            return -1;
        }
        if (Diff.showAllChanges &&
                isSynchronized_ != o.isSynchronized_) {
            return -1;
        }
        comp = exceptions_.compareTo(o.exceptions_);
        if (comp != 0)
            return comp;
        comp = modifiers_.compareTo(o.modifiers_);
        if (comp != 0)
            return comp;
        comp = getSignature().compareTo(o.getSignature());
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, o.doc_))
            return -1;
        return 0;
    }

    /**
     * Tests two methods, using just the method name, used by indexOf().
     */
    public boolean equals(Object o) {
        return this == o || o instanceof MethodAPI && name_.compareTo(((MethodAPI) o).name_) == 0;
    }

    /**
     * Tests two methods for equality, using just the signature.
     */
    public boolean equalSignatures(MethodAPI o) {
        return this == o || getSignature().compareTo((o).getSignature()) == 0;
    }

    /**
     * Return the signature of the method.
     */
    public String getSignature() {
        if (signature_ != null)
            return signature_;
        StringBuilder res = new StringBuilder();
        boolean first = true;
        for (ParamAPI paramAPI : params_) {
            if (!first)
                res.append(", ");
            res.append(paramAPI.toString());
            first = false;
        }
        signature_ = res.toString();
        return res.toString();
    }
}
