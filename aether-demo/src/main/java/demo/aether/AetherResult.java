package demo.aether;

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

import java.io.File;
import java.util.List;

import org.sonatype.aether.graph.DependencyNode;

public class AetherResult
{
    private DependencyNode root;
    private List<File> resolvedFiles;
    private String resolvedClassPath;
    
    public AetherResult( DependencyNode root, List<File> resolvedFiles, String resolvedClassPath )
    {
        this.root = root;
        this.resolvedFiles = resolvedFiles;
        this.resolvedClassPath = resolvedClassPath;
    }

    public DependencyNode getRoot()
    {
        return root;
    }

    public List<File> getResolvedFiles()
    {
        return resolvedFiles;
    }

    public String getResolvedClassPath()
    {
        return resolvedClassPath;
    }
}
