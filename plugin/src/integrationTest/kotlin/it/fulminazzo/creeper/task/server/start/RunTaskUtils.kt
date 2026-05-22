package it.fulminazzo.creeper.task.server.start

import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.task.server.InstallServerRunnerTask
import java.io.File

object RunTaskUtils {

    fun copyRunFilesToTaskWorkDir(
        workingDir: File,
        taskWorkingDir: File,
        specification: MinecraftServerSpec
    ) {
        InstallServerRunnerTask.FILE_PATH.toFile().copyTo(
            File(taskWorkingDir, "server-runner.jar"),
            overwrite = true
        )
        listOf("paper-1.8.8.jar", "eula.txt").forEach {
            File(workingDir, "${specification.id}/$it").copyTo(
                File(taskWorkingDir, "${specification.id}/$it"),
                overwrite = true
            )
        }
    }

}