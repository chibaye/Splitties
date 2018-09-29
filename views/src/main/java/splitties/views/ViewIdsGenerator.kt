/*
 * Copyright (c) 2018. Louis Cognault Ayeva Derman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package splitties.views

import android.os.Build.VERSION.SDK_INT
import android.view.View
import splitties.mainthread.isMainThread
import java.util.concurrent.atomic.AtomicInteger

/**
 * 1. Generates a View id that doesn't collide with AAPT generated ones (`R.id.xxx`),
 * more efficiently than [View.generateViewId] if called on the main thread.
 * 2. Assigns it to the View.
 * 3. Disables instance state saving for increased efficiency.
 * as state can't be restored to a view that has its id changing across Activity restarts.
 * 4. Returns the generated id.
 *
 * Note that unlike [View.generateViewId], this method is backwards compatible, below API 17.
 */
fun View.assignAndGetGeneratedId(): Int = generateViewId().also { generatedId ->
    id = generatedId
    isSaveEnabled = false // New id will be generated, so can't restore saved state.
}

/**
 * Generates a view id that doesn't clash with AAPT generated ids.
 * Specially optimized for usage on main thread to be synchronization free.
 * Backwards compatible below API 17.
 */
@Suppress("NOTHING_TO_INLINE") // Used only once in this file.
private inline fun generateViewId(): Int = when {
    isMainThread -> mainThreadLastGeneratedId.also {
        // Decrement here to avoid any collision with other generated ids which are incremented.
        mainThreadLastGeneratedId = (if (it == 1) aaptIdsStart else it) - 1
    }
    SDK_INT >= 17 -> View.generateViewId()
    else -> generatedViewIdCompat()
}

/** aapt-generated IDs have the high byte nonzero. Clamp to the range under that. */
private const val aaptIdsStart = 0x00FFFFFF
private var mainThreadLastGeneratedId = aaptIdsStart - 1

private val nextGeneratedId = AtomicInteger(1)
private fun generatedViewIdCompat(): Int {
    while (true) {
        val result = nextGeneratedId.get()
        // aapt-generated IDs have the high byte nonzero. Clamp to the range under that.
        var newValue = result + 1
        if (newValue > aaptIdsStart) newValue = 1 // Roll over to 1, not 0.
        if (nextGeneratedId.compareAndSet(result, newValue)) {
            return result
        }
    }
}
