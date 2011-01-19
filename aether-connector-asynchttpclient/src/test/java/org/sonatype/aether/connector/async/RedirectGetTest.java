package org.sonatype.aether.connector.async;

import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.behaviour.Redirect;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class RedirectGetTest
    extends GetTest
{

    @Override
    protected RemoteRepository repository()
    {
        return super.repository().setUrl( url( "redirect" ) );
    }

    @Override
    public void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        provider().addBehaviour( "/redirect/*", new Redirect( "^", "/repo" ) );
    }

}
