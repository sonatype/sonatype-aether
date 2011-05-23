package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeployResult;
import org.sonatype.aether.deployment.DeploymentException;

/**
 * @author Benjamin Bentmann
 */
public interface Deployer
{

    /**
     * Uploads a collection of artifacts and their accompanying metadata to a remote repository.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The deployment request, must not be {@code null}.
     * @return The deployment result, never {@code null}.
     * @throws DeploymentException If any artifact/metadata from the request could not be deployed.
     * @see RepositorySystem#deploy(RepositorySystemSession, DeployRequest)
     */
    DeployResult deploy( RepositorySystemSession session, DeployRequest request )
        throws DeploymentException;

}
