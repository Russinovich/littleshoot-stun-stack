package org.lastbamboo.common.stun.stack.transaction;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
import org.lastbamboo.common.stun.stack.message.BindingRequest;
import org.lastbamboo.common.stun.stack.message.CanceledStunMessage;
import org.lastbamboo.common.stun.stack.message.IcmpErrorStunMessage;
import org.lastbamboo.common.stun.stack.message.NullStunMessage;
import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
import org.lastbamboo.common.stun.stack.message.StunMessage;
import org.lastbamboo.common.stun.stack.message.turn.AllocateErrorResponse;
import org.lastbamboo.common.stun.stack.message.turn.AllocateRequest;
import org.lastbamboo.common.stun.stack.message.turn.ConnectRequest;
import org.lastbamboo.common.stun.stack.message.turn.ConnectionStatusIndication;
import org.lastbamboo.common.stun.stack.message.turn.DataIndication;
import org.lastbamboo.common.stun.stack.message.turn.SendIndication;
import org.lastbamboo.common.stun.stack.message.turn.AllocateSuccessResponse;
import org.lastbamboo.common.util.Closure;
import org.lastbamboo.common.util.CollectionUtils;
import org.lastbamboo.common.util.CollectionUtilsImpl;

/**
 * Implementation of a STUN client transaction.
 */
public class StunClientTransactionImpl 
    implements StunClientTransaction<StunMessage>
    {
    
    private static final Log LOG = 
        LogFactory.getLog(StunClientTransactionImpl.class);
    
    private final StunMessage m_request;

    private long m_transactionTime = Long.MAX_VALUE;

    /**
     * The listeners for a transaction.  We must lock this whenever 
     * it's accessed.
     */
    private List<StunTransactionListener> m_transactionListeners;

    private final long m_transactionStartTime;

    private final InetSocketAddress m_remoteAddress;

    /**
     * Creates a new STUN client transaction.
     * 
     * @param request The request starting the transaction.
     * @param transactionListeners The listeners for transaction events.
     * @param remoteAddress The remote address for the transaction.
     */
    public StunClientTransactionImpl(final StunMessage request, 
        final List<StunTransactionListener> transactionListeners, 
        final InetSocketAddress remoteAddress)
        {
        this.m_request = request;
        this.m_transactionListeners = 
            Collections.synchronizedList(transactionListeners);
        this.m_remoteAddress = remoteAddress;
        this.m_transactionStartTime = System.currentTimeMillis();
        }
    
    /**
     * Creates a new STUN client transaction.
     * 
     * @param request The request starting the transaction.
     * @param transactionListener The listener for transaction events.
     * @param remoteAddress The remote address for the transaction.
     */
    public StunClientTransactionImpl(final StunMessage request, 
        final StunTransactionListener transactionListener, 
        final InetSocketAddress remoteAddress)
        {
        this.m_request = request;
        final List<StunTransactionListener> listeners = 
            new LinkedList<StunTransactionListener>();
        listeners.add(transactionListener);
        this.m_transactionListeners = 
            Collections.synchronizedList(listeners);
        this.m_remoteAddress = remoteAddress;
        this.m_transactionStartTime = System.currentTimeMillis();
        }
    
    public void addListener(final StunTransactionListener listener)
        {
        this.m_transactionListeners.add(listener);
        }

    public StunMessage getRequest()
        {
        return this.m_request;
        }
    
    public long getTransactionTime()
        {
        return m_transactionTime;
        }
    
    public InetSocketAddress getIntendedDestination()
        {
        return this.m_remoteAddress;
        }
    
    public StunMessage visitBindingSuccessResponse(
        final BindingSuccessResponse response)
        {
        LOG.debug("Received success response");
        final Closure<StunTransactionListener> success =
            new Closure<StunTransactionListener>()
            {
            public void execute(final StunTransactionListener listener)
                {
                listener.onTransactionSucceeded(m_request, response);
                }
            };
        return notifyListeners(response, success);
        }
    
    public StunMessage visitBindingErrorResponse(
        final BindingErrorResponse response)
        {
        return notifyFailure(response);
        }
    
    public StunMessage visitIcmpErrorMesssage(
        final IcmpErrorStunMessage message)
        {
        return notifyFailure(message);
        }
    
    private StunMessage notifyFailure(final StunMessage message)
        {
        final Closure<StunTransactionListener> error =
            new Closure<StunTransactionListener>()
            {
            public void execute(final StunTransactionListener listener)
                {
                listener.onTransactionFailed(m_request, message);
                }
            };
        return notifyListeners(message, error);
        }

    private StunMessage notifyListeners(final StunMessage response, 
        final Closure<StunTransactionListener> closure)
        {
        if (isSameTransaction(response))
            {
            final CollectionUtils utils = new CollectionUtilsImpl();

            LOG.debug("About to notify listeners...");
            utils.forAllDoSynchronized(this.m_transactionListeners, closure);
            return response;
            }
        else
            {
            LOG.warn("Received response from different transaction.");
            return new NullStunMessage();
            }
        }

    private boolean isSameTransaction(final StunMessage response)
        {
        if (!this.m_request.getTransactionId().equals(
            response.getTransactionId()))
            {
            LOG.error("Unexpected transaction ID.  Expected " + 
                this.m_request.getTransactionId() +
                " but was "+response.getTransactionId());
            return false;
            }
        else
            {
            setTransactionTime();
            return true;
            }
        }

    private void setTransactionTime()
        {
        this.m_transactionTime = 
            System.currentTimeMillis() - this.m_transactionStartTime;
        }

    public StunMessage visitBindingRequest(final BindingRequest binding)
        {
        return null;
        }
    
    public StunMessage visitAllocateRequest(final AllocateRequest request)
        {
        return null;
        }

    public StunMessage visitDataIndication(final DataIndication data)
        {
        return null;
        }

    public StunMessage visitSendIndication(final SendIndication request)
        {
        return null;
        }

    public StunMessage visitAllocateSuccessResponse(
        final AllocateSuccessResponse response)
        {
        return null;
        }
    
    public StunMessage visitAllocateErrorResponse(
        final AllocateErrorResponse response)
        {
        return null;
        }

    public StunMessage visitConnectRequest(final ConnectRequest request)
        {
        LOG.error("Client received connect request");
        return null;
        }

    public StunMessage visitConnectionStatusIndication(
        final ConnectionStatusIndication indication)
        {
        return null;
        }

    public StunMessage visitNullMessage(final NullStunMessage message)
        {
        return message;
        }

    public StunMessage visitCanceledMessage(final CanceledStunMessage message)
        {
        return message;
        }
    }
