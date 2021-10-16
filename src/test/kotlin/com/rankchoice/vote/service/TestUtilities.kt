package com.rankchoice.vote.service

import kotlin.test.assertEquals

object TestUtilities {

    fun replaceId(target: String, idField: String): String {
        return target.replace(
            Regex("$idField=[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}"),
                "$idField=$idField"
        )
    }

    fun <T> validateIdReplaced(expected: T, actual: T, idField: String) {
        val expectedStr = replaceId(expected.toString(), idField)
        val actualStr = replaceId(actual.toString(), idField)
        assertEquals(expectedStr, actualStr)
    }
}