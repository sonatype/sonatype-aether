package org.sonatype.aether.util.artifact;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

/**
 * The dependency scopes used for Java dependencies.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.graph.Dependency#getScope()
 */
public final class JavaScopes
{

    public static final String COMPILE = "compile";

    public static final String PROVIDED = "provided";

    public static final String SYSTEM = "system";

    public static final String RUNTIME = "runtime";

    public static final String TEST = "test";

    private JavaScopes()
    {
        // hide constructor
    }

}
