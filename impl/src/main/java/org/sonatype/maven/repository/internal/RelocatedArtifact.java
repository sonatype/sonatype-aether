package org.sonatype.maven.repository.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.DerivedArtifact;

/**
 * @author Benjamin Bentmann
 */
class RelocatedArtifact
    extends DerivedArtifact
{

    private final String groupId;

    private final String artifactId;

    public RelocatedArtifact( Artifact artifact, String groupId, String artifactId, String version )
    {
        super( artifact.clone() );
        if ( version != null )
        {
            setVersion( version );
        }
        this.groupId = ( groupId != null ) ? groupId.intern() : "";
        this.artifactId = ( artifactId != null ) ? artifactId.intern() : "";
    }

    @Override
    public String getGroupId()
    {
        if ( groupId.length() > 0 )
        {
            return groupId;
        }
        else
        {
            return super.getGroupId();
        }
    }

    @Override
    public String getArtifactId()
    {
        if ( artifactId.length() > 0 )
        {
            return artifactId;
        }
        else
        {
            return super.getArtifactId();
        }
    }

}
