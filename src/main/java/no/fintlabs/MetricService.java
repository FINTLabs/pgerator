package no.fintlabs;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricService {

    private final MeterRegistry meterRegistry;

    private final Map<String, AtomicInteger> gauges = new ConcurrentHashMap<>();

    public MetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void updateMetric(String metric, int value) {
        gauges.computeIfPresent(metric, (key, v) -> {
            v.set(value);
            return v;
        });

        gauges.putIfAbsent(metric, meterRegistry.gauge(metric,
                Collections.emptyList(),
                new AtomicInteger(value)));
    }
}