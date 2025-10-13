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
package io.zell.zdb.raft

import io.atomix.raft.storage.serializer.MetaStoreSerializer
import io.atomix.raft.storage.system.Configuration
import io.atomix.raft.storage.system.MetaStoreRecord
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class MetaStoreReader @JvmOverloads constructor(
    private val metaPath: Path,
    private val configPath: Path,
    private val serializer: MetaStoreSerializer = MetaStoreSerializer()
) {
    fun readConfig(): Configuration {
        try {
            val bytes = Files.readAllBytes(configPath)
            val buffer = ByteBuffer.wrap(bytes)
            return serializer.readConfiguration(buffer)
        } catch (e: IOException) {
            throw UncheckedIOException("Failed to read configuration from $configPath", e)
        }
    }

    fun readMetaStore(): MetaStoreRecord {
        try {
            FileChannel.open(metaPath, StandardOpenOption.READ).use { channel ->
                var count: Int
                do {
                    count = channel.read(serializer.metaByteBuffer())
                } while (count > 0)
            }
        } catch (e: IOException) {
            throw UncheckedIOException("Failed to read meta store record from $metaPath", e)
        }

        return serializer.readRecord()
    }
}