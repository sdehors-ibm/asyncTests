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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Path("external")
public class ExternalCallResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getExternalResource() throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();

        HttpUriRequest get = new HttpGet("https://www.google.com");

        CloseableHttpResponse response = client.execute(get);

        return response.getStatusLine().toString();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("async")
    public CompletableFuture<String> getExternalResourceAsync() {

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();

        HttpUriRequest get = new HttpGet("https://www.google.com");

        CompletableFuture<String> res = new CompletableFuture<>();

        client.execute(get, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
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