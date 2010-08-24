package org.sonatype.aether.version;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import java.util.Collection;

/**
 * A constraint on versions for a dependency. A constraint can either consist of one or more version ranges or a single
 * version. In the first case, the constraint expresses a hard requirement on a version matching one of its ranges. In
 * the second case, the constraint expresses a soft requirement on a specific version (i.e. a recommendation).
 * 
 * @author Benjamin Bentmann
 */
public interface VersionConstraint
{

    /**
     * Gets the version ranges of this constraint.
     * 
     * @return The version ranges, may be empty but never {@code null}.
     */
    Collection<VersionRange> getRanges();

    /**
     * Gets the version recommended by this constraint.
     * 
     * @return The recommended version or {@code null} if none.
     */
    Version getVersion();

    /**
     * Determines whether the specified version satisfies this constraint. In more detail, a version satisfies this
     * constraint if it matches at least one version range or if this constraint has no version ranges at all and the
     * specified version equals the version recommended by the constraint.
     * 
     * @param version The version to test, must not be {@code null}.
     * @return {@code true} if the specified version satisfies this constraint, {@code false} otherwise.
     */
    boolean containsVersion( Version version );

}
