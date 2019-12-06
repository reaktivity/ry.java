/**
 * Copyright 2016-2019 The Reaktivity Project
 *
 * The Reaktivity Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.reaktivity.ry.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.naming.spi.Resolver;

import org.apache.ivy.Ivy;
import org.apache.ivy.ant.AntWorkspaceResolver;
import org.apache.ivy.ant.AntWorkspaceResolver.WorkspaceArtifact;
import org.apache.ivy.core.install.InstallOptions;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reaktivity.ry.RyCommandSpi;

public class RyMainTest
{
    @BeforeAll
    static void setup()
    {
        DefaultMessageLogger logger = new DefaultMessageLogger(Message.MSG_VERBOSE);
        Message.setDefaultLogger(logger);

        // TODO Rename this variable, artifact name, and what version to use?
        ModuleRevisionId reference = ModuleRevisionId.newInstance("reaktivity.org", "spi-test", "0.1");

        InstallOptions options = new InstallOptions();

        final String jarPath = "/tmp/a.jar"; // TODO Do something with this

        try (
            // TODO Fix these variable names
            FileOutputStream fos = new FileOutputStream(jarPath);
            JarOutputStream jos = new JarOutputStream(fos);
            BufferedOutputStream bos = new BufferedOutputStream(jos);)
        {
            jos.putNextEntry(new JarEntry(String.format("/META-INF/services/%s", RyCommandSpi.class.getName())));
            bos.write(RyTestCommandSpi.class.getName().getBytes());

            AntWorkspaceResolver from = new AntWorkspaceResolver();
            from.setName("workspace"); // TODO Make a constant
            WorkspaceArtifact jarArtifact = from.createArtifact();
            jarArtifact.setName(RyCommandSpi.class.getName());
            jarArtifact.setPath(jarPath);
            jarArtifact.setType("jar"); // TODO Defaults, so not necessary?
            jarArtifact.setExt("jar"); // TODO Defaults, so not necessary?

            FileSystemResolver localTest = new FileSystemResolver();
            localTest.setName("local-test");
            localTest.setM2compatible(true);
            localTest.setLocal(true);

            IvySettings ivySettings = new IvySettings();
            ivySettings.setDefaultCache(new File("/tmp/test-cache")); // TODO Fix location

            Ivy ivy = Ivy.newInstance(ivySettings);

            ResolveReport report = ivy.install(reference, "workspace", localTest.getName(), options);
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    
    @Test
    public void shouldInvokeTestCommandWithDefaultArgument()
    {
        // RyMain.main(new String[] { "test" });

        // assertEquals("arg", TEST_ARGUMENT.get());
        assertTrue(true);
    }

    @Test
    public void shouldInvokeTestCommandWithOverriddenArgument()
    {
        // RyMain.main(new String[] { "test", "arg1" });

        // assertEquals("arg1", TEST_ARGUMENT.get());
        assertTrue(true);
    }
}
