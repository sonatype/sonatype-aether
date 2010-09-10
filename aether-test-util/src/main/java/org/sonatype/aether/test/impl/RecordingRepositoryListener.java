package org.sonatype.aether.test.impl;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositoryListener;

public class RecordingRepositoryListener
    implements RepositoryListener
{

    public enum Type
    {
        METADATA_DEPLOYED,
        METADATA_DEPLOYING,
        ARTIFACT_DEPLOYED,
        ARTIFACT_DEPLOYING,
        METADATA_INSTALLED,
        METADATA_INSTALLING,
        ARTIFACT_INSTALLED,
        ARTIFACT_INSTALLING,
        METADATA_RESOLVED,
        METADATA_RESOLVING,
        ARTIFACT_RESOLVED,
        ARTIFACT_RESOLVING,
        METADATA_INVALID,
        ARTIFACT_DESCRIPTOR_MISSING,
        ARTIFACT_DESCRIPTOR_INVALID
    }

    private List<EventWrapper> events = Collections.synchronizedList( new ArrayList<EventWrapper>() );

    private RepositoryListener realListener;

    public RecordingRepositoryListener()
    {
        this( null );
    }

    public RecordingRepositoryListener( RepositoryListener listener )
    {
        this.realListener = listener;
    }

    public List<EventWrapper> getEvents()
    {
        return events;
    }

    public void clear()
    {
        events.clear();
    }

    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_DESCRIPTOR_INVALID;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactDescriptorInvalid( event );
        }

    }

    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_DESCRIPTOR_MISSING;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactDescriptorMissing( event );
        }

    }

    public void metadataInvalid( RepositoryEvent event )
    {
        Type type = Type.METADATA_INVALID;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.metadataInvalid( event );
        }

    }

    public void artifactResolving( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_RESOLVING;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactResolving( event );
        }

    }

    public void artifactResolved( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_RESOLVED;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactResolved( event );
        }

    }

    public void metadataResolving( RepositoryEvent event )
    {
        Type type = Type.METADATA_RESOLVING;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.metadataResolving( event );
        }

    }

    public void metadataResolved( RepositoryEvent event )
    {
        Type type = Type.METADATA_RESOLVED;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.metadataResolved( event );
        }

    }

    public void artifactInstalling( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_INSTALLING;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactInstalling( event );
        }

    }

    public void artifactInstalled( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_INSTALLED;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactInstalled( event );
        }

    }

    public void metadataInstalling( RepositoryEvent event )
    {
        Type type = Type.METADATA_INSTALLING;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.metadataInstalling( event );
        }

    }

    public void metadataInstalled( RepositoryEvent event )
    {
        Type type = Type.METADATA_INSTALLED;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.metadataInstalled( event );
        }

    }

    public void artifactDeploying( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_DEPLOYING;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactDeploying( event );
        }

    }

    public void artifactDeployed( RepositoryEvent event )
    {
        Type type = Type.ARTIFACT_DEPLOYED;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.artifactDeployed( event );
        }

    }

    public void metadataDeploying( RepositoryEvent event )
    {
        Type type = Type.METADATA_DEPLOYING;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.metadataDeploying( event );
        }

    }

    public void metadataDeployed( RepositoryEvent event )
    {
        Type type = Type.METADATA_DEPLOYED;
        this.events.add( new EventWrapper( type, event ) );
        if ( realListener != null )
        {
            realListener.metadataDeployed( event );
        }

    }

    public class EventWrapper
    {
        Type type;

        RepositoryEvent event;

        public EventWrapper( Type type, RepositoryEvent event )
        {
            super();
            this.type = type;
            this.event = event;
        }

        public Type getType()
        {
            return type;
        }

        public RepositoryEvent getEvent()
        {
            return event;
        }
    }
}
