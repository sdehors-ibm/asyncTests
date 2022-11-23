package io.openliberty.guides.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Path("external/java")
public class ExternalCallJava11Resource {

    private static String SERVICE_URL = "http://localhost:8080/hello-resteasy/wait";

    @Inject
    private HttpClient client;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getExternalResource(@QueryParam("wait") long wait) throws IOException, URISyntaxException, InterruptedException {

        HttpRequest get = HttpRequest.newBuilder()
                .uri(new URI(SERVICE_URL + "?wait=" + wait))
                .GET()
                .build();

        HttpResponse<String> response = client.send(get, BodyHandlers.ofString());

        String statusLine = response.statusCode() + " : " + response.body();

        return statusLine;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("async")
    public CompletableFuture<String> getExternalResourceAsync(@QueryParam("wait") long wait) throws URISyntaxException {

        HttpRequest get = HttpRequest.newBuilder()
                .uri(new URI("https://github.com"))
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(get, BodyHandlers.ofString());

        return response.thenApply(r -> r.statusCode() + " : " + r.body());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("asyncresponse")
    public void getExternalResourceAsync(@QueryParam("wait") long wait, @Suspended AsyncResponse asyncResponse) throws URISyntaxException {

        HttpRequest get = HttpRequest.newBuilder()
                .uri(new URI(SERVICE_URL + "?wait=" + wait))
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(get, BodyHandlers.ofString());

        response.thenAccept(r -> asyncResponse.resume(r.statusCode() + " : " + r.body()));
    }
}
