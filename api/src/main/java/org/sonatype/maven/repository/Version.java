package org.sonatype.maven.repository;

/**
 * A parsed artifact version.
 * 
 * @author Benjamin Bentmann
 */
public interface Version
    extends Comparable<Version>
{

    /**
     * Gets the original string representation of the version.
     * 
     * @return The string representation of the version.
     */
    String toString();

}
