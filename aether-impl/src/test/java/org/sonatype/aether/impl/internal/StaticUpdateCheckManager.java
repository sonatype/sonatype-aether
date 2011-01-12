package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.UpdateCheck;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataTransferException;

class StaticUpdateCheckManager
    implements UpdateCheckManager
{
    boolean checkRequired;

    public StaticUpdateCheckManager( boolean checkRequired )
    {
        super();
        this.checkRequired = checkRequired;
    }

    public void touchMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check )
    {
    }

    public void touchArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
    }

    public String getEffectiveUpdatePolicy( RepositorySystemSession session, String policy1, String policy2 )
    {
        return policy1;
    }

    public void checkMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check )
    {
        check.setRequired( checkRequired );
    }

    public void checkArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        check.setRequired( checkRequired );
    }

    public boolean isUpdatedRequired( RepositorySystemSession session, long lastModified, String policy )
    {
        return checkRequired;
    }

}
