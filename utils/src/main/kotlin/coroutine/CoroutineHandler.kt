package coroutine

import kotlinx.coroutines.*
import logger.Logger

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

        fun launchTask(
            task: suspend () -> Unit,
            taskName: String,
            beforeDelay: Long = 0L,
            afterDelay: Long = 0L
        ): String {
            Logger.debug("Launching task $taskName with beforeDelay: $beforeDelay and afterDelay: $afterDelay")
            val taskData = launchTaskLogic(task, taskName, beforeDelay, afterDelay)
            return taskData.taskId
        }

        private fun launchTaskLogic(task: suspend () -> Unit, taskName: String, beforeDelay: Long, afterDelay: Long): Task {
            if (tasks.containsKey(taskName)) {
                Logger.debug("There is already a task with the name $taskName")
                throw Exception("There is already a task with the name $taskName")
            }

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

        fun cancelTask(taskId: String) {
            taskJobs[taskId]?.let { job ->
                job.cancel()
                taskJobs.remove(taskId)
            }
        }

        fun cancelAllTasks() {
            Logger.info("Disabling all tasks")
            coroutineScope.coroutineContext.cancelChildren()
        }

        fun isTaskActive(taskId: String): Boolean {
            val isActive = taskJobs[taskId]?.isActive == true

            Logger.debug("Checking if task $taskId is active: $isActive")
            return isActive
        }
    }
}