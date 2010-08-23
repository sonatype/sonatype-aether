package org.sonatype.aether.util.filter;

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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.Version;
import org.sonatype.aether.VersionConstraint;

public abstract class AbstractDependencyFilterTest
{

    protected DependencyFilter getAcceptFilter()
    {
        return new DependencyFilter()
        {

            public boolean accept( DependencyNode node, List<DependencyNode> parents )
            {
                return true;
            }

        };
    }

    protected DependencyFilter getDenyFilter()
    {
        return new DependencyFilter()
        {

            public boolean accept( DependencyNode node, List<DependencyNode> parents )
            {
                return false;
            }
        };
    }

    protected Artifact getArtifactStub()
    {
        return new Artifact()
        {

            public boolean isSnapshot()
            {
                return false;
            }

            public String getVersion()
            {
                return "1.0.3";
            }

            public String getGroupId()
            {
                return "com.example.test";
            }

            public String getArtifactId()
            {
                return "testArtifact";
            }

            public String getExtension()
            {
                return "jar";
            }

            public String getProperty( String key, String defaultValue )
            {
                return null;
            }

            public Map<String, String> getProperties()
            {
                return null;
            }

            public File getFile()
            {
                return null;
            }

            public String getClassifier()
            {
                return null;
            }

            public String getBaseVersion()
            {
                return null;
            }

            public Artifact setVersion( String version )
            {
                return null;
            }

            public Artifact setFile( File file )
            {
                return null;
            }

        };
    }

    protected Artifact getSNAPSHOTStub()
    {
        return new Artifact()
        {

            public boolean isSnapshot()
            {
                return true;
            }

            public String getVersion()
            {
                return "2.0-SNAPSHOT";
            }

            public String getGroupId()
            {
                return "com.example.api";
            }

            public String getArtifactId()
            {
                return "testArtifactAPI";
            }

            public String getExtension()
            {
                return "jar";
            }

            public String getProperty( String key, String defaultValue )
            {
                return null;
            }

            public Map<String, String> getProperties()
            {
                return null;
            }

            public File getFile()
            {
                return null;
            }

            public String getClassifier()
            {
                return null;
            }

            public String getBaseVersion()
            {
                return null;
            }

            public Artifact setVersion( String version )
            {
                return null;
            }

            public Artifact setFile( File file )
            {
                return null;
            }

        };
    }

}
