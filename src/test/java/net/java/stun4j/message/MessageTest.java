package net.java.stun4j.message;

import junit.framework.*;
import net.java.stun4j.*;
import net.java.stun4j.attribute.*;
import java.util.*;

public class MessageTest extends TestCase {
    private Message bindingRequest       = null;
    private Message bindingResponse      = null;
    private Message bindingErrorResponse = null;

    private MappedAddressAttribute  mappedAddress = null;
    private SourceAddressAttribute  sourceAddress = null;
    private ChangedAddressAttribute changedAddress = null;

    private ChangeRequestAttribute  changeRequest = null;

    private ErrorCodeAttribute         errorCodeAttribute         = null;
    private UnknownAttributesAttribute unknownAttributesAttribute = null;

    private MsgFixture msgFixture;



    protected void setUp() throws Exception {
        super.setUp();
        msgFixture = new MsgFixture(this);
        msgFixture.setUp();


        //binding request
        bindingRequest = new Request();
        bindingRequest.setMessageType(Message.BINDING_REQUEST);

        changeRequest = AttributeFactory.createChangeRequestAttribute(
                   msgFixture.CHANGE_IP_FLAG_1, msgFixture.CHANGE_PORT_FLAG_1);
        bindingRequest.addAttribute(changeRequest);
        bindingRequest.setTransactionID(msgFixture.TRANSACTION_ID);

        //binding response
        bindingResponse = new Response();
        bindingResponse.setMessageType(Message.BINDING_RESPONSE);

        mappedAddress = AttributeFactory.createMappedAddressAttribute(
                               new StunAddress( msgFixture.ADDRESS_ATTRIBUTE_ADDRESS, msgFixture.ADDRESS_ATTRIBUTE_PORT));

        bindingResponse.addAttribute(mappedAddress);

        sourceAddress = AttributeFactory.createSourceAddressAttribute(
                                new StunAddress( msgFixture.ADDRESS_ATTRIBUTE_ADDRESS_2, msgFixture.ADDRESS_ATTRIBUTE_PORT_2));

        bindingResponse.addAttribute(sourceAddress);

        changedAddress = AttributeFactory.createChangedAddressAttribute(
                                new StunAddress( msgFixture.ADDRESS_ATTRIBUTE_ADDRESS_3,msgFixture.ADDRESS_ATTRIBUTE_PORT_3));

        bindingResponse.addAttribute(changedAddress);
        bindingResponse.setTransactionID(msgFixture.TRANSACTION_ID);


    }

    protected void tearDown() throws Exception
    {
        bindingRequest = null;
        bindingResponse = null;
        bindingErrorResponse = null;
        mappedAddress = null;
        sourceAddress = null;
        changedAddress = null;
        changeRequest = null;
        errorCodeAttribute = null;
        unknownAttributesAttribute = null;
        changeRequest = null;

        msgFixture.tearDown();

        msgFixture = null;
        super.tearDown();
    }

    /**
     * Adds and gets an attribute and test that they are the same then adds a
     * another attribute (same typ different value) and veriies that the first
     * one is properly replaced.
     *
     * @throws StunException java.lang.Exception if we fail
     */

    public void testAddAndGetAttribute() throws StunException {

        Response   message = new Response();
        message.setMessageType(Message.BINDING_RESPONSE);
        message.addAttribute(mappedAddress);

        Attribute getResult = null;


        getResult = message.getAttribute(mappedAddress.getAttributeType());
        assertEquals("Originally added attribute did not match the one retrned "
                     +"by getAttribute()",
                     mappedAddress,
                     getResult);

        //do it again
        message.addAttribute(sourceAddress);

        getResult = message.getAttribute(sourceAddress.getAttributeType());


        assertEquals("The second attribute could not be extracted.",
                    sourceAddress,
                    getResult);



    }

