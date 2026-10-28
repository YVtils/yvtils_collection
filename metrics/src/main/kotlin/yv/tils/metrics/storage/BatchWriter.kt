/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.storage

import yv.tils.metrics.data.WriteBatch
import yv.tils.metrics.data.WriteBuffer

/** One worker owns the in-flight batch until acknowledged. New events remain in the buffer. */
class BatchWriter(private val buffer: WriteBuffer, private val commit: (WriteBatch) -> Unit) {
    private var pending: WriteBatch? = null

    fun flush() {
        // At most the failed batch plus the current buffer, never a loop chasing ongoing gameplay.
        repeat(2) {
            val batch = pending ?: buffer.drain()?.also { pending = it } ?: return
            commit(batch)
            pending = null
        }
    }
}
