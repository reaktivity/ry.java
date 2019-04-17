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

import java.util.ServiceLoader;

import org.reaktivity.ry.RyCommandSpi;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.help.Help;

public final class RyMain
{
    private static final String RY_EXECUTABLE_NAME = "ry";
//    private static final String RY_DEPENDENCIES_NAME = "ry.deps";

    public static void main(
        String[] args)
    {
        final CliBuilder<Runnable> builder = Cli.<Runnable>builder(RY_EXECUTABLE_NAME)
                .withDefaultCommand(Help.class)
                .withCommand(Help.class);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (RyCommandSpi service : ServiceLoader.load(RyCommandSpi.class, loader))
        {
            service.mixin(builder);
        }

        final Cli<Runnable> parser = builder.build();
        final Runnable command = parser.parse(args);

        command.run();
    }

//    public static ClassLoader initClassLoader()
//    {
//        URL[] deps;
//
//        try (InputStream in = Files.newInputStream(Paths.get(RY_DEPENDENCIES_NAME)))
//        {
//            // parse JSON
//
//            // construct equivalent POM
//            // note: implicitly include ry.java dependencies, requires awareness of own version via Package API
//
//            // use Maven / Ivy to construct class loader
//            // including transitive dependencies
//
//            // TODO
//            deps = new URL[0];
//        }
//        catch (IOException ex)
//        {
//            // TODO: log warning
//            deps = new URL[0];
//        }
//
//        return new URLClassLoader(deps, Thread.currentThread().getContextClassLoader());
//    }

    private RyMain()
    {
    }
}