package jdiff;

import java.util.ArrayList;
import java.util.List;

/**
 * The class contains the changes between two API objects; packages added,
 * removed and changed. The packages are represented by PackageDiff objects,
 * which contain the changes in each package, and so on.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
public class APIDiff {

    /**
     * Name of the old API.
     */
    public static String oldAPIName_; //FIXME Why is this static?

    /**
     * Name of the old API.
     */
    public static String newAPIName_; //FIXME Why is this static?

    // PackageAPI[]

    /**
     * Packages added in the new API.
     */
    public List<PackageAPI> packagesAdded = new ArrayList<>();

    // PackageAPI[]

    /**
     * Packages removed in the new API.
     */
    public List<PackageAPI> packagesRemoved = new ArrayList<>();

    // PackageDiff[]

    /**
     * Packages changed in the new API.
     */
    public List<PackageDiff> packagesChanged = new ArrayList<>();

    /**
     * The overall percentage difference between the two APIs.
     */
    public double pdiff;

    /**
     * Default constructor.
     */
    public APIDiff() {
        oldAPIName_ = null;
        newAPIName_ = null;
    }
}

