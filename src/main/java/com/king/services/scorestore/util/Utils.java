package com.king.services.scorestore.util;

import com.king.services.scorestore.model.Constants;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilility Methods for Score Store
 */
public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    /**
     * Generate Http Response with given Message and HttpCode
     *
     * @param httpExchange
     * @param message
     * @param httpCode
     */
    public static void generateResponse(HttpExchange httpExchange, String message, int httpCode) {
        OutputStream os = null;
        try {
            if (Objects.nonNull(httpExchange) && Objects.nonNull(message)) {
                httpExchange.sendResponseHeaders(httpCode, message.length());
                os = httpExchange.getResponseBody();
                os.write(message.getBytes());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in generateResponse : ", e);
        } finally {
            try {
                if (Objects.nonNull(os))
                    os.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception encountered in generateResponse while handling close stream: ", e);
            }
        }
    }

    /**
     * Extract Payload from HttpRequest
     *
     * @param httpExchange
     * @return Optional String - Payload
     */
    public static Optional<String> extractPayload(HttpExchange httpExchange) {
        InputStream payloadStream = null;
        if (Objects.nonNull(httpExchange) && Objects.nonNull(payloadStream = httpExchange.getRequestBody())) {
            return Optional.of(Utils.processStream(payloadStream));
        }
        return Optional.empty();
    }

    /**
     * Extract Value for a given Param from QueryString
     *
     * @param httpExchange
     * @param key
     * @return Optional String - Query Param Value
     */
    public static Optional<String> extractQueryParamValue(HttpExchange httpExchange, String key) {
        String query = null;
        if (Objects.nonNull(key) && Objects.nonNull(httpExchange) && Objects.nonNull(httpExchange.getRequestURI()) &&
                Objects.nonNull(query = httpExchange.getRequestURI().getQuery())) {
            String temp[];
            for (String queryParamValue : query.split("&")) {
                if (key.equals((temp = queryParamValue.split("="))[0]) && temp.length > 1) {
                    return Optional.of(temp[1]);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Extracts Value for a given Path Param
     *
     * @param httpExchange
     * @param field
     * @return Optional String - Path Param Value
     */
    public static Optional<String> extractPathParamValue(HttpExchange httpExchange, String field) {
        String path;
        if (Objects.nonNull(field) && Objects.nonNull(httpExchange) && Objects.nonNull(httpExchange.getRequestURI()) &&
                Objects.nonNull(path = httpExchange.getRequestURI().getPath())) {
            String queryParamValues[] = path.split("/");
            int index = 0;
            while (index < queryParamValues.length) {
                if (field.equals(queryParamValues[index]) && index > 0) {
                    return Optional.of(queryParamValues[index - 1]);
                }
                index++;
            }
        }
        return Optional.empty();
    }

    /**
     * Helper Method to Process Stream of Http Request Payload
     *
     * @param inputStream
     * @return request Payload
     */
    public static String processStream(InputStream inputStream) {
        String output = "";
        try (InputStreamReader isr = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(isr);) {

            String content = "";
            while ((content = br.readLine()) != null) {
                output += content;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Generate Random Alphabetical String in UpperCase. The length is controlled By Constants.SESSION_ID_LENGTH
     *
     * @return Random String
     */
    public static String generateRandomString() {
        final String salt = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return generateRandomChars(salt, Constants.SESSION_ID_LENGTH);
    }

    /**
     * Helper Method to generate Random String
     *
     * @param candidateChars
     * @param length
     * @return
     */
    public static String generateRandomChars(String candidateChars, int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }
        return sb.toString();
    }

    /**
     * Helper Method to check if an integer is a unsigned 31 bit integer
     *
     * @param num
     * @return
     */
    public static boolean rangeCheck(int num) {
        int max = Integer.MAX_VALUE;
        return (num >= 0 && num <= max);

    }
}
