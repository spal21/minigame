package com.king.services.scorestore;

import com.king.services.scorestore.app.Application;
import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.util.Utils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class IntegrationTest {

    private final static CountDownLatch latch = new CountDownLatch(1);

    @BeforeClass
    public static void setup() {
        Application testApp = new Application();

        Thread thread = new Thread(() -> {
            testApp.runTestServer();
            try {
                latch.await();
                testApp.closeServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        thread.start();
    }

    @AfterClass
    public static void teardown() {
        latch.countDown();
    }

    @Test
    public void testLoginAPI() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://127.0.0.1:8090/2310/login");
        CloseableHttpResponse response = httpClient.execute(get);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        String body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals(7, body.length());
    }

    @Test
    public void testLoginAPIOutOfRange() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://127.0.0.1:8090/234423423443/login");
        CloseableHttpResponse response = httpClient.execute(get);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        String body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals("LoginID must be a number/ unsigned integer.", body);
    }


    @Test
    public void testRegisterScoreInvalidSessionKey() throws IOException {
        String sessionKey = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://127.0.0.1:8090/2/score?sessionkey=ABCEDF");
        post.setEntity(new StringEntity("1200"));
        CloseableHttpResponse response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        String body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals("", body);
    }

    @Test
    public void testRegisterScoreAllScenarios() throws IOException {
        String sessionKey = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://127.0.0.1:8090/2311/login");
        CloseableHttpResponse response = httpClient.execute(get);
        sessionKey = Utils.processStream(response.getEntity().getContent());


        httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://127.0.0.1:8090/0/score?sessionkey=" + sessionKey);
        post.setEntity(new StringEntity("1200"));
        response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        String body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals("", body);

        post = new HttpPost("http://127.0.0.1:8090/1/score?sessionkey=");
        post.setEntity(new StringEntity("1200"));
        response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals(Constants.SESSIONKEY_REQUIRED_ERROR_MESSAGE, body);

        post = new HttpPost("http://127.0.0.1:8090/1/score?sessionkey=" + sessionKey);
        response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals(Constants.PAYLOAD_REQUIRED_ERROR_MESSAGE, body);

        post = new HttpPost("http://127.0.0.1:8090/1/score?sessionkey=" + sessionKey);
        post.setEntity(new StringEntity("1200"));
        response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals("", body);

    }

    @Test
    public void testAllFunctionalRequirements() throws IOException {
        String sessionKey = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://127.0.0.1:8090/2312/login");
        CloseableHttpResponse response = httpClient.execute(get);
        sessionKey = Utils.processStream(response.getEntity().getContent());

        httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://127.0.0.1:8090/2/score?sessionkey=" + sessionKey);
        post.setEntity(new StringEntity("1200"));
        response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        String body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals("", body);

        httpClient = HttpClients.createDefault();
        post = new HttpPost("http://127.0.0.1:8090/0/score?sessionkey=" + sessionKey);
        post.setEntity(new StringEntity("1200"));
        response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals("", body);

        httpClient = HttpClients.createDefault();
        get = new HttpGet("http://localhost:8090/1/highscorelist");
        response = httpClient.execute(get);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        body = Utils.processStream(response.getEntity().getContent());
        Assert.assertEquals(0, body.length());

        httpClient = HttpClients.createDefault();
        post = new HttpPost("http://127.0.0.1:8090/1/score?sessionkey=" + sessionKey);
        post.setEntity(new StringEntity("1200"));
        response = httpClient.execute(post);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertNotNull(response.getEntity());
        body = Utils.processStream(response.getEntity().getContent());
        Assert.assertNotNull(body);
        Assert.assertEquals("", body);

        httpClient = HttpClients.createDefault();
        get = new HttpGet("http://localhost:8090/1/highscorelist");
        response = httpClient.execute(get);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getStatusLine());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        body = Utils.processStream(response.getEntity().getContent());
        Assert.assertEquals("2312=1200", body);
    }
}
