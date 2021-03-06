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

import org.redhat.eap.jira.kanban.board.editor.commands.Commands;
import org.redhat.eap.jira.kanban.board.editor.commands.JiraConfiguration;
import org.redhat.eap.jira.kanban.board.editor.commands.JqlSwimlaneConfigurations;
import org.redhat.eap.jira.kanban.board.editor.commands.JqlSwimlaneConfigurations.JqlSwimlaneConfig;
import org.redhat.eap.jira.kanban.board.editor.commands.JqlSwimlaneConfigurations.JqlSwimlaneSet;

/**
 * @author Kabir Khan
 */
public class SwimlaneCreator {
    public static void main(String[] args) throws Exception {
        //TODO make sure these are configurable
        String boardName = "Copy of Throwaway";
        JqlSwimlaneSet swimlaneSet = JqlSwimlaneSet.TEST;
        boolean deleteExistingSwimlanes = true;

        JiraConfiguration jiraConfiguration = JiraConfiguration.loadConfiguration();
        Commands commands = jiraConfiguration.createCommands();
        Commands.Board board = commands.findBoard(boardName);

        if (deleteExistingSwimlanes) {
            commands.deleteExistingJqlSwimlanes(board);
        }
        commands.setSwimlaneStrategy(board, Commands.SwimLaneStrategy.QUERIES);
        JqlSwimlaneConfig[] swimlaneConfigs = JqlSwimlaneConfigurations.getSwimlaneConfigs(swimlaneSet);
        for (int i = swimlaneConfigs.length - 1 ; i >= 0 ; i--) {
            commands.addJqlSwimlane(board, swimlaneConfigs[i]);
        }

        System.out.println("done");
    }
}
