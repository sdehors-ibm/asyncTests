// tag::comment[]
/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
// end::comment[]
package io.openliberty.guides.rest;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Path("external")
public class ExternalCallResource {

    private static String SERVICE_URL = "http://localhost:8080/hello-resteasy/wait";

    @Inject
    private CloseableHttpClient client;

    @Inject
    private PoolingHttpClientConnectionManager connectionManager;

    @Inject
    private PoolingNHttpClientConnectionManager asyncConnectionManager;

    @Inject
    private CloseableHttpAsyncClient asyncClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getExternalResource(@QueryParam("wait") long wait) throws IOException {

        HttpUriRequest get = new HttpGet(SERVICE_URL + "?wait=" + wait);

        CloseableHttpResponse response = client.execute(get);

        String statusLine = response.getStatusLine().toString() + " : " + IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());

        response.close();

        System.out.println(connectionManager.getTotalStats());

        return statusLine;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("async")
    public CompletableFuture<String> getExternalResourceAsync(@QueryParam("wait") long wait) {

        HttpUriRequest get = new HttpGet(SERVICE_URL + "?wait=" + wait);

        CompletableFuture<String> res = new CompletableFuture<>();

        asyncClient.execute(get, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                System.out.println(asyncConnectionManager.getTotalStats());
                res.complete(response.getStatusLine().toString());
            }

            @Override
            public void failed(Exception ex) {
                res.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                res.complete(null);
            }
        });

        return res;
    }


}
