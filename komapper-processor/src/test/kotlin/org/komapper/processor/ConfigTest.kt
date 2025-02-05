package org.komapper.processor

import org.komapper.core.CamelToLowerSnakeCase
import org.komapper.core.Implicit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfigTest {
    @Test
    fun empty() {
        val options = emptyMap<String, String>()
        val config = Config.create(options)
        assertEquals("_", config.prefix)
        assertEquals("", config.suffix)
        assertEquals(EnumStrategy.Name, config.enumStrategy)
        assertEquals(CamelToLowerSnakeCase, config.namingStrategy)
        assertEquals(false, config.alwaysQuote)
    }

    @Test
    fun prefix() {
        val options = mapOf("komapper.prefix" to "A")
        val config = Config.create(options)
        assertEquals("A", config.prefix)
    }

    @Test
    fun suffix() {
        val options = mapOf("komapper.suffix" to "A")
        val config = Config.create(options)
        assertEquals("A", config.suffix)
    }

    @Test
    fun enumStrategy() {
        val options = mapOf("komapper.enumStrategy" to "ordinal")
        val config = Config.create(options)
        assertEquals(EnumStrategy.Ordinal, config.enumStrategy)
    }

    @Test
    fun enumStrategy_error() {
        val options = mapOf("komapper.enumStrategy" to "unknown")
        val e = assertFailsWith<IllegalStateException> {
            Config.create(options)
        }
        assertEquals("'unknown' is illegal value as a komapper.enumStrategy option.", e.message)
    }

    @Test
    fun namingStrategy() {
        val options = mapOf("komapper.namingStrategy" to "implicit")
        val config = Config.create(options)
        assertEquals(Implicit, config.namingStrategy)
    }

    @Test
    fun namingStrategy_error() {
        val options = mapOf("komapper.namingStrategy" to "unknown")
        val e = assertFailsWith<IllegalStateException> {
            Config.create(options)
        }
        assertEquals("'unknown' is illegal value as a komapper.namingStrategy option.", e.message)
    }

    @Test
    fun alwaysQuote() {
        val options = mapOf("komapper.alwaysQuote" to "true")
        val config = Config.create(options)
        assertEquals(true, config.alwaysQuote)
    }
}
