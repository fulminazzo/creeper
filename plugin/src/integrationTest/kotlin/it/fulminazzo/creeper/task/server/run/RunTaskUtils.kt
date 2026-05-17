package it.fulminazzo.creeper.task.server.run

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import java.io.File

object RunTaskUtils {

    fun copyRunFilesToTaskWorkDir(
        workingDir: File,
        taskWorkingDir: File,
        specification: MinecraftServerSpec
    ) {
        File("build/resources/main/${ProjectInfo.NAME}/server-runner.jar").copyTo(
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