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
 * @author Benjamin Bentmann
 */
public class RepositoryPolicy
{

    public static final String UPDATE_POLICY_NEVER = "never";

    public static final String UPDATE_POLICY_ALWAYS = "always";

    public static final String UPDATE_POLICY_DAILY = "daily";

    public static final String UPDATE_POLICY_INTERVAL = "interval";

    public static final String CHECKSUM_POLICY_FAIL = "fail";

    public static final String CHECKSUM_POLICY_WARN = "warn";

    public static final String CHECKSUM_POLICY_IGNORE = "ignore";

    private boolean enabled;

    private String updatePolicy;

    private String checksumPolicy;

    public RepositoryPolicy()
    {
        this( true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN );
    }

    public RepositoryPolicy( RepositoryPolicy policy )
    {
        this( policy.isEnabled(), policy.getUpdatePolicy(), policy.getChecksumPolicy() );
    }

    public RepositoryPolicy( boolean enabled, String updatePolicy, String checksumPolicy )
    {
        setEnabled( enabled );
        setUpdatePolicy( updatePolicy );
        setChecksumPolicy( checksumPolicy );
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public RepositoryPolicy setEnabled( boolean enabled )
    {
        this.enabled = enabled;

        return this;
    }

    public String getUpdatePolicy()
    {
        return updatePolicy;
    }

    public RepositoryPolicy setUpdatePolicy( String updatePolicy )
    {
        this.updatePolicy = ( updatePolicy != null ) ? updatePolicy : "";

        return this;
    }

    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public RepositoryPolicy setChecksumPolicy( String checksumPolicy )
    {
        this.checksumPolicy = ( checksumPolicy != null ) ? checksumPolicy : "";

        return this;
    }

}
