package demo.aether;

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
