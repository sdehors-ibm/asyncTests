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

import okhttp3.OkHttpClient;
import okhttp3.Request;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Path("external/jaxrs")
public class ExternalCallRestClientResource {

    private static String SERVICE_URL = "http://localhost:8080/hello-resteasy/wait";

    @Inject
    private Client client;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getExternalResource(@QueryParam("wait") long wait) throws IOException {

        Response response = client.target(SERVICE_URL ).queryParam("wait", wait).request().get();

        return response.readEntity(String.class);
        //System.out.println(connectionManager.getTotalStats());
    }

}
