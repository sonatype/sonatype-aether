package demo.aether;

import java.io.File;
import java.util.List;

public class AetherResult
{
    private String tree;
    private List<File> resolvedFiles;
    private String resolvedClassPath;
    
    public AetherResult( String tree, List<File> resolvedFiles, String resolvedClassPath )
    {
        this.tree = tree;
        this.resolvedFiles = resolvedFiles;
        this.resolvedClassPath = resolvedClassPath;
    }

    public String getTree()
    {
        return tree;
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
