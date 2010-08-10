package org.sonatype.aether.impl.internal;

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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.DeployRequest;
import org.sonatype.aether.InstallRequest;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.MetadataGenerator;
import org.sonatype.aether.impl.MetadataGeneratorFactory;

/**
 * @author Benjamin Bentmann
 */
@Component( role = MetadataGeneratorFactory.class, hint = "versions" )
public class VersionsMetadataGeneratorFactory
    implements MetadataGeneratorFactory
{

    public MetadataGenerator newInstance( RepositorySystemSession session, InstallRequest request )
    {
        return new VersionsMetadataGenerator( session, request );
    }

    public MetadataGenerator newInstance( RepositorySystemSession session, DeployRequest request )
    {
        return new VersionsMetadataGenerator( session, request );
    }

    public int getPriority()
    {
        return 5;
    }

}
