package org.sonatype.aether.util;

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

public class StringUtils
{
    /**
     * <p>
     * Checks if a String is non <code>null</code> and is not empty (<code>length > 0</code>).
     * </p>
     * 
     * @param str the String to check
     * @return true if the String is non-null, and not length zero
     */
    public static boolean isNotEmpty( String str )
    {
        return ( str != null && str.length() > 0 );
    }
}
