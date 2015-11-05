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
    private static final String JIRA_URL_DEFAULT = "https://issues.jboss.org";


    private final String username;
    private final String password;
    private final URI uri;

    private Commands commands;

    private JiraConfiguration(String username, String password, URI uri) {
        this.username = username;
        this.password = password;
        this.uri = uri;
    }

    Commands createCommands() {
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

    public static JiraConfiguration loadConfiguration() throws Exception {
        final URL url = JiraConfiguration.class.getResource("/configuration.properties");
        final Properties properties = new Properties();
        if (url == null) {
            System.out.println("There is no /src/resources/jiraConfiguration.properties file. Relying on system properties");
        } else {
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(url.toURI())))){
                properties.load(in);
            }
        }

        final String username = getProperty(USERNAME_KEY, null, properties, url);
        final String password = getProperty(PASSWORD_KEY, null, properties, url);
        final String jiraUrl = getProperty(JIRA_URL_KEY, JIRA_URL_DEFAULT, properties, url);

        return new JiraConfiguration(username, password, new URL(jiraUrl).toURI());
    }

    private static String getProperty(String key, String defaultValue, Properties properties, URL propertiesUrl) {
        String value = System.getProperty(key);
        if (value == null) {
            value = properties.getProperty(key);
        }
        if (value != null) {
            return value;
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        throw new IllegalStateException("No value set for '" + key + "' either in " + propertiesUrl + " or as -D" + key + "=...");
    }
}
