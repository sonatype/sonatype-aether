package org.sonatype.aether;

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

/**
 * A version scheme that handles interpretation of version strings to facilitate their comparison.
 * 
 * @author Benjamin Bentmann
 * @author Alin Dreghiciu
 */
public interface VersionScheme
{

    /**
     * Parses the specified version string, for example "1.0".
     * 
     * @param version The version string to parse, must not be {@code null}.
     * @return The parsed version, never {@code null}.
     * @throws InvalidVersionSpecificationException If the string violates the syntax rules of this scheme.
     */
    Version parseVersion( String version )
        throws InvalidVersionSpecificationException;

    /**
     * Parses the specified version range specification, for example "[1.0,2.0)".
     * 
     * @param range The range specification to parse, must not be {@code null}.
     * @return The parsed version range, never {@code null}.
     * @throws InvalidVersionSpecificationException If the range specification violates the syntax rules of this scheme.
     */
    VersionRange parseVersionRange( String range )
        throws InvalidVersionSpecificationException;

    /**
     * Parses the specified version constraint specification, for example "1.0" or "[1.0,2.0),(2.0,)".
     * 
     * @param constraint The constraint specification to parse, must not be {@code null}.
     * @return The parsed version constraint, never {@code null}.
     * @throws InvalidVersionSpecificationException If the constraint specification violates the syntax rules of this
     *             scheme.
     */
    VersionConstraint parseVersionConstraint( final String constraint )
        throws InvalidVersionSpecificationException;

}
