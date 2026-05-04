package it.fulminazzo.creeper

import kotlin.test.Test
import kotlin.test.assertNotNull

class CreeperPluginTest {

    @Test
    fun `test buildconfig plugin works`() {
        assertNotNull(ProjectInfo.GROUP)
        assertNotNull(ProjectInfo.NAME)
        assertNotNull(ProjectInfo.VERSION)
    }

}
