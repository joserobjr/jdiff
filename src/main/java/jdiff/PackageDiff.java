package jdiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes between two packages.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class PackageDiff {

    public String name_;

    /**
     * Classes added in the new API.
     */
    public List<ClassAPI> classesAdded = null;
    /**
     * Classes removed in the new API.
     */
    public List<ClassAPI> classesRemoved = null;
    /**
     * Classes changed in the new API.
     */
    public List<ClassDiff> classesChanged = null;

    /**
     * A string describing the changes in documentation.
     */
    public String documentationChange_ = null;

    /* The percentage difference for this package. */
    public double pdiff = 0.0;

    /**
     * Default constructor.
     */
    public PackageDiff(String name) {
        name_ = name;
        classesAdded = new ArrayList<ClassAPI>(); // ClassAPI[]
        classesRemoved = new ArrayList<ClassAPI>(); // ClassAPI[]
        classesChanged = new ArrayList<>(); // ClassDiff[]
    }
}
