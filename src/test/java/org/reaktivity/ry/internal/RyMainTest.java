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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.reaktivity.ry.RyCommand;
import org.reaktivity.ry.RyCommandSpi;

import io.airlift.airline.Arguments;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Command;

public class RyMainTest
{
    private static final ThreadLocal<String> TEST_COMMAND = new ThreadLocal<>();

    @Test
    public void shouldInvokeTestCommandWithDefaultArgument()
    {
        RyMain.main(new String[] { "test" });

        assertEquals("arg", TEST_COMMAND.get());
    }

    @Test
    public void shouldInvokeTestCommandWithOverriddenArgument()
    {
        RyMain.main(new String[] { "test", "arg1" });

        assertEquals("arg1", TEST_COMMAND.get());
    }

    public static final class RyTestSpi implements RyCommandSpi
    {
        @Override
        public void mixin(
            CliBuilder<Runnable> builder)
        {
            builder.withCommand(RyTest.class);
        }
    }

    @Command(name = "test")
    public static final class RyTest extends RyCommand
    {
        @Arguments(description = "argument")
        public String argument = "arg";

        @Override
        public void run()
        {
            TEST_COMMAND.set(argument);
        }
    }
}
