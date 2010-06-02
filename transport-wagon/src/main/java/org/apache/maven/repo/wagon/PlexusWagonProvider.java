package org.apache.maven.repo.wagon;

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

import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Benjamin Bentmann
 */
@Component( role = WagonProvider.class )
public class PlexusWagonProvider
    implements WagonProvider
{

    @Requirement
    private PlexusContainer container;

    public Wagon lookup( String roleHint )
        throws Exception
    {
        return container.lookup( Wagon.class, roleHint );
    }

    public void release( Wagon wagon )
    {
        try
        {
            if ( wagon != null )
            {
                container.release( wagon );
            }
        }
        catch ( Exception e )
        {
            // too bad
        }
    }

}
