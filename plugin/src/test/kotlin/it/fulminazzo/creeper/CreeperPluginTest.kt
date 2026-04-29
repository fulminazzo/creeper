package it.fulminazzo.creeper

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'it.fulminazzo.creeper' plugin.
 */
class CreeperPluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("it.fulminazzo.creeper")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
    }
}
