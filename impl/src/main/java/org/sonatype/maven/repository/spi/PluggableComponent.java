package org.sonatype.maven.repository.spi;

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
