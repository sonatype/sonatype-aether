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
 * An exclusion of one or more transitive dependencies.
 * 
 * @author Benjamin Bentmann
 */
public class Exclusion
{

    private String groupId = "";

    private String artifactId = "";

    private String classifier = "";

    private String type = "";

    /**
     * Creates an empty exclusion
     */
    public Exclusion()
    {
        // enables default constructor
    }

    /**
     * Creates an exclusion for artifacts with the specified coordinates.
     * 
     * @param groupId The group identifier, may be {@code null}.
     * @param artifactId The artifact identifier, may be {@code null}.
     * @param classifier The classifier, may be {@code null}.
     * @param type The file type, may be {@code null}.
     */
    public Exclusion( String groupId, String artifactId, String classifier, String type )
    {
        setGroupId( groupId );
        setArtifactId( artifactId );
        setClassifier( classifier );
        setType( type );
    }

    /**
     * Gets the group identifier for artifacts to exclude.
     * 
     * @return The group identifier, never {@code null}.
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Sets the group identifier for artifacts to exclude.
     * 
     * @param groupId The group identifier, may be {@code null}.
     * @return This exclusion for chaining, never {@code null}.
     */
    public Exclusion setGroupId( String groupId )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        return this;
    }

    /**
     * Gets the artifact identifier for artifacts to exclude.
     * 
     * @return The artifact identifier, never {@code null}.
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Sets the artifact identifier for artifacts to exclude.
     * 
     * @param artifactId The artifact identifier, may be {@code null}.
     * @return This exclusion for chaining, never {@code null}.
     */
    public Exclusion setArtifactId( String artifactId )
    {
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        return this;
    }

    /**
     * Gets the classifier for artifacts to exclude.
     * 
     * @return The classifier, never {@code null}.
     */
    public String getClassifier()
    {
        return classifier;
    }

    /**
     * Sets the classifier for artifacts to exclude.
     * 
     * @param classifier The classifier, may be {@code null}.
     * @return This exclusion for chaining, never {@code null}.
     */
    public Exclusion setClassifier( String classifier )
    {
        this.classifier = ( classifier != null ) ? classifier : "";
        return this;
    }

    /**
     * Gets the file type for artifacts to exclude.
     * 
     * @return The file type of artifacts to exclude, never {@code null}.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the file type for artifacts to exclude.
     * 
     * @param type The file type, may be {@code null}.
     * @return This exclusion for chaining, never {@code null}.
     */
    public Exclusion setType( String type )
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
