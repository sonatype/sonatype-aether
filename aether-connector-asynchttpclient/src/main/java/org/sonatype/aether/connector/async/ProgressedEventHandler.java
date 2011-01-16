package org.sonatype.aether.connector.async;

import java.nio.ByteBuffer;

import org.sonatype.aether.transfer.TransferCancelledException;

/**
 * @author Benjamin Hanzelmann
 */
interface ProgressedEventHandler
{

    public abstract void fireTransferProgressed( final ByteBuffer buffer )
        throws TransferCancelledException;

    public abstract long getTransferredBytes();

}