    /**
     * Decodes a bindingRequest and then a binding response and checks whether
     * they match the corresponding objects.
     *
     * @throws StunException java.lang.Exception if we fail
     */
    public void testEncode()
        throws StunException
    {
        //Binding Request
        byte[] expectedReturn = msgFixture.bindingRequest;

        byte[] actualReturn = bindingRequest.encode();
        assertTrue("A binding request was not properly encoded",
                   Arrays.equals(  expectedReturn, actualReturn ) );

        //Binding Response
        expectedReturn = msgFixture.bindingResponse;

        actualReturn = bindingResponse.encode();

        assertTrue("A binding response was not properly encoded",
                     Arrays.equals(  expectedReturn, actualReturn ) );


    }

    /**
     * Encodes a bindingRequest and then a binding response and checks whether
     * they match the corresponding binary arrays.
     *
     * @throws StunException java.lang.Exception if we fail
     */
    public void testDecode()
        throws StunException
    {
        //Binding Request
        Message expectedReturn = bindingRequest;

        Message actualReturn = Message.decode(msgFixture.bindingRequest,
                                             (char)0,
                                             (char)msgFixture.bindingRequest.length);

        assertEquals("A binding request was not properly decoded",
                     expectedReturn, actualReturn );

        //Binding Response
        expectedReturn = bindingResponse;

        actualReturn = Message.decode(msgFixture.bindingResponse,
                                      (char)0,
                                      (char)msgFixture.bindingResponse.length);

        assertEquals("A binding response was not properly decoded",
                     expectedReturn, actualReturn );



    }


    /**
     * Tests the equals method against a null, a different and an identical
     * object.
     *
     * @throws StunException java.lang.Exception if we fail
     */
    public void testEquals()
        throws StunException
    {
        Object target = null;
        boolean expectedReturn = false;
        boolean actualReturn = bindingRequest.equals(target);
        assertEquals("Equals failed against a null target", expectedReturn, actualReturn);

        actualReturn = bindingResponse.equals(target);
        assertEquals("Equals failed against a null target", expectedReturn, actualReturn);

        //different
        actualReturn = bindingRequest.equals(bindingResponse);
        assertEquals("Equals failed against a different target", expectedReturn, actualReturn);

        actualReturn = bindingResponse.equals(bindingRequest);
        assertEquals("Equals failed against a different target", expectedReturn, actualReturn);

        //same
        expectedReturn = true;

        //Create a binding request with the same attributes as this.bindingRequest
        Request binReqTarget = new Request();
        binReqTarget.setMessageType(Message.BINDING_REQUEST);
        binReqTarget.addAttribute(changeRequest);
        actualReturn = bindingRequest.equals(binReqTarget);
        assertEquals("Equals failed against an equal target", expectedReturn, actualReturn);

        //Create a binding response with the same attributes as this.bindingRequest
        Response binResTarget = new Response();
        binResTarget.setMessageType(Message.BINDING_RESPONSE);
        binResTarget.addAttribute(mappedAddress);
        binResTarget.addAttribute(sourceAddress);
        binResTarget.addAttribute(changedAddress);
        actualReturn = bindingResponse.equals(binResTarget);
        assertEquals("Equals failed against a different target", expectedReturn, actualReturn);



    }

    /**
     * Tests  whether attributes are properly counted
     */
    public void testGetAttributeCount() {
        int expectedReturn = 1;
        int actualReturn = bindingRequest.getAttributeCount();
        assertEquals("getAttributeCount failed for a bindingRequest",
                     expectedReturn, actualReturn);
        expectedReturn = 3;
        actualReturn = bindingResponse.getAttributeCount();
        assertEquals("getAttributeCount failed for a bindingRequest",
                     expectedReturn, actualReturn);

    }

    /**
     * Test whether attributes are properly removed.
     */
    public void testRemoveAttribute() {

        bindingRequest.removeAttribute(changeRequest.getAttributeType());

        assertNull("An attribute was still in the request after being removed",
                   bindingRequest.getAttribute(changeRequest.getAttributeType()));

        //test count
        int expectedReturn = 0;
        int actualReturn = bindingRequest.getAttributeCount();
        assertEquals("Attribute count did not change after removing an attribute",
                     expectedReturn, actualReturn);


    }

}
