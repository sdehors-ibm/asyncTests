package io.openliberty.guides.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Path("external/java")
public class ExternalCallJdkResource {

    private static String SERVICE_URL = "http://localhost:8080/hello-resteasy/wait";

    @Inject
    private HttpClient client;

    private HttpClient virtualClient = HttpClient.newBuilder()
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();



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
    public String getExternalResourceVirtual(@QueryParam("wait") long wait) throws IOException, URISyntaxException, InterruptedException {

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
