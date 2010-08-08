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

import java.util.Locale;

/**
 * The dependency scopes used for Java dependencies. Be sure to invoke {@link #toString()} on a constant and not
 * {@link #name()} to get the string representation.
 * 
 * @author Benjamin Bentmann
 */
public enum JavaScopes
{

    COMPILE, PROVIDED, SYSTEM, RUNTIME, TEST, IMPORT;

    /**
     * Gets the string representation of this scope constant, i.e. its lower-case form as used for dependencies.
     */
    @Override
    public String toString()
    {
        return name().toLowerCase( Locale.ENGLISH );
    };

}
