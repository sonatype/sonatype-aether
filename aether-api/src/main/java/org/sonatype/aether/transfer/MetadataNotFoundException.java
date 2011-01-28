package org.sonatype.aether.transfer;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * @author Benjamin Bentmann
 */
public class MetadataNotFoundException
    extends MetadataTransferException
{

    public MetadataNotFoundException( Metadata metadata, RemoteRepository repository )
    {
        super( metadata, repository, "Could not find metadata " + metadata + getString( " in ", repository ) );
    }

    public MetadataNotFoundException( Metadata metadata, RemoteRepository repository, String message )
    {
        super( metadata, repository, message );
    }

}
