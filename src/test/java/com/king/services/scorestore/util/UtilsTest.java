package com.king.services.scorestore.util;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    @Mock
    HttpExchange httpExchange;
    @Mock
    OutputStream os;

    @Test
    public void generateRandomString() {
        String randomString = Utils.generateRandomString();
        Assert.assertNotNull(randomString);
        Assert.assertEquals(7, randomString.length());
    }

    @Test
    public void generateRandomChars() {
        String randomString = Utils.generateRandomChars("ABCDEFGH", 2);
        Assert.assertNotNull(randomString);
        Assert.assertEquals(2, randomString.length());
    }

    @Test
    public void generateResponseNullHttpExchange() {
        HttpExchange httpExchange = null;
        Utils.generateResponse(httpExchange, "ABCDEFGH", 200);
        Assert.assertNull(httpExchange);
    }

    @Test
    public void generateResponseNullMessage() {
        HttpExchange httpExchange = null;
        Utils.generateResponse(httpExchange, null, 200);
        Assert.assertNull(httpExchange);
    }

    @Test
    public void generateResponseNotNullMessage() {
        Mockito.when(httpExchange.getResponseBody()).thenReturn(os);
        Utils.generateResponse(httpExchange, "Test", 200);
        Assert.assertNotNull(httpExchange);
    }

    @Test
    public void extractQueryParamValueNullHttpExchange() {
        Optional<String> payloadOptional = Utils.extractQueryParamValue(null, "Test");
        Assert.assertNotNull(payloadOptional);
        Assert.assertFalse(payloadOptional.isPresent());
    }

    @Test
    public void extractQueryParamValueNullKey() {
        Optional<String> payloadOptional = Utils.extractQueryParamValue(httpExchange, null);
        Assert.assertNotNull(payloadOptional);
        Assert.assertFalse(payloadOptional.isPresent());
    }

    @Test
    public void extractQueryParamValueNotMatch() throws URISyntaxException {
        URI uri = new URI("key=value");
        Mockito.when(httpExchange.getRequestURI()).thenReturn(uri);
        Optional<String> payloadOptional = Utils.extractQueryParamValue(httpExchange, "Test");
        Assert.assertNotNull(payloadOptional);
        Assert.assertFalse(payloadOptional.isPresent());
    }

    @Test
    public void extractQueryParamValue() throws URISyntaxException {
        URI uri = new URI("key=value");
        Mockito.when(httpExchange.getRequestURI()).thenReturn(uri);
        Optional<String> payloadOptional = Utils.extractQueryParamValue(httpExchange, "key");
        Assert.assertNotNull(payloadOptional);
        Assert.assertFalse(payloadOptional.isPresent());
    }

    @Test
    public void extractPayloadNullHttpExchange() {
        Optional<String> payloadOptional = Utils.extractPayload(null);
        Assert.assertNotNull(payloadOptional);
        Assert.assertFalse(payloadOptional.isPresent());
    }

    @Test
    public void extractPayloadNull() {
        Optional<String> payloadOptional = Utils.extractPayload(httpExchange);
        Assert.assertNotNull(payloadOptional);
        Assert.assertFalse(payloadOptional.isPresent());
    }

    @Test
    public void eangeCheck() {
        Assert.assertFalse(Utils.rangeCheck(-1));
        Assert.assertTrue(Utils.rangeCheck(0));
    }
}
