package org.apache.maven.repository.internal;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        if ( checksum.regionMatches( true, 0, "MD", 0, 2 ) || checksum.regionMatches( true, 0, "SHA", 0, 3 ) )
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

    public static String calc( File dataFile, String algo )
        throws NoSuchAlgorithmException, IOException
    {
        MessageDigest digest = MessageDigest.getInstance( algo );

        FileInputStream fis = new FileInputStream( dataFile );
        try
        {
            DigestInputStream dis = new DigestInputStream( fis, digest );
            for ( byte[] buffer = new byte[1024 * 4];; )
            {
                int read = dis.read( buffer );
                if ( read < 0 )
                {
                    break;
                }
            }
        }
        finally
        {
            fis.close();
        }

        byte[] bytes = digest.digest();

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

        return buffer.toString();
    }

}
