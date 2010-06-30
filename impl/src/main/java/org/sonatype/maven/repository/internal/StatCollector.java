package org.sonatype.maven.repository.internal;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.DependencyVisitor;

class StatCollector
    implements DependencyVisitor
{

    int maxDepth;

    int totalNodes;

    Set<String> uniqueArtifacts = new HashSet<String>();

    Set<String> uniqueDeps = new HashSet<String>();

    public boolean visitEnter( DependencyNode node )
    {
        maxDepth = Math.max( maxDepth, node.getDepth() );

        totalNodes++;

        if ( node.getDependency() != null )
        {
            uniqueArtifacts.add( getKey( node.getDependency().getArtifact() ) );
            uniqueDeps.add( getHash( node ) );
        }

        return true;
    }

    private String getHash( DependencyNode n )
    {
        String hash = getKey(n.getDependency().getArtifact());
        hash += n.getDependency().getScope();
        hash += n.getDependency().isOptional();
        hash += n.getDependency().getExclusions().size();
        hash += n.getContext();
        hash += n.getPremanagedScope();
        hash += n.getPremanagedVersion();
        hash += n.getAliases().hashCode();
        hash += n.getRelocations().hashCode();
        hash += n.getRepositories().hashCode();

        return hash;
    }

    private String getKey( Artifact a )
    {
        return a.getGroupId() + ':' + a.getArtifactId() + ':' + a.getVersion() + ':' + a.getClassifier() + ':'
            + a.getExtension();
    }

    public boolean visitLeave( DependencyNode node )
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "depth = " + maxDepth + ", nodes = " + totalNodes + ", artifacts = " + uniqueArtifacts.size() + " ("
            + ( uniqueArtifacts.size() * 100 / totalNodes ) + "%), dependencies = " + uniqueDeps.size();
    }

}
