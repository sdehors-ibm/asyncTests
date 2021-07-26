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

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Path("properties")
@Produces(MediaType.APPLICATION_JSON)
public class PropertiesResource {

    @GET
    @Path("sync")
    public Properties getProperties() {
        return System.getProperties();
    }

    @GET
    @Path("async")
    public CompletableFuture<Properties> getPropertiesAsync() {
        return CompletableFuture.completedFuture(System.getProperties());
    }


    @GET
    @Path("asyncresponse")
    public void getPropertiesAsyncResponse(@Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(System.getProperties());
    }
}
