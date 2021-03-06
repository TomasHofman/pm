/*
 * Copyright 2016-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.provisioning.plugin.wildfly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jboss.provisioning.MessageWriter;
import org.jboss.provisioning.ProvisioningException;

/**
 * Create an embedded server and execute a list of commands on it.
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class EmbeddedServer {
    private final Path installDir;
    private final MessageWriter messageWriter;

    public EmbeddedServer(Path installDir, MessageWriter messageWriter) {
        this.installDir = installDir;
        this.messageWriter = messageWriter;
    }

    /**
     * Starts an embedded server to execute commands
     * @param commands the list of commands to execute on the embedded server.
     * @throws ProvisioningException
     */
    public void execute(boolean validate, String ... commands) throws ProvisioningException {
        execute(validate, Arrays.asList(commands));
    }

    /**
     * Starts an embedded server to execute commands
     * @param commands the list of commands to execute on the embedded server.
     * @throws ProvisioningException
     */
    public void execute(boolean validate, List<String> commands) throws ProvisioningException {
        List<String> allCommands = new ArrayList<>();
        allCommands.add(startEmbeddedServerCommand("standalone.xml"));
        allCommands.addAll(commands);
        allCommands.add("stop-embedded-server");
        try {
            Path script = Files.createTempFile("", ".cli");
            Files.write(script, allCommands);
            messageWriter.verbose("Cli script %s ", script);
            CliScriptRunner.runCliScript(installDir, script, messageWriter);
        } catch (IOException e) {
            messageWriter.error(e, "Error using console");
            messageWriter.verbose(e, null);
            throw new ProvisioningException(e);
        }
    }

    public static String startEmbeddedServerCommand(String config) {
         String localConfig = "standalone.xml";
         if(config != null && ! config.isEmpty()) {
             localConfig = config;
         }
         return String.format("embed-server --admin-only --server-config=%s", localConfig);
    }
}
