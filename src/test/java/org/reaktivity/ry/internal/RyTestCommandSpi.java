/**
 * Copyright 2016-2020 The Reaktivity Project
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

import org.reaktivity.ry.RyCommandSpi;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.builder.CliBuilder;

@Command(name = "test")
public final class RyTestCommandSpi implements RyCommandSpi
{
    public static final ThreadLocal<String> TEST_ARGUMENT = new ThreadLocal<>();

    @Override
    public void mixin(
        CliBuilder<Runnable> builder)
    {
        builder.withCommand(RyTestCommand.class);
    }
}
