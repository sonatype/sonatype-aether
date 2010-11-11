package org.sonatype.aether.connector.async;

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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;
import org.sonatype.tests.jetty.runner.ConfigurationRunner.Configurators;
import org.sonatype.tests.jetty.server.configurations.HttpProxyAuthConfigurator;
import org.sonatype.tests.jetty.server.configurations.HttpProxyConfigurator;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
@Configurators( { HttpProxyConfigurator.class, HttpProxyAuthConfigurator.class } )
public class ProxyPutTest
    extends PutTest
{

    @Override
    @Before
    public void before()
        throws Exception
    {
        super.before();

        Authentication auth = new Authentication( "puser", "password" );
        Proxy proxy = new Proxy( "http", "localhost", provider().getPort(), auth );
        repository().setProxy( proxy );
    }

    @Override
    public String url()
    {
        URL orig;
        try
        {
            orig = new URL( super.url() );
            return new URL( orig.getProtocol(), "proxiedhost", orig.getPort(), "" ).toString();
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
            throw new IllegalStateException( e.getMessage(), e );
        }
    }

}
