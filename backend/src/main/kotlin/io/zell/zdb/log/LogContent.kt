/*
 * Copyright © 2021 Christopher Kujawa (zelldon91@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zell.zdb.log

import io.camunda.zeebe.protocol.record.ValueType
import io.zell.zdb.log.records.ApplicationRecord
import io.zell.zdb.log.records.PersistedRecord
import io.zell.zdb.log.records.Record

class LogContent {
    val records = mutableListOf<PersistedRecord>()

    override fun toString(): String {
        return "{ \"records\": [${records.joinToString()}] } "
    }

    fun asDotFile(): String {
        val content = StringBuilder("digraph log {")
            .append(System.lineSeparator())
            .append("rankdir=\"RL\"")
            .append(";")
            .append(System.lineSeparator())

        records
            .filterIsInstance<ApplicationRecord>()
            .flatMap { it.entries }
            .forEach{
                addEventAsDotNode(it, content)
            }
        content.append(System.lineSeparator())
            .append("}")
        return content.toString()
    }

    private fun addEventAsDotNode(
        entry: Record,
        content: java.lang.StringBuilder
    ) {
        content.append(entry.position)
            .append(" [label=\"")
            .append("\\n").append(entry.recordType)
            .append("\\n").append(entry.valueType.name)
            .append("\\n").append(entry.intent.name())

        if (entry.valueType == ValueType.PROCESS_INSTANCE) {
            val piRelatedValue = entry.recordValue.piRelatedValue
            piRelatedValue.bpmnElementType?.let {
                content.append("\\n").append(it)
            }

            piRelatedValue.processInstanceKey?.let {
                content.append("\\nPI Key: ").append(it)
            }

            piRelatedValue.processDefinitionKey?.let {
                content.append("\\nPD Key: ").append(it)
            }
        }

        content
            .append("\\nKey: ").append(entry.key)
            .append("\"]")
            .append(";")
            .append(System.lineSeparator())
        if (entry.sourceRecordPosition != -1L) {
            content.append(entry.position)
                .append(" -> ")
                .append(entry.sourceRecordPosition)
                .append(";")
                .append(System.lineSeparator())
        }
    }
}
