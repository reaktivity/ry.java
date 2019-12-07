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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.reaktivity.ry.internal.RyTestCommandSpi.TEST_ARGUMENT;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.ivy.Ivy;
import org.apache.ivy.ant.AntWorkspaceResolver;
import org.apache.ivy.ant.AntWorkspaceResolver.WorkspaceArtifact;
import org.apache.ivy.core.install.InstallOptions;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
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
        ModuleRevisionId reference = ModuleRevisionId.newInstance("org.reaktivity", "ry", "develop-SNAPSHOT");

        InstallOptions options = new InstallOptions();
        options.setTransitive(false);
        options.setOverwrite(true);

        ResourceCollection ivyFile = new FileResource(new File("pom.xml").getAbsoluteFile());

        final File jarPath = new File("target/dist/jars/ry.jar");
        jarPath.getParentFile().mkdirs();

        AntWorkspaceResolver workspace = new AntWorkspaceResolver();
        workspace.setName("workspace");
        workspace.addConfigured(ivyFile);
        DependencyResolver workspaceResolver = workspace.getResolver();
        WorkspaceArtifact artifact = workspace.createArtifact();
        artifact.setPath(jarPath.getPath());

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath)))
        {
            jos.putNextEntry(new JarEntry(String.format("META-INF/services/%s", RyCommandSpi.class.getName())));
            jos.write(RyTestCommandSpi.class.getName().getBytes());

            FileSystemResolver cache = new FileSystemResolver();
            cache.setName("cache");
            cache.setLocal(true);
            cache.setTransactional("false");
            cache.addArtifactPattern("/tmp/test-cache/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
            // cache.addIvyPattern("/tmp/test-cache/[organisation]/[module]/[type]s/[artifact]-[revision].xml");

            IvySettings ivySettings = new IvySettings();
            ivySettings.setDefaultCache(new File("/tmp/test-cache"));
            ivySettings.addConfigured(workspaceResolver);
            ivySettings.addConfigured(cache);

            Ivy ivy = Ivy.newInstance(ivySettings);

            ResolveReport report = ivy.install(reference, workspaceResolver.getName(), cache.getName(), options);
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
         RyMain.main(new String[] { "test" });

         assertEquals("arg", TEST_ARGUMENT.get());
    }

    @Test
    public void shouldInvokeTestCommandWithOverriddenArgument()
    {
         RyMain.main(new String[] { "test", "arg1" });

         assertEquals("arg1", TEST_ARGUMENT.get());
    }
}
