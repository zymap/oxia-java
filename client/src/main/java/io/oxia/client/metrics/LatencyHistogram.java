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
package io.oxia.client.metrics;

import com.google.common.collect.Lists;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.Meter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LatencyHistogram {

    private static final List<Double> latencyHistogramBuckets =
            Lists.newArrayList(
                    .0005, .001, .0025, .005, .01, .025, .05, .1, .25, .5, 1.0, 2.5, 5.0, 10.0, 30.0, 60.0);

    private static final double NANOS = TimeUnit.SECONDS.toNanos(1);

    private final Attributes successAttributes;

    private final Attributes failedAttributes;
    private final DoubleHistogram histogram;

    private LatencyHistogram() {
        successAttributes = null;
        failedAttributes = null;
        histogram = null;
    }

    LatencyHistogram(
            Meter meter, String name, String description, String namespace, Attributes attributes) {
        DoubleHistogramBuilder builder =
                meter
                        .histogramBuilder(name)
                        .setDescription(description)
                        .setUnit(Unit.Seconds.toString())
                        .setExplicitBucketBoundariesAdvice(latencyHistogramBuckets);

        if (namespace != null) {
            attributes = attributes.toBuilder().put("oxia.namespace", namespace).build();
        }

        successAttributes = attributes.toBuilder().put("oxia.response.status", "success").build();
        failedAttributes = attributes.toBuilder().put("oxia.response.status", "failed").build();
        this.histogram = builder.build();
    }

    private LatencyHistogram(
            DoubleHistogram histogram, Attributes successAttributes, Attributes failedAttributes) {
        this.histogram = histogram;
        this.successAttributes = successAttributes;
        this.failedAttributes = failedAttributes;
    }

    /** Create a new histograms that inherits the old histograms attributes and adds new ones. */
    public LatencyHistogram withAttributes(Attributes attributes) {
        return new LatencyHistogram(
                histogram,
                successAttributes.toBuilder().putAll(attributes).build(),
                failedAttributes.toBuilder().putAll(attributes).build());
    }

    public void recordSuccess(long latencyNanos) {
        histogram.record(latencyNanos / NANOS, successAttributes);
    }

    public void recordFailure(long latencyNanos) {
        histogram.record(latencyNanos / NANOS, failedAttributes);
    }
}
