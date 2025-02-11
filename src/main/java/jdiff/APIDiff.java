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

import java.util.ArrayList;
import java.util.List;

/**
 * The class contains the changes between two API objects; packages added,
 * removed and changed. The packages are represented by PackageDiff objects,
 * which contain the changes in each package, and so on.
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

