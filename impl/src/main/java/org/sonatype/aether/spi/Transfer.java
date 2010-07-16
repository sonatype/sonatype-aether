package org.sonatype.aether.spi;

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
