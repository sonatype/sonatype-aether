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

import org.junit.runner.RunWith;
import org.sonatype.tests.http.runner.junit.ConfigurationRunner;
import org.sonatype.tests.http.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
public class StutteringGetTest
extends GetTest
{

    @Override
    public void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        provide.setLatency( 100 );
    }

}
