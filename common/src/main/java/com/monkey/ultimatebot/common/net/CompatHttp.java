package com.monkey.ultimatebot.common.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class CompatHttp {

    private static final boolean JDK_HTTP_CLIENT_AVAILABLE = isJdkHttpClientAvailable();

    private CompatHttp() {}

    public static final class Response {
        private final int statusCode;
        private final String body;

        public Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body == null ? "" : body;
        }

        public int statusCode() {
            return statusCode;
        }

        public String body() {
            return body;
        }
    }

    public static Response postJson(
            URI uri, byte[] jsonBody, int connectTimeoutMs, int requestTimeoutMs, String userAgent) throws IOException {
        Objects.requireNonNull(uri, "uri");
        Objects.requireNonNull(jsonBody, "jsonBody");
        if (JDK_HTTP_CLIENT_AVAILABLE) {
            try {
                return postJsonWithJdkHttpClient(uri, jsonBody, connectTimeoutMs, requestTimeoutMs, userAgent);
            } catch (IOException ex) {
                if (!shouldFallbackToUrlConnection(ex)) {
                    throw ex;
                }
            } catch (RuntimeException ex) {
            }
        }
        return postJsonWithUrlConnection(uri, jsonBody, connectTimeoutMs, requestTimeoutMs, userAgent);
    }

    public static boolean isRetryableNetworkFailure(Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException
                    || cause instanceof ConnectException
                    || cause instanceof UnknownHostException) {
                return true;
            }
            String name = cause.getClass().getName();
            if ("java.net.http.HttpTimeoutException".equals(name)
                    || "java.net.http.HttpConnectTimeoutException".equals(name)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static boolean shouldFallbackToUrlConnection(IOException ex) {
        String message = ex.getMessage();
        return message != null && message.startsWith("JDK HttpClient unavailable:");
    }

    private static boolean isJdkHttpClientAvailable() {
        try {
            Class.forName("java.net.http.HttpClient");
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    private static Response postJsonWithJdkHttpClient(
            URI uri, byte[] jsonBody, int connectTimeoutMs, int requestTimeoutMs, String userAgent) throws IOException {
        try {
            Class<?> httpClientClass = Class.forName("java.net.http.HttpClient");
            Class<?> httpClientBuilderClass = Class.forName("java.net.http.HttpClient$Builder");
            Class<?> httpRequestClass = Class.forName("java.net.http.HttpRequest");
            Class<?> httpRequestBuilderClass = Class.forName("java.net.http.HttpRequest$Builder");
            Class<?> bodyPublisherClass = Class.forName("java.net.http.HttpRequest$BodyPublisher");
            Class<?> bodyPublishersClass = Class.forName("java.net.http.HttpRequest$BodyPublishers");
            Class<?> bodyHandlersClass = Class.forName("java.net.http.HttpResponse$BodyHandlers");
            Class<?> bodyHandlerClass = Class.forName("java.net.http.HttpResponse$BodyHandler");
            Class<?> httpResponseClass = Class.forName("java.net.http.HttpResponse");
            Class<?> durationClass = Class.forName("java.time.Duration");

            Method durationOfMillis = durationClass.getMethod("ofMillis", long.class);
            Object connectTimeout = durationOfMillis.invoke(null, (long) connectTimeoutMs);
            Object requestTimeout = durationOfMillis.invoke(null, (long) requestTimeoutMs);

            Object clientBuilder = httpClientClass.getMethod("newBuilder").invoke(null);
            clientBuilder = httpClientBuilderClass
                    .getMethod("connectTimeout", durationClass)
                    .invoke(clientBuilder, connectTimeout);
            Class<?> redirectClass = Class.forName("java.net.http.HttpClient$Redirect");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object neverRedirect = Enum.valueOf((Class) redirectClass, "NEVER");
            clientBuilder = httpClientBuilderClass
                    .getMethod("followRedirects", redirectClass)
                    .invoke(clientBuilder, neverRedirect);
            Object httpClient = httpClientBuilderClass.getMethod("build").invoke(clientBuilder);

            Object bodyPublisher =
                    bodyPublishersClass.getMethod("ofByteArray", byte[].class).invoke(null, new Object[] {jsonBody});

            Object requestBuilder =
                    httpRequestClass.getMethod("newBuilder", URI.class).invoke(null, uri);
            requestBuilder =
                    httpRequestBuilderClass.getMethod("timeout", durationClass).invoke(requestBuilder, requestTimeout);
            requestBuilder = httpRequestBuilderClass
                    .getMethod("header", String.class, String.class)
                    .invoke(requestBuilder, "Content-Type", "application/json; charset=utf-8");
            requestBuilder = httpRequestBuilderClass
                    .getMethod("header", String.class, String.class)
                    .invoke(requestBuilder, "Accept", "application/json");
            if (userAgent != null && !userAgent.trim().isEmpty()) {
                requestBuilder = httpRequestBuilderClass
                        .getMethod("header", String.class, String.class)
                        .invoke(requestBuilder, "User-Agent", userAgent);
            }
            requestBuilder = httpRequestBuilderClass
                    .getMethod("POST", bodyPublisherClass)
                    .invoke(requestBuilder, bodyPublisher);
            Object httpRequest = httpRequestBuilderClass.getMethod("build").invoke(requestBuilder);

            Object bodyHandler = bodyHandlersClass.getMethod("ofString").invoke(null);
            Object httpResponse = httpClientClass
                    .getMethod("send", httpRequestClass, bodyHandlerClass)
                    .invoke(httpClient, httpRequest, bodyHandler);

            int status = ((Integer) httpResponseClass.getMethod("statusCode").invoke(httpResponse)).intValue();
            String body = (String) httpResponseClass.getMethod("body").invoke(httpResponse);
            return new Response(status, body);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            throw new IOException("JDK HttpClient unavailable: " + ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            if (cause instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while executing HTTP request", cause);
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IOException("JDK HttpClient request failed: " + cause, cause);
        }
    }

    private static Response postJsonWithUrlConnection(
            URI uri, byte[] jsonBody, int connectTimeoutMs, int requestTimeoutMs, String userAgent) throws IOException {
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(requestTimeoutMs);
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Accept", "application/json");
        if (userAgent != null && !userAgent.trim().isEmpty()) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
        connection.setRequestProperty("Content-Length", String.valueOf(jsonBody.length));
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(jsonBody);
                output.flush();
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            return new Response(status, readFully(stream));
        } finally {
            connection.disconnect();
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = stream.read(chunk)) >= 0) {
            if (read > 0) {
                buffer.write(chunk, 0, read);
            }
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
