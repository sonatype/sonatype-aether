package org.sonatype.aether.test.util;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil
{

    public static File createTempFile( byte[] pattern, int repeat )
        throws IOException
    {
    
        File tmpFile = null;
        FileOutputStream out = null;
        try
        {
            tmpFile = File.createTempFile( "aether-test-util-", ".data" );
            tmpFile.deleteOnExit();
    
            out = new FileOutputStream( tmpFile );
            for ( int i = 0; i < repeat; i++ )
            {
                out.write( pattern );
            }
        }
        finally
        {
            if ( out != null )
            {
                out.close();
            }
        }
    
        return tmpFile;
    }

}
