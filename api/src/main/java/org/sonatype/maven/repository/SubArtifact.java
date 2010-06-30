package org.sonatype.maven.repository;

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

import java.io.File;
import java.util.Map;

/**
 * @author Benjamin Bentmann
 */
public final class SubArtifact
    extends AbstractArtifact
{

    private final Artifact mainArtifact;

    private final String classifier;

    private final String extension;

    public SubArtifact( Artifact mainArtifact, String classifier, String extension )
    {
        if ( mainArtifact == null )
        {
            throw new IllegalArgumentException( "no artifact specified" );
        }
        this.mainArtifact = mainArtifact;
        this.classifier = classifier;
        this.extension = extension;
    }

    public String getGroupId()
    {
        return mainArtifact.getGroupId();
    }

    public String getArtifactId()
    {
        return mainArtifact.getArtifactId();
    }

    public String getVersion()
    {
        return mainArtifact.getVersion();
    }

    public Artifact setVersion( String version )
    {
        return new DefaultArtifact( getGroupId(), getArtifactId(), getClassifier(), getExtension(), version, getFile(),
                                    getProperties() );
    }

    public SnapshotHandler getSnapshotHandler()
    {
        return mainArtifact.getSnapshotHandler();
    }

    public String getBaseVersion()
    {
        return mainArtifact.getBaseVersion();
    }

    public boolean isSnapshot()
    {
        return mainArtifact.isSnapshot();
    }

    public String getClassifier()
    {
        return expand( classifier, mainArtifact.getClassifier() );
    }

    public String getExtension()
    {
        return expand( extension, mainArtifact.getExtension() );
    }

    public File getFile()
    {
        return mainArtifact.getFile();
    }

    public Artifact setFile( File file )
    {
        return new DefaultArtifact( getGroupId(), getArtifactId(), getClassifier(), getExtension(), getVersion(), file,
                                    getProperties() );
    }

    public String getProperty( String key, String defaultValue )
    {
        return mainArtifact.getProperty( key, defaultValue );
    }

    public Map<String, String> getProperties()
    {
        return mainArtifact.getProperties();
    }

    private static String expand( String pattern, String replacement )
    {
        String result = "";
        if ( pattern != null )
        {
            result = pattern.replace( "*", replacement );

            if ( replacement.length() <= 0 )
            {
                if ( pattern.startsWith( "*" ) )
                {
                    int i = 0;
                    for ( ; i < result.length(); i++ )
                    {
                        char c = result.charAt( i );
                        if ( c != '-' && c != '.' )
                        {
                            break;
                        }
                    }
                    result = result.substring( i );
                }
                if ( pattern.endsWith( "*" ) )
                {
                    int i = result.length() - 1;
                    for ( ; i >= 0; i-- )
                    {
                        char c = result.charAt( i );
                        if ( c != '-' && c != '.' )
                        {
                            break;
                        }
                    }
                    result = result.substring( 0, i + 1 );
                }
            }
        }
        return result;
    }

}
