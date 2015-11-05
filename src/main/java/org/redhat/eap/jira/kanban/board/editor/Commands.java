/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.redhat.eap.jira.kanban.board.editor;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.jboss.dmr.ModelNode;


/**
 * @author Kabir Khan
 */
public class Commands {
    final JiraConfiguration jiraConfiguration;
    final HttpAuthenticationFeature authenticationFeature;
    final Client client;

    Commands(JiraConfiguration jiraConfiguration) {
        this.jiraConfiguration = jiraConfiguration;
        authenticationFeature = HttpAuthenticationFeature.basic(jiraConfiguration.getUsername(), jiraConfiguration.getPassword());
        client = ClientBuilder.newBuilder()
            .register(authenticationFeature)
            .build();
    }


    private UriBuilder getRootRestUri() {
        return UriBuilder.fromUri(jiraConfiguration.getUri()).path("rest");
    }

    private UriBuilder getRootAgileUri() {
        return getRootRestUri().path("agile").path("1.0");
    }

    private UriBuilder getRootGrasshopperUri() {
        return getRootRestUri().path("greenhopper").path("1.0");
    }


    public Board findBoard(String name) {
        WebTarget target = client.target(
                getRootAgileUri().path("board")
                        .queryParam("type", "kanban")
                        .queryParam("name", name));

        Response response = target.request(MediaType.APPLICATION_JSON)
                .get();
        ModelNode modelNode = getOneValue(response, "name", name);
        return new Board(
                modelNode.get("id").asInt(),
                UriBuilder.fromUri(modelNode.get("self").asString()).build(),
                name);
    }

    public void deleteBoard(int id){
        WebTarget target = client.target(
                getRootGrasshopperUri().path("rapidview").path(String.valueOf(id)));

        Response response = target.request(MediaType.APPLICATION_JSON)
                .delete();
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new IllegalStateException("Could not delete " + id + " " + response.getStatus() + " " + responseBody(response, true));
        }
    }


    public Board copyBoard(Board source, String targetName) {
        WebTarget target = client.target(
                getRootGrasshopperUri().path("rapidview").path(String.valueOf(source.id)).path("copy"));

        Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.entity("{}", MediaType.APPLICATION_JSON));
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new IllegalStateException("Could not copy " + source.id + " " + response.getStatus() + " " + responseBody(response, true));
        }

        ModelNode modelNode = responseBody(response);
        String sourceUri = source.uri.toString();
        sourceUri = sourceUri.substring(0, sourceUri.lastIndexOf("/"));
        return new Board(
                modelNode.get("id").asInt(),
                UriBuilder.fromUri(sourceUri).path(modelNode.get("id").asString()).build(),
                modelNode.get("name").asString());
    }

    private ModelNode getOneValue(Response response, String key, String value) {
        ModelNode result = responseBody(response);
        List<ModelNode> values = result.get("values").asList();
        ModelNode found = null;
        for (ModelNode node : values) {
            if (node.get(key).asString().equals(value)) {
                if (found != null) {
                    throw new IllegalStateException("More than one entry with " + key + "==" + value + result);
                }
                found = node;
            }
        }
        if (found == null) {
            throw new IllegalStateException("Could not find any entry with " + key + "==" + value + result);
        }
        return found;
    }

    private ModelNode responseBody(Response response) {
        return responseBody(response, false);
    }

    private ModelNode responseBody(Response response, boolean forError) {
        String body = response.readEntity(String.class);
        try {
            return ModelNode.fromJSONString(body);
        } catch (IllegalArgumentException e) {
            if (forError) {
                return new ModelNode(body);
            }
            System.err.println("Could not convert the following body to json " + body);
            throw e;
        }
    }

    static class Board {
        final int id;
        final URI uri;
        final String name;

        public Board(int id, URI uri, String name) {
            this.id = id;
            this.uri = uri;
            this.name = name;
        }
    }


}
