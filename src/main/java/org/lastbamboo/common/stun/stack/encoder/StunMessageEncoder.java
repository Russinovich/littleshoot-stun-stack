package org.lastbamboo.common.stun.stack.encoder;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.id.uuid.UUID;
import org.apache.mina.common.ByteBuffer;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageType;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttribute;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttributeType;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttributeVisitor;
import org.lastbamboo.common.util.mina.MinaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes bytes into STUN messages.  This is separate from the MINA
 * protocol encoder for easier testing.
 */
public class StunMessageEncoder
    {

    private final Logger LOG = 
        LoggerFactory.getLogger(StunMessageEncoder.class);
    
    /**
     * Encodes a {@link StunMessage} into a {@link ByteBuffer}.
     * 
     * @param stunMessage The STUN message to encode.
     * @return The message encoded in a {@link ByteBuffer} ready for writing
     * (flipped).
     */
    public ByteBuffer encode(final StunMessage stunMessage) 
        {
        
        final int length = stunMessage.getTotalLength();
        final ByteBuffer buf = ByteBuffer.allocate(length);
        
        if (LOG.isDebugEnabled())
            {
            LOG.debug("Total message length: "+length+" for STUN message: "+stunMessage);
            }
        final StunMessageType type = stunMessage.getType();
        MinaUtils.putUnsignedShort(buf, type.toInt());
        MinaUtils.putUnsignedShort(buf, stunMessage.getBodyLength());
        
        final UUID transactionId = stunMessage.getTransactionId();

        buf.put(transactionId.getRawBytes());
        
        final Map<StunAttributeType, StunAttribute> attributes = 
            stunMessage.getAttributes();
        
        putAttributes(attributes, buf);
        
        buf.flip();
        if (LOG.isDebugEnabled())
            {
            LOG.debug("Successfully encoded message!!");
            }
        return buf;
        }

    private void putAttributes(
        final Map<StunAttributeType, StunAttribute> attributesMap, 
        final ByteBuffer buf)
        {
        final StunAttributeVisitor visitor = new StunAttributeEncoder(buf);
        final Collection<StunAttribute> attributes = attributesMap.values();
        for (final StunAttribute attribute : attributes)
            {
            attribute.accept(visitor);
            }
        }

    }
