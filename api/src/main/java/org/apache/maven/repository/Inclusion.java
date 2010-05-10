package org.apache.maven.repository;

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
