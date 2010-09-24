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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A simple digester for strings.
 * 
 * @author Benjamin Bentmann
 */
class SimpleDigest
{

    private MessageDigest digest;

    private long hash;

    public SimpleDigest()
    {
        try
        {
            digest = MessageDigest.getInstance( "SHA-1" );
        }
        catch ( NoSuchAlgorithmException e )
        {
            try
            {
                digest = MessageDigest.getInstance( "MD5" );
            }
            catch ( NoSuchAlgorithmException ne )
            {
                digest = null;
                hash = 13;
            }
        }
    }

    public void update( String data )
    {
        if ( data == null )
        {
            return;
        }
        if ( digest != null )
        {
            try
            {
                digest.update( data.getBytes( "UTF-8" ) );
            }
            catch ( UnsupportedEncodingException e )
            {
                // broken JVM
            }
        }
        else
        {
            hash = hash * 31 + data.hashCode();
        }
    }

    public String digest()
    {
        if ( digest != null )
        {
            StringBuilder buffer = new StringBuilder( 64 );

            byte[] bytes = digest.digest();
            for ( int i = 0; i < bytes.length; i++ )
            {
                int b = bytes[i] & 0xFF;

                if ( b < 0x10 )
                {
                    buffer.append( '0' );
                }

                buffer.append( Integer.toHexString( b ) );
            }

            return buffer.toString();
        }
        else
        {
            return Long.toHexString( hash );
        }
    }

}
