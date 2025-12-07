/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.utils.coroutine

import kotlinx.coroutines.*
import yv.tils.utils.logger.Logger

class CoroutineHandler {
    companion object {
        val tasks = mutableMapOf<String, Task>()

        data class Task (
            val taskId: String,
            val taskName: String,
            val task: suspend () -> Unit
        )

        private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private val taskJobs = mutableMapOf<String, Job>()

        /**
         * Launches a task that can run indefinitely or once, with specified delays before and after execution.
         * @param task The suspend function to run as a task.
         * @param taskName Optional name for the task, used for identification.
         * @param beforeDelay Delay before the task starts executing.
         * @param afterDelay Delay after each execution of the task before it runs again.
         * @param isOnce If true, the task will run only once after the beforeDelay.
         * @return The ID of the launched task.
         */
        fun launchTask(
            task: suspend () -> Unit,
            taskName: String? = null,
            beforeDelay: Long = 0L,
            afterDelay: Long = 0L,
            isOnce: Boolean = false
        ): String {
            try {
                if (isOnce) {
                    Logger.Companion.debug("Launching one-time task $taskName with beforeDelay: $beforeDelay")
                    val taskData = launchOnceTaskLogic(task, taskName, beforeDelay)
                    return taskData.taskId
                }

                Logger.Companion.debug("Launching task $taskName with beforeDelay: $beforeDelay and afterDelay: $afterDelay")
                val taskData = launchTaskLogic(task, taskName, beforeDelay, afterDelay)
                return taskData.taskId
            } catch (e: Exception) {
                Logger.Companion.error("Failed to launch task: ${e.message}")
                throw e
            }
        }

        /**
         * Launches a task that runs indefinitely with specified delays before and after each execution.
         * @param task The suspend function to run as a task.
         * @param taskName Optional name for the task, used for identification.
         * @param beforeDelay Delay before the task starts executing.
         * @param afterDelay Delay after each execution of the task before it runs again.
         * @return A Task object containing the task ID, name, and the task function.
         * @throws Exception If a task with the same name already exists.
         */
        private fun launchTaskLogic(task: suspend () -> Unit, taskName: String? = null, beforeDelay: Long, afterDelay: Long): Task {
            if (tasks.containsKey(taskName) && taskName != null) {
                Logger.Companion.debug("There is already a task with the name $taskName")
                throw Exception("There is already a task with the name $taskName")
            }

            val taskName = taskName ?: "yvtils-task-${System.currentTimeMillis()}"

            val taskId = System.currentTimeMillis().toString()

            val job = coroutineScope.launch {
                while (isActive) {
                    delay(beforeDelay)
                    task()
                    delay(afterDelay)
                }
            }
            taskJobs[taskId] = job

            val taskData = Task(
                taskId = taskId,
                taskName = taskName,
                task = task
            )

            tasks[taskName] = taskData
            return taskData
        }

        /**
         * Launches a one-time task that runs after a specified delay.
         * @param task The suspend function to run as a one-time task.
         * @param taskName Optional name for the task, used for identification.
         * @param beforeDelay Delay before the task starts executing.
         * @return A Task object containing the task ID, name, and the task function.
         * @throws Exception If a task with the same name already exists.
         */
        private fun launchOnceTaskLogic(task: suspend () -> Unit, taskName: String? = null, beforeDelay: Long): Task {
            if (tasks.containsKey(taskName) && taskName != null) {
                Logger.Companion.debug("There is already a task with the name $taskName")
                throw Exception("There is already a task with the name $taskName")
            }

            val taskName = taskName ?: "yvtils-task-${System.currentTimeMillis()}"

            val taskId = System.currentTimeMillis().toString()

            val job = coroutineScope.launch {
                delay(beforeDelay)
                task()
                taskJobs.remove(taskId)
                tasks.remove(taskName)
                Logger.Companion.debug("Task $taskName completed and removed")
            }
            taskJobs[taskId] = job

            val taskData = Task(
                taskId = taskId,
                taskName = taskName,
                task = task
            )

            tasks[taskName] = taskData

            return taskData
        }

        /**
         * Cancels a specific task by its ID.
         * @param taskId The ID of the task to cancel.
         */
        fun cancelTask(taskId: String) {
            taskJobs[taskId]?.let { job ->
                job.cancel()
                taskJobs.remove(taskId)
            }
        }

        /**
         * Cancels all active tasks.
         * This will stop all tasks that are currently running.
         */
        fun cancelAllTasks() {
            Logger.Companion.info("Disabling all tasks")
            coroutineScope.coroutineContext.cancelChildren()
        }

        /**
         * Checks if a task is currently active by its ID.
         * @param taskId The ID of the task to check.
         * @return True if the task is active, false otherwise.
         */
        fun isTaskActive(taskId: String): Boolean {
            val isActive = taskJobs[taskId]?.isActive == true

            Logger.Companion.debug("Checking if task $taskId is active: $isActive")
            return isActive
        }
    }
}
