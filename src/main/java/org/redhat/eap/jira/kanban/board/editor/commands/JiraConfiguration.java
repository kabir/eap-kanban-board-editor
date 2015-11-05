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
package org.redhat.eap.jira.kanban.board.editor.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

/**
 * To run this set up the src/main/resources/jiraConfiguration.properties file. The keys are
 * <ul>
 * <li><b></b>editor.username</b> the Jira username</li>
 * <li><b>editor.password</b> the Jira password</li>
 * <li><b>editor.jira.url</b> the url of the Jira instance (defaults to https://issues.jboss.org</li>
 * <li><b>editor.jira.project</b> the key of the Jira project</li>
 * </ul>
 *
 * The above can also be specified using system properties, in which case the system property takes precedence over what
 * is in the file.
 *
 * @author Kabir Khan
 */
public class JiraConfiguration {

    private static final String USERNAME_KEY = "editor.username";
    private static final String PASSWORD_KEY = "editor.password";
    private static final String JIRA_URL_KEY = "editor.jira.url";
    private static final String PROJECT_KEY = "editor.jira.project";

    private final String username;
    private final String password;
    private final URI uri;
    private final String project;
    private Commands commands;

    private JiraConfiguration(String username, String password, URI uri, String project) {
        this.username = username;
        this.password = password;
        this.uri = uri;
        this.project = project;
    }

    public Commands createCommands() {
        if (commands == null) {
            Commands commands = new Commands(this);
            this.commands = commands;
        }
        return commands;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public URI getUri() {
        return uri;
    }

    public Object getProject() {
        return project;
    }

    public static JiraConfiguration loadConfiguration() throws Exception {
        final URL configUrl = JiraConfiguration.class.getResource("/configuration.properties");
        final Properties configProperties = new Properties();
        if (configUrl == null) {
            System.out.println("There is no /src/resources/jiraConfiguration.properties file. Relying on system properties");
        } else {
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(configUrl.toURI())))){
                configProperties.load(in);
            }
        }

        final URL defaultsUrl = JiraConfiguration.class.getResource("/defaults.properties");
        final Properties defaultsProperties = new Properties();
        if (defaultsUrl != null) {
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(defaultsUrl.toURI())))){
                defaultsProperties.load(in);
            }
        }

        final String username = getProperty(USERNAME_KEY, configProperties, defaultsProperties, configUrl);
        final String password = getProperty(PASSWORD_KEY, configProperties, defaultsProperties, configUrl);
        final String jiraUrl = getProperty(JIRA_URL_KEY, configProperties, defaultsProperties, configUrl);
        final String project = getProperty(PROJECT_KEY, configProperties, defaultsProperties, configUrl);

        return new JiraConfiguration(username, password, new URL(jiraUrl).toURI(), project);
    }

    private static String getProperty(String key, Properties configProperties, Properties defaultProperties, URL propertiesUrl) {
        String value = System.getProperty(key);
        if (value == null) {
            value = configProperties.getProperty(key);
        }
        if (value == null) {
            value = defaultProperties.getProperty(key);
        }
        if (value != null) {
            return value;
        }

        throw new IllegalStateException("No value set for '" + key + "' either in " + propertiesUrl + " or as -D" + key + "=...");
    }

}
