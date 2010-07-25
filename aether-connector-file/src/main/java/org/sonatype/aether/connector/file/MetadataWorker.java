package org.sonatype.aether.connector.file;

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

import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;

public class MetadataWorker
    extends FileConnectorWorker
    implements Runnable
{

    private MetadataWorker( RepositorySystemSession session, RemoteRepository repository, Direction direction )
    {
        super( session, repository, direction );
    }

    public MetadataWorker( MetadataUpload metadataUpload, RemoteRepository repository, RepositorySystemSession session )
    {
        this( session, repository, Direction.UPLOAD);
    }

    public MetadataWorker( MetadataDownload metadataDownload, RemoteRepository repository,
                           RepositorySystemSession session )
    {
        this( session, repository, Direction.DOWNLOAD);
    }

    public void run()
    {
        // TODO Auto-generated method stub

    }

}
