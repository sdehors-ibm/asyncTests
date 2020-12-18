package io.openliberty.guides.rest;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@ApplicationScoped
public class Config {

    // time outs for http clients, in seconds
    private static final int TIMEOUT_SOCKET_SEC = 120;
    private static final int TIMEOUT_CONNECTION_REQUEST_SEC = 20;
    private static final int TIMEOUT_CONNECT_SEC = 10;
    private static final int HTTP_POOLS_MAX_CONN_TOTAL = 200;
    private static final int HTTP_POOLS_MAX_CONN_PER_ROUTE = 200;

    @Produces
    @Singleton
    CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        return HttpClients.custom()
            .setDefaultRequestConfig(getRequestConfig())
            .setConnectionManager(connectionManager)
            .setKeepAliveStrategy((response, context) -> 20000)
            .disableCookieManagement()
            .build();
    }

    @Produces
    @Singleton
    CloseableHttpAsyncClient getHttpAsyncClient(PoolingNHttpClientConnectionManager connectionManager) {
        CloseableHttpAsyncClient asyncClient = HttpAsyncClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(getRequestConfig())
            .disableCookieManagement()
            .build();

        asyncClient.start();
        return asyncClient;
    }

    private RequestConfig getRequestConfig() {
        return RequestConfig.copy(RequestConfig.DEFAULT)
            .setSocketTimeout(TIMEOUT_SOCKET_SEC * 1000)
            .setConnectionRequestTimeout(TIMEOUT_CONNECTION_REQUEST_SEC * 1000)
            .setConnectTimeout(TIMEOUT_CONNECT_SEC * 1000)
            .build();
    }

    @Produces
    @Singleton
    private PoolingHttpClientConnectionManager getHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager(getDefaultSocketFactoryRegistry());
        poolingConnManager.setMaxTotal(HTTP_POOLS_MAX_CONN_TOTAL);
        poolingConnManager.setDefaultMaxPerRoute(HTTP_POOLS_MAX_CONN_PER_ROUTE);
        return poolingConnManager;
    }

    @Produces
    @Singleton
    private PoolingNHttpClientConnectionManager getNHttpClientConnectionManager() throws IOReactorException {
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT);

        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connectionManager.setMaxTotal(HTTP_POOLS_MAX_CONN_TOTAL);
        connectionManager.setDefaultMaxPerRoute(HTTP_POOLS_MAX_CONN_PER_ROUTE);

        return connectionManager;
    }


    private Registry<ConnectionSocketFactory> getDefaultSocketFactoryRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
    }

}
