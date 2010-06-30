package org.sonatype.maven.repository;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A snapshot handler that treats versions with the prefix "SNAPSHOT" as snapshots and expands them into a combination
 * of timestamp and build number.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultSnapshotHandler
    implements SnapshotHandler
{

    private static final String SNAPSHOT = "SNAPSHOT";

    private static final Pattern SNAPSHOT_TIMESTAMP = Pattern.compile( "^(.*-)?([0-9]{8}.[0-9]{6}-[0-9]+)$" );

    public static final DefaultSnapshotHandler INSTANCE = new DefaultSnapshotHandler();

    private DefaultSnapshotHandler()
    {
        // hide constructor
    }

    public boolean isSnapshot( String version )
    {
        return version.endsWith( SNAPSHOT ) || SNAPSHOT_TIMESTAMP.matcher( version ).matches();
    }

    public String toBaseVersion( String version )
    {
        String baseVersion;

        if ( version == null )
        {
            baseVersion = null;
        }
        else if ( version.startsWith( "[" ) || version.startsWith( "(" ) )
        {
            baseVersion = "";
        }
        else
        {
            Matcher m = SNAPSHOT_TIMESTAMP.matcher( version );
            if ( m.matches() )
            {
                if ( m.group( 1 ) != null )
                {
                    baseVersion = m.group( 1 ) + SNAPSHOT;
                }
                else
                {
                    baseVersion = SNAPSHOT;
                }
            }
            else
            {
                baseVersion = version;
            }
        }

        return baseVersion;
    }

    public String toFullVersion( String version, String timestamp, int buildNumber )
    {
        String fullVersion;

        if ( version.endsWith( SNAPSHOT ) )
        {
            String qualifier = timestamp + "-" + buildNumber;
            fullVersion = version.substring( 0, version.length() - SNAPSHOT.length() ) + qualifier;
        }
        else
        {
            fullVersion = version;
        }

        return fullVersion;
    }

}
