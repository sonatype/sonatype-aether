package org.apache.maven.repository;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * A policy controlling access to a repository.
 * 
 * @author Benjamin Bentmann
 */
public class RepositoryPolicy
{

    /**
     * Never update locally cached data.
     */
    public static final String UPDATE_POLICY_NEVER = "never";

    /**
     * Always update locally cached data.
     */
    public static final String UPDATE_POLICY_ALWAYS = "always";

    /**
     * Update locally cached data once a day.
     */
    public static final String UPDATE_POLICY_DAILY = "daily";

    /**
     * Update locally cached data every X minutes as given by "interval:X".
     */
    public static final String UPDATE_POLICY_INTERVAL = "interval";

    /**
     * Verify checksums and fail the resolution if they do not match.
     */
    public static final String CHECKSUM_POLICY_FAIL = "fail";

    /**
     * Verify checksums and warn if they do not match.
     */
    public static final String CHECKSUM_POLICY_WARN = "warn";

    /**
     * Do not verify checksums.
     */
    public static final String CHECKSUM_POLICY_IGNORE = "ignore";

    private boolean enabled;

    private String updatePolicy;

    private String checksumPolicy;

    /**
     * Creates a new policy with checksum warnings and daily update checks.
     */
    public RepositoryPolicy()
    {
        this( true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN );
    }

    /**
     * Creates a copy of the specified policy.
     * 
     * @param policy The policy to copy, must not be {@code null}.
     */
    public RepositoryPolicy( RepositoryPolicy policy )
    {
        this( policy.isEnabled(), policy.getUpdatePolicy(), policy.getChecksumPolicy() );
    }

    /**
     * Creates a new policy with the specified settings.
     * 
     * @param enabled A flag whether the associated repository should be accessed or not.
     * @param updatePolicy The update interval after which locally cached data from the repository is considered stale
     *            and should be refetched, may be {@code null}.
     * @param checksumPolicy The way checksum verification should be handled, may be {@code null}.
     */
    public RepositoryPolicy( boolean enabled, String updatePolicy, String checksumPolicy )
    {
        setEnabled( enabled );
        setUpdatePolicy( updatePolicy );
        setChecksumPolicy( checksumPolicy );
    }

    /**
     * Indicates whether the associated repository should be contacted or not.
     * 
     * @return {@code true} if the repository should be contacted, {@code false} otherwise.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Sets the enabled flag for the associated repository.
     * 
     * @param enabled {@code true} if the repository should be contacted, {@code false} otherwise.
     * @return This policy for chaining, never {@code null}.
     */
    public RepositoryPolicy setEnabled( boolean enabled )
    {
        this.enabled = enabled;

        return this;
    }

    /**
     * Gets the update policy for locally cached data from the repository.
     * 
     * @return The update policy, never {@code null}.
     */
    public String getUpdatePolicy()
    {
        return updatePolicy;
    }

    /**
     * Sets the update policy for locally cached data from the repository. Well-known policies are
     * {@link #UPDATE_POLICY_NEVER}, {@link #UPDATE_POLICY_ALWAYS}, {@link #UPDATE_POLICY_DAILY} and
     * {@link #UPDATE_POLICY_INTERVAL}
     * 
     * @param updatePolicy The update policy, may be {@code null}.
     * @return This policy for chaining, never {@code null}.
     */
    public RepositoryPolicy setUpdatePolicy( String updatePolicy )
    {
        this.updatePolicy = ( updatePolicy != null ) ? updatePolicy : "";

        return this;
    }

    /**
     * Gets the policy for checksum validation.
     * 
     * @return The checksum policy, never {@code null}.
     */
    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    /**
     * Sets the policy for checksum validation. Well-known policies are {@link #CHECKSUM_POLICY_FAIL},
     * {@link #CHECKSUM_POLICY_WARN} and {@link #CHECKSUM_POLICY_IGNORE}.
     * 
     * @param checksumPolicy The checksum policy, may be {@code null}.
     * @return This policy for chaining, never {@code null}.
     */
    public RepositoryPolicy setChecksumPolicy( String checksumPolicy )
    {
        this.checksumPolicy = ( checksumPolicy != null ) ? checksumPolicy : "";

        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "enabled=" ).append( isEnabled() );
        buffer.append( ", checksums=" ).append( getChecksumPolicy() );
        buffer.append( ", updates=" ).append( getUpdatePolicy() );
        return buffer.toString();
    }

}
