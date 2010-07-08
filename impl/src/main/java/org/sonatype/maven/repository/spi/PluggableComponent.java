package org.sonatype.maven.repository.spi;

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

import java.util.Comparator;

/**
 * A component that can be plugged into the repository system.
 * 
 * @author Benjamin Bentmann
 */
public interface PluggableComponent
{

    /**
     * The priority of this component. Components with higher priority are preferred over those with lower priority.
     * 
     * @return The priority of this component.
     */
    int getPriority();

    /**
     * A comparator that sorts components in descending order of their priority.
     */
    static final Comparator<PluggableComponent> COMPARATOR = new Comparator<PluggableComponent>()
    {

        public int compare( PluggableComponent o1, PluggableComponent o2 )
        {
            return o2.getPriority() - o1.getPriority();
        }

    };

}
