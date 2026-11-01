package me.weishu.kernelsu.ui.component.kernel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BootSlotResourcesTest {
    @Test
    fun `every locale has the same keys and format placeholders`() {
        val root = sequenceOf(
            File("src/main/res"),
            File("app/src/main/res"),
        ).first(File::isDirectory)
        val locales = listOf(
            "values",
            "values-zh-rCN",
            "values-zh-rTW",
        )
        val expected = parseStrings(File(root, "values/boot_slot.xml"))
        assertTrue(expected.containsKey("boot_slot_source"))
        locales.forEach { locale ->
            val actual = parseStrings(File(root, "$locale/boot_slot.xml"))
            assertEquals("keys in $locale", expected.keys, actual.keys)
            expected.forEach { (key, text) ->
                assertEquals("placeholders in $locale/$key", placeholders(text), placeholders(actual.getValue(key)))
            }
        }
    }

    private fun parseStrings(file: File): Map<String, String> {
        assertTrue("missing ${file.path}", file.isFile)
        return Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(file.readText())
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private fun placeholders(value: String): List<String> =
        Regex("""%(?:\d+\$)?[a-zA-Z]""").findAll(value).map { it.value }.sorted().toList()
}
