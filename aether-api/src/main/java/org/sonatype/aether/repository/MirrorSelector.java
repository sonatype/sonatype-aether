package org.sonatype.aether.repository;

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

/**
 * Selects a mirror for a given remote repository.
 * 
 * @author Benjamin Bentmann
 */
public interface MirrorSelector
{

    /**
     * Selects a mirror for the specified repository.
     * 
     * @param repository The repository to select a mirror for, must not be {@code null}.
     * @return The selected mirror or {@code null} if none.
     */
    RemoteRepository getMirror( RemoteRepository repository );

}
