package org.sonatype.aether.impl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author Benjamin Bentmann
 */
public class ChecksumUtils
{

    public static String read( File checksumFile )
        throws IOException
    {
        String checksum = FileUtils.fileRead( checksumFile, "UTF-8" );

        // remove whitespaces at the end
        checksum = checksum.trim();

        // check for 'ALGO (name) = CHECKSUM' like used by openssl
        if ( checksum.matches( ".+= [0-9A-Fa-f]+" ) )
        {
            int lastSpacePos = checksum.lastIndexOf( ' ' );
            checksum = checksum.substring( lastSpacePos + 1 );
        }
        else
        {
            // remove everything after the first space (if available)
            int spacePos = checksum.indexOf( ' ' );

            if ( spacePos != -1 )
            {
                checksum = checksum.substring( 0, spacePos );
            }
        }

        return checksum;
    }

    public static Map<String, Object> calc( File dataFile, Collection<String> algos )
        throws IOException
    {
        Map<String, Object> results = new LinkedHashMap<String, Object>();

        Map<String, MessageDigest> digests = new LinkedHashMap<String, MessageDigest>();
        for ( String algo : algos )
        {
            try
            {
                digests.put( algo, MessageDigest.getInstance( algo ) );
            }
            catch ( NoSuchAlgorithmException e )
            {
                results.put( algo, e );
            }
        }

        FileInputStream fis = new FileInputStream( dataFile );
        try
        {
            BufferedInputStream bis = new BufferedInputStream( fis );
            try
            {
                for ( byte[] buffer = new byte[1024 * 4];; )
                {
                    int read = bis.read( buffer );
                    if ( read < 0 )
                    {
                        break;
                    }
                    for ( MessageDigest digest : digests.values() )
                    {
                        digest.update( buffer, 0, read );
                    }
                }
            }
            finally
            {
                bis.close();
            }
        }
        finally
        {
            fis.close();
        }

        for ( Map.Entry<String, MessageDigest> entry : digests.entrySet() )
        {
            byte[] bytes = entry.getValue().digest();

            StringBuilder buffer = new StringBuilder( 64 );

            for ( int i = 0; i < bytes.length; i++ )
            {
                int b = bytes[i] & 0xFF;
                if ( b < 0x10 )
                {
                    buffer.append( '0' );
                }
                buffer.append( Integer.toHexString( b ) );
            }

            results.put( entry.getKey(), buffer.toString() );
        }

        return results;
    }

}
