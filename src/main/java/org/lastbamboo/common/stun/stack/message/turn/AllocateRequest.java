package org.lastbamboo.common.stun.stack.message.turn;

import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.stun.stack.message.AbstractStunMessage;
import org.lastbamboo.common.stun.stack.message.StunMessageType;
import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
import org.lastbamboo.common.stun.stack.message.VisitableStunMessage;

/**
 * Allocate request message.  This can create an allocate request from scratch
 * or from network data.
 */
public final class AllocateRequest extends AbstractStunMessage
    implements VisitableStunMessage
    {

    /**
     * Creates a new request to allocate a TURN client mapping.  It uses the
     * given attribute factory for creating message attributes.
     */
    public AllocateRequest()
        {
        super(UUID.randomUUID(), StunMessageType.ALLOCATE_REQUEST);
        }

    /**
     * Creates a new request from the network with the given transaction ID 
     * and factory for creating attributes.
     * 
     * @param id The ID of the request.
     */
    public AllocateRequest(final UUID id)
        {
        super(id, StunMessageType.ALLOCATE_REQUEST); 
        }

    public void accept(final StunMessageVisitor visitor)
        {
        visitor.visitAllocateRequest(this);
        }

    }
