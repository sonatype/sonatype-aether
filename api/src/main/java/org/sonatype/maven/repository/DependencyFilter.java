package org.sonatype.maven.repository;

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
 * A filter to include/exclude dependency nodes during other operations.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyFilter
{

    /**
     * Indicates whether the specified dependency node shall be included or excluded.
     * 
     * @param node The dependency node to filter, must not be {@code null}.
     * @return {@code true} to include the dependency node, {@code false} to exclude it.
     */
    boolean filterDependency( DependencyNode node );

}
