/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */
package yv.tils.utils.stats;

import java.util.List;

/**
 * Event-driven, module-scoped usage telemetry. Obtain one publisher per module from
 * {@link StatsApi#forModule(String)} and close it when the module disables.
 *
 * <p>Updates return false and are discarded (never buffered) when stats is absent,
 * disabled, not opted in, or a registration limit/type conflict prevents publication.
 * Do not publish player identities, IP addresses, world seeds or other personal data.
 * All methods are thread-safe; callers must perform any Bukkit reads on the server thread.
 * Long-lived publishers follow backend/consent changes automatically.
 */
public interface ModuleStats extends AutoCloseable {
    enum MetricType { COUNTER, GAUGE, STRING, STRING_LIST, TIMESTAMP, HISTOGRAM }

    String moduleName();
    boolean isEnabled();
    boolean incrementCounter(String key, String help, long delta);
    boolean setGauge(String key, String help, long value);
    boolean setGauge(String key, String help, double value);
    boolean setString(String key, String help, String value);
    boolean setList(String key, String help, List<String> values);
    boolean setTimestamp(String key, String help, long timestampMillis);
    boolean observeHistogram(String key, String help, double value);

    /** Permanently closes this publisher and removes its module's published series. */
    @Override void close();
}
