package org.sonatype.aether.spi.connector;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 * An artifact/metadata transfer.
 * 
 * @author Benjamin Bentmann
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
        if ( state == null )
        {
            throw new IllegalArgumentException( "no transfer state specified" );
        }
        this.state = state;
        return this;
    }

}
