package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Arrays;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.test.util.impl.StubVersion;
import org.sonatype.aether.version.Version;

/**
 * @author Benjamin Hanzelmann
 */
public class StubVersionRangeResolver
    implements VersionRangeResolver
{

    public VersionRangeResult resolveVersionRange( RepositorySystemSession session, VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        String version = request.getArtifact().getVersion();
        boolean range = false;

        if ( version.matches( "\\[[^,]+,.*" ) )
        {
            version = version.substring( 1, version.indexOf( ',', 1 ) );
            range = true;
        }

        VersionRangeResult result = new VersionRangeResult( request );
        StubVersion ver = new StubVersion( version );
        result.setVersions( Arrays.asList( (Version) ver ) );
        if ( range && !request.getRepositories().isEmpty() )
        {
            result.setRepository( ver, request.getRepositories().get( 0 ) );
        }

        return result;
    }

}
