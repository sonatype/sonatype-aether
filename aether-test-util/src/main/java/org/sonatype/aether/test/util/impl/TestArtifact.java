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
package org.sonatype.aether.test.util.impl;

import java.io.File;
import java.util.Map;

import org.sonatype.aether.artifact.Artifact;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class TestArtifact
    implements Artifact
{

    private String version;
    private String extension;
    private String classifier;
    private String artifactId;
    private String groupId;
    private Map<String, String> properties;
    private File file;
    private String baseVersion;
    private boolean snapshot;

    /**
     * @param groupId
     * @param artifactId
     * @param classifier
     * @param ext
     * @param version
     */
    public TestArtifact( String groupId, String artifactId, String classifier, String ext, String version )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.extension = ext;
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

    public Artifact setVersion( String version )
    {
        this.version = version;
        return this;
    }

    public String getExtension()
    {
        return extension;
    }

    public void setExtension( String extension )
    {
        this.extension = extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.artifact.Artifact#getBaseVersion()
     */
    public String getBaseVersion()
    {
        return baseVersion;
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.artifact.Artifact#isSnapshot()
     */
    public boolean isSnapshot()
    {
        return snapshot;
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.artifact.Artifact#getFile()
     */
    public File getFile()
    {
        return file;
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.artifact.Artifact#setFile(java.io.File)
     */
    public Artifact setFile( File file )
    {
        this.file = file;
        return this;
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.artifact.Artifact#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty( String key, String defaultValue )
    {
        String value = properties.get(key);
        return value != null ? value : defaultValue;
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.artifact.Artifact#getProperties()
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties( Map<String, String> properties )
    {
        this.properties = properties;
    }

    public void setBaseVersion( String baseVersion )
    {
        this.baseVersion = baseVersion;
    }

    public void setSnapshot( boolean snapshot )
    {
        this.snapshot = snapshot;
    }
}