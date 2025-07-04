/*
 * Copyright © 2022-2025 StreamNative Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.oxia.client.perf.ycsb.output;

public final class Outputs {

    public static Output createLogOutput(boolean pretty) {
        return new LogOutput(pretty);
    }

    public static Output createOutput(OutputTypes types, OutputOptions options) {
        return switch (types) {
            case LOG -> new LogOutput(options.pretty());
            case PULSAR -> {
                final PulsarOutputOptions pulsarOutputOptions = options.pulsarOptions();
                if (!pulsarOutputOptions.validate()) {
                    throw new IllegalArgumentException("unexpected pulsar options");
                }
                yield new PulsarOutput(options.pulsarOptions());
            }
        };
    }
}
