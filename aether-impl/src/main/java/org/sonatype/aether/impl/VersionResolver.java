package org.sonatype.aether.impl;

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

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.VersionRequest;
import org.sonatype.aether.VersionResolutionException;
import org.sonatype.aether.VersionResult;

/**
 * @author Benjamin Bentmann
 */
public interface VersionResolver
{

    /**
     * Resolves an artifact's meta version (if any) to a concrete version. For example, resolves "1.0-SNAPSHOT" to
     * "1.0-20090208.132618-23" or "RELEASE"/"LATEST" to "2.0".
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The version request, must not be {@code null}
     * @return The version result, never {@code null}.
     * @throws VersionResolutionException If the metaversion could not be resolved.
     */
    VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
        throws VersionResolutionException;

}
