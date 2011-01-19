package org.sonatype.aether.connector.async;

import org.sonatype.aether.repository.RemoteRepository;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class DavUrlGetTest
    extends GetTest
{

    @Override
    protected RemoteRepository repository()
    {
        RemoteRepository repo = super.repository();
        return repo.setUrl( "dav:" + repo.getUrl() );
    }

}
