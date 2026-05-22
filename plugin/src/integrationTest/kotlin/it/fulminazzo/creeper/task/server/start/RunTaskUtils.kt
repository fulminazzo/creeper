package it.fulminazzo.creeper.task.server.start

import it.fulminazzo.creeper.task.server.InstallServerRunnerTask
import java.io.File

object RunTaskUtils {

    fun copyRunFilesToTaskWorkDir(workingDir: File, taskWorkingDir: File) {
        InstallServerRunnerTask.FILE_PATH.toFile().copyTo(
            File(taskWorkingDir, "server-runner.jar"),
            overwrite = true
        )
        listOf("paper-1.8.8.jar", "eula.txt").forEach { file ->
            File(workingDir, file).copyTo(
                File(taskWorkingDir, file),
                overwrite = true
            )
        }
    }

}