package org.sonatype.maven.repository.spi;

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
 * An artifact/metadata transfer.
 * 
 * @author Benjamin
 */
public abstract class Transfer
{

    public enum State
    {
        /**
         * Transfer has not started yet.
         */
        NEW,

        /**
         * Transfer is in progress.
         */
        ACTIVE,

        /**
         * Transfer is over, either successfully or not.
         */
        DONE
    }

    private State state = State.NEW;

    /**
     * Gets the state of this transfer.
     * 
     * @return The state of this transfer, never {@code null}.
     */
    public State getState()
    {
        return state;
    }

    /**
     * Sets the state of this transfer.
     * 
     * @param state The new state, must not be {@code null}.
     * @return This transfer for chaining, never {@code null}.
     */
    public Transfer setState( State state )
    {
        this.state = state;
        return this;
    }

}
