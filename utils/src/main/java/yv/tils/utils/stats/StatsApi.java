/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */
package yv.tils.utils.stats;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/** Optional stats bridge in the single shared utils runtime; never shade it into features. */
public final class StatsApi {
    private static final Pattern MODULE_NAME = Pattern.compile("[A-Za-z][A-Za-z0-9_.-]{0,63}");
    private static final Pattern KEY = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,127}");
    private static final AtomicReference<Backend> BACKEND = new AtomicReference<>();

    private StatsApi() {}

    /** Implemented by stats, not by consumers. Only JDK/shared-runtime types cross the boundary. */
    public interface Backend {
        boolean isCollecting();
        boolean publish(String module, String key, String help, ModuleStats.MetricType type, Object value);
        void removeModule(String module);
    }

    /** Does not collect data or require stats to be installed; retains no metric values. */
    public static ModuleStats forModule(String moduleName) {
        Objects.requireNonNull(moduleName, "moduleName");
        if (!MODULE_NAME.matcher(moduleName).matches()) {
            throw new IllegalArgumentException("Module name must match [A-Za-z][A-Za-z0-9_.-]{0,63}");
        }
        return new Publisher(moduleName);
    }

    /** Lifecycle hook for stats. A second backend must not silently replace the first. */
    public static void installBackend(Backend backend) {
        if (!BACKEND.compareAndSet(null, Objects.requireNonNull(backend, "backend"))) {
            throw new IllegalStateException("A stats backend is already installed");
        }
    }

    /** Identity-checked so a stale module cannot detach a newer backend. */
    public static void uninstallBackend(Backend backend) {
        BACKEND.compareAndSet(Objects.requireNonNull(backend, "backend"), null);
    }

    private static final class Publisher implements ModuleStats {
        private final String module;
        private volatile boolean closed;

        private Publisher(String module) { this.module = module; }
        @Override public String moduleName() { return module; }

        @Override public boolean isEnabled() {
            Backend backend = BACKEND.get();
            return !closed && backend != null && backend.isCollecting();
        }

        private synchronized boolean publish(String key, String help, MetricType type, Object value) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(help, "help");
            if (!KEY.matcher(key).matches()) throw new IllegalArgumentException("Invalid module metric key: " + key);
            Backend backend = BACKEND.get();
            return !closed && backend != null && backend.isCollecting()
                && backend.publish(module, key, help, type, value);
        }

        @Override public boolean incrementCounter(String key, String help, long delta) {
            if (delta < 0) throw new IllegalArgumentException("Counter delta must be non-negative");
            return publish(key, help, MetricType.COUNTER, delta);
        }
        @Override public boolean setGauge(String key, String help, long value) {
            return publish(key, help, MetricType.GAUGE, value);
        }
        @Override public boolean setGauge(String key, String help, double value) {
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Gauge must be finite");
            return publish(key, help, MetricType.GAUGE, value);
        }
        @Override public boolean setString(String key, String help, String value) {
            return publish(key, help, MetricType.STRING, Objects.requireNonNull(value, "value"));
        }
        @Override public boolean setList(String key, String help, List<String> values) {
            return publish(key, help, MetricType.STRING_LIST, List.copyOf(values));
        }
        @Override public boolean setTimestamp(String key, String help, long value) {
            return publish(key, help, MetricType.TIMESTAMP, value);
        }
        @Override public boolean observeHistogram(String key, String help, double value) {
            if (!Double.isFinite(value) || value < 0) {
                throw new IllegalArgumentException("Histogram observations must be finite and non-negative");
            }
            return publish(key, help, MetricType.HISTOGRAM, value);
        }
        @Override public synchronized void close() {
            if (closed) return;
            closed = true;
            Backend backend = BACKEND.get();
            if (backend != null) backend.removeModule(module);
        }
    }
}
