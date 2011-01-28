package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TransferListeners which computes MD5 checksum on the fly when files are transfered.
 * <p/>
 * Highly inspired from Wagon impl.
 *
 * @author Jeanfrancois Arcand
 */
class ChecksumTransferListener
    implements TransferListener
{
    private MessageDigest digester = null;

    private String actualChecksum;

    public ChecksumTransferListener()
        throws NoSuchAlgorithmException
    {
        this( "MD5" );
    }

    /**
     * @param algorithm One of the algorithms supported by JDK: MD5, MD2 or SHA-1
     */
    public ChecksumTransferListener( String algorithm )
        throws NoSuchAlgorithmException
    {
        digester = MessageDigest.getInstance( algorithm );
    }

    public void transferInitiated( TransferEvent transferEvent )
    {
        // This space left intentionally blank
    }


    public void transferStarted( TransferEvent transferEvent )
    {
        actualChecksum = null;

        digester.reset();
    }

    public void transferProgressed( TransferEvent event )
        throws TransferCancelledException
    {
        digester.update( event.getDataBuffer() );
    }

    public void transferCorrupted( TransferEvent event )
        throws TransferCancelledException
    {
        digester.reset();

        actualChecksum = null;
    }

    public void transferSucceeded( TransferEvent event )
    {
        actualChecksum = encode( digester.digest() );
    }

    public void transferFailed( TransferEvent event )
    {
        digester.reset();

        actualChecksum = null;
    }

    public void debug( String message )
    {
        // left intentionally blank
    }

    /**
     * Returns md5 checksum which was computed during transfer
     *
     * @return
     */
    public String getActualChecksum()
    {
        return actualChecksum;
    }

    /**
     * Encodes a 128 bit or 160-bit byte array into a String.
     *
     * @param binaryData Array containing the digest
     * @return Encoded hex string, or null if encoding failed
     */
    private String encode( byte[] binaryData )
    {
        if ( binaryData.length != 16 && binaryData.length != 20 )
        {
            int bitLength = binaryData.length * 8;
            throw new IllegalArgumentException( "Unrecognised length for binary data: " + bitLength + " bits" );
        }

        String retValue = "";

        for ( int i = 0; i < binaryData.length; i++ )
        {
            String t = Integer.toHexString( binaryData[i] & 0xff );

            if ( t.length() == 1 )
            {
                retValue += ( "0" + t );
            }
            else
            {
                retValue += t;
            }
        }

        return retValue.trim();
    }

}
