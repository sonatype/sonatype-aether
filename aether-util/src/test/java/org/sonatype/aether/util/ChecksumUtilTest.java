package org.sonatype.aether.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class ChecksumUtilTest
{
    private static final String PREFIX = "src/test/resources/ChecksumUtilTest";

    /**
     * Generate checksum. Usage: main $datafile $checksumbasename
     */
    public static void main( String[] args )
        throws Throwable
    {

        Map<String, Object> checksums;

        File file = new File( args[0] );
        String targetBasename = args[1];

        checksums = ChecksumUtils.calc( file, Arrays.asList( "SHA-1", "MD5" ) );

        for ( Entry<String, Object> entry : checksums.entrySet() )
        {
            File target = new File( targetBasename + "." + entry.getKey() );
            FileWriter w = new FileWriter( target );
            if ( entry.getValue() instanceof Throwable )
            {
                throw (Throwable) entry.getValue();
            }

            w.write( entry.getValue().toString() );
            w.close();
        }
    }

    @Test
    public void testEquality()
        throws Throwable
    {
        Map<String, Object> checksums = null;

        File[] list = new File( PREFIX, "data" ).listFiles();
        for ( File file : list )
        {
            checksums = ChecksumUtils.calc( file, Arrays.asList( "SHA-1", "MD5" ) );

            File checksumDir = new File( PREFIX, "checksums" );
            for ( Entry<String, Object> entry : checksums.entrySet() )
            {
                if ( entry.getValue() instanceof Throwable )
                {
                    throw (Throwable) entry.getValue();
                }
                String actual = entry.getValue().toString();
                String expected = ChecksumUtils.read( new File( checksumDir, file.getName() + "." + entry.getKey() ) );
                assertEquals( String.format("checksums do not match for '%s', algorithm '%s'", file.getName(), entry.getKey()), expected, actual );
            }

        }

    }
}
