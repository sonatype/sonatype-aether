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
 * @author Benjamin Bentmann
 */
public class Inclusion
{

    private String groupId = "";

    private String artifactId = "";

    private String classifier = "";

    private String type = "";

    public Inclusion()
    {
        // enables default constructor
    }

    public Inclusion( String groupId, String artifactId, String classifier, String type )
    {
        setGroupId( groupId );
        setArtifactId( artifactId );
        setClassifier( classifier );
        setType( type );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public Inclusion setGroupId( String groupId )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public Inclusion setArtifactId( String artifactId )
    {
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        return this;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public Inclusion setClassifier( String classifier )
    {
        this.classifier = ( classifier != null ) ? classifier : "";
        return this;
    }

    public String getType()
    {
        return type;
    }

    public Inclusion setType( String type )
    {
        this.type = ( type != null ) ? type : "";
        return this;
    }

    @Override
    public String toString()
    {
        return getGroupId() + ':' + getArtifactId() + ':' + getType()
            + ( getClassifier().length() > 0 ? ':' + getClassifier() : "" );
    }

}
