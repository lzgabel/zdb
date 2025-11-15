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
package io.zell.zdb.state.job

import io.camunda.zeebe.db.impl.ZeebeDbConstants
import io.camunda.zeebe.protocol.ZbColumnFamilies
import io.zell.zdb.state.ZeebeDbReader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.agrona.concurrent.UnsafeBuffer
import java.nio.file.Path
import java.util.function.Predicate


@Serializable
class Record(val key: Long, val state: String, val record: JobDetails)

@Serializable
class State(val state: String)

class JobState(statePath: Path) {

    private var json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private var zeebeDbReader: ZeebeDbReader

    init {
        zeebeDbReader = ZeebeDbReader(statePath)
    }

    fun listJobs(predicate: Predicate<JobRecord>, visitor: ZeebeDbReader.JsonValueWithKeyPrefixVisitor) {
        zeebeDbReader.visitDBWithPrefix(ZbColumnFamilies.JOBS) { key: ByteArray, value: String ->
            val jobDetails = json.decodeFromString<JobRecord>(value)
            val keyBuffer = UnsafeBuffer(key)
            val jobKey = keyBuffer.getLong(keyBuffer.capacity() - Long.SIZE_BYTES, ZeebeDbConstants.ZB_DB_BYTE_ORDER)
            if (predicate.test(jobDetails)) {
                val state = zeebeDbReader.getValueAsJson(ZbColumnFamilies.JOB_STATES, jobKey)
                val data = Record(
                    jobKey,
                    state,
                    jobDetails.jobRecord
                )
                visitor.visit(key, json.encodeToString(data))
            }
        }
    }

}
