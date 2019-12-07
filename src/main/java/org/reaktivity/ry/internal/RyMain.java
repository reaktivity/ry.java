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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.reaktivity.ry.RyCommandSpi;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.help.Help;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public final class RyMain
{
    private static final String RY_EXECUTABLE_NAME = "ry";

    private static final String DEPENDENCY_FILENAME = "ry.deps";
    private static final String DEPENDENCY_LOCK_FILENAME = String.format("%s.lock", DEPENDENCY_FILENAME);

    private static final String PROPERTY_DEPENDENCIES = "dependencies";

    private static final String CACHE_DIR = String.format("%s/.ry", System.getProperty("user.home"));

    private static final String DEFAULT_GROUP_ID = "org.reaktivity";

    private static Map<String, String> dependencies = new LinkedHashMap<>();

    private static List<URL> artifactOrigins = new LinkedList<>();

    public static void main(
        String[] args)
    {
        DefaultMessageLogger logger = new DefaultMessageLogger(Message.MSG_INFO);

        ResolveOptions options = new ResolveOptions();
        options.setLog(ResolveOptions.LOG_DOWNLOAD_ONLY);

        final CliBuilder<Runnable> builder = Cli.<Runnable>builder(RY_EXECUTABLE_NAME)
                .withDefaultCommand(Help.class)
                .withCommand(Help.class);

        try
        {
            final File depsFile = resolveDepsFile();
            readDepsFile(depsFile);

            boolean resolved = resolveDependencies(options);
            if (!resolved)
            {
                throw new Exception("Dependencies failed to resolve");
            }

            URLClassLoader loader  = new URLClassLoader(
                artifactOrigins.toArray(new URL[artifactOrigins.size()]),
                Thread.currentThread().getContextClassLoader());
            for (RyCommandSpi service : ServiceLoader.load(RyCommandSpi.class, loader))
            {
                service.mixin(builder);
            }
        }
        catch (Exception ex)
        {
            logger.error(String.format("Error: %s", ex.getMessage()));
        }
        finally
        {
            logger.sumupProblems();
        }

        final Cli<Runnable> parser = builder.build();
        parser.parse(args).run();
    }

    private static File resolveDepsFile() throws IOException
    {
        File depsFile = new File(DEPENDENCY_LOCK_FILENAME);
        if (!depsFile.exists())
        {
            depsFile = new File(DEPENDENCY_FILENAME);
            if (!depsFile.exists())
            {
                throw new FileNotFoundException(
                    String.format("Cannot find %s or %s", DEPENDENCY_LOCK_FILENAME, DEPENDENCY_FILENAME));
            }
        }
        return depsFile;
    }

    private static void readDepsFile(File depsFile) throws IOException
    {
        JsonElement deps = null;

        try (BufferedReader br = new BufferedReader(new FileReader(depsFile)))
        {
            deps = new JsonParser().parse(br);
        }
        if (!deps.isJsonObject())
        {
            throw new JsonSyntaxException(String.format("%s is not in JSON format\n", DEPENDENCY_FILENAME));
        }

        JsonObject depsObj = (JsonObject)deps;

        JsonElement dependenciesEl = depsObj.get(PROPERTY_DEPENDENCIES);
        if (dependenciesEl != null)
        {
            if (!dependenciesEl.isJsonObject())
            {
                throw new JsonSyntaxException(String.format("%s is not a JSON object", PROPERTY_DEPENDENCIES));
            }
            JsonObject dependenciesObj = (JsonObject)dependenciesEl;
            dependenciesObj.entrySet().forEach(e -> dependencies.put(e.getKey(), e.getValue().getAsString()));
        }

    }

    private static boolean resolveDependencies(
        ResolveOptions options) throws ParseException, IOException
    {
        FileSystemResolver local = new FileSystemResolver();
        local.setName("local");
        local.setM2compatible(true);
        local.setLocal(true);

        IvySettings ivySettings = new IvySettings();
        ivySettings.setDefaultCache(new File(CACHE_DIR));
        ivySettings.addConfigured(local);
        ivySettings.setDefaultResolver(local.getName());

        Ivy ivy = Ivy.newInstance(ivySettings);

        boolean hasErrors = false;
        for (Map.Entry<String, String> dependency : dependencies.entrySet())
        {
            String optionallyQualifiedArtifact = dependency.getKey();
            String version = dependency.getValue();

            String[] parts = optionallyQualifiedArtifact.split(":");
            String groupId = parts.length == 2 ? parts[0] : DEFAULT_GROUP_ID;
            String artifactId = parts[parts.length - 1];

            ModuleRevisionId reference = ModuleRevisionId.newInstance(groupId, artifactId, version);

            ResolveReport report = ivy.resolve(reference, options, false);
            hasErrors |= report.hasError();
            for (ArtifactDownloadReport artifactReport : report.getAllArtifactsReports())
            {
                artifactOrigins.add(artifactReport.getLocalFile().toURI().toURL());
            }
        }
        return !hasErrors;
    }

    private RyMain()
    {
        // utility class
    }
}
