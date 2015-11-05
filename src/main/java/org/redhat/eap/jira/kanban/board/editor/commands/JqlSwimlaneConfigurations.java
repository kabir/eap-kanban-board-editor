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

/**
 * @author Kabir Khan
 */
public class JqlSwimlaneConfigurations {

    /**
     * A set of test configs just used for testing the API
     */
    static JqlSwimlaneConfig[] testConfigs = new JqlSwimlaneConfig[]{
            new JqlSwimlaneConfig("Expedite", "priority=\"Blocker\""),
            JqlSwimlaneConfig.forComponent("Component A"),
            JqlSwimlaneConfig.forComponent("Component B")
    };

    public static JqlSwimlaneConfig[] getSwimlaneConfigs(JqlSwimlaneSet set) {
        if (set == JqlSwimlaneSet.TEST) {
            return testConfigs;
        }
        throw new IllegalStateException("Unknown set " + set);
    }

    public static class JqlSwimlaneConfig {
        private final String name;
        private final String jql;
        private final String description;

        public JqlSwimlaneConfig(String name, String jql) {
            this(name, jql, null);
        }

        public JqlSwimlaneConfig(String name, String jql, String description) {
            if (name == null || jql == null) {
                throw new IllegalStateException("Null name or jql");
            }
            this.name = name;
            this.jql = jql;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getJql() {
            return jql;
        }

        public String getDescription() {
            return description;
        }

        static JqlSwimlaneConfig forComponent(String componentNane) {
            return new JqlSwimlaneConfig(componentNane, "component=\"" + componentNane + "\"", "Swimlane for " + componentNane);
        }
    }

    public enum JqlSwimlaneSet {
        TEST
    }

}
