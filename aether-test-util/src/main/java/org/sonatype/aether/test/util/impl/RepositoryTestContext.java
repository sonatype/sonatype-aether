package org.sonatype.aether.test.util.impl;

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

import org.sonatype.aether.Artifact;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.test.impl.RecordingRepositoryListener;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;

public class RepositoryTestContext
{
    TestRepositorySystemSession session;

    Artifact artifact;

    public TestRepositorySystemSession getSession()
    {
        return session;
    }

    public void setSession( TestRepositorySystemSession session )
    {
        this.session = session;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }
    
    public RecordingRepositoryListener getRecordingRepositoryListener()
    {
        if ( session.getRepositoryListener() instanceof RecordingRepositoryListener )
        {
            return (RecordingRepositoryListener) session.getRepositoryListener();
        }
        else
        {
            return new RecordingRepositoryListener( session.getRepositoryListener() );
        }
    }
}
