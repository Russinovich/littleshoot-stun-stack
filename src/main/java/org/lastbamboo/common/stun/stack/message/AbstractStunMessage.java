package org.lastbamboo.common.stun.stack.message;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.stun.stack.message.attributes.StunAttribute;

/**
 * Abstracts out common methods and data of STUN messages.
 */
public abstract class AbstractStunMessage implements StunMessage,
    VisitableStunMessage
    {

    private static final Map<Integer, StunAttribute> EMPTY_MAP =
        Collections.emptyMap();
    
    private final UUID m_transactionId;
    private final Map<Integer, StunAttribute> m_attributes;

    private final int m_totalLength;

    private final int m_bodyLength;

    private final int m_messageType;

    /**
     * Creates a new STUN message.
     * 
     * @param transactionId The transaction ID.
     * @param messageType The type of message.
     */
    public AbstractStunMessage(final UUID transactionId,
        final int messageType)
        {
        this(transactionId, messageType, EMPTY_MAP);
        }
    
    /**
     * Creates a new STUN message.
     * 
     * @param transactionId The transaction ID.
     * @param attributes The message attributes.
     * @param messageType The type of the message.
     */
    public AbstractStunMessage(final UUID transactionId, final int messageType,
        final Map<Integer, StunAttribute> attributes)
        {
        m_transactionId = transactionId;
        m_attributes = attributes;
        m_bodyLength = calculateBodyLength(attributes);
        m_totalLength = m_bodyLength + 20;
        m_messageType = messageType;
        }

    private int calculateBodyLength(
        final Map<Integer, StunAttribute> attributesMap)
        {
        final Collection<StunAttribute> attributes = attributesMap.values();
        int length = 0;
        for (final StunAttribute attribute : attributes)
            {
            length += attribute.getTotalLength();
            }
        return length;
        }

    public UUID getTransactionId()
        {
        return this.m_transactionId;
        }

    public int getTotalLength()
        {
        return this.m_totalLength;
        }

    public Map<Integer, StunAttribute> getAttributes()
        {
        return m_attributes;
        }
    
    public int getBodyLength()
        {
        return this.m_bodyLength;
        }
    
    public int getType()
        {
        return this.m_messageType;
        }
    
    public StunAttribute getAttribute(final int attributeType)
        {
        return this.m_attributes.get(new Integer(attributeType));
        }
    }
