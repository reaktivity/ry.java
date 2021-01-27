/**
 * Copyright 2016-2021 The Reaktivity Project
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

import static org.reaktivity.ry.internal.RyTestCommandSpi.TEST_ARGUMENT;

import org.reaktivity.ry.RyCommand;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;

@Command(name = "test", hidden = true)
public final class RyTestCommand extends RyCommand
{
    @Arguments(description = "argument")
    public String argument = "arg";

    @Override
    public void run()
    {
        if ("throw".equals(argument))
        {
            throw new RuntimeException();
        }

        TEST_ARGUMENT.set(argument);
    }
}

