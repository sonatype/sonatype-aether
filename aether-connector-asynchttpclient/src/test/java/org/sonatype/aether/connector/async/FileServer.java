package org.sonatype.aether.connector.async;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tests.http.server.api.Behaviour;

/**
 * @author Benjamin Hanzelmann
 */
public class FileServer
    implements Behaviour
{

    private static Logger logger = LoggerFactory.getLogger( FileServer.class );

    public Map<String, Resource> db = new ConcurrentHashMap<String, Resource>();

    public void prepare( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {

    }

    public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
        throws Exception
    {
        String path = request.getPathInfo();
        logger.debug( request.getMethod() + " " + path );
        if ( "GET".equals( request.getMethod() ) )
        {
            Resource res = db.get( path );
            if ( res == null )
            {
                response.sendError( HttpServletResponse.SC_NOT_FOUND, "Not Found" );
                return false;
            }
            response.setContentLength( res.size );
            response.setContentType( "application/octet-stream" );
            copy( new GZIPInputStream( new ByteArrayInputStream( res.data ) ), response.getOutputStream() );
        }
        else if ( "PUT".equals( request.getMethod() ) )
        {
            Resource res = new Resource();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            res.size = copy( request.getInputStream(), new GZIPOutputStream( baos ) );
            res.data = baos.toByteArray();

            db.put( path, res );
        }
        return false;
    }

    private int copy( InputStream is, OutputStream os )
        throws IOException
    {
        int total = 0;

        for ( byte[] buffer = new byte[1024 * 4];; )
        {
            int read = is.read( buffer );
            if ( read < 0 )
            {
                break;
            }
            os.write( buffer, 0, read );
            os.flush();
            total += read;
        }

        is.close();
        os.close();

        return total;
    }

    static class Resource
    {

        byte[] data;

        int size;

    }

}
