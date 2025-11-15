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

import kotlinx.serialization.Serializable

@Serializable
class JobDetails constructor(
    val deadline: Long,
    val timeout: Int,
    val worker: String,
    val retries: Int,
    val recurringTime: Long,
    val type: String,
    val customHeaders: Map<String, String>,
    val variables: String,
    val errorMessage: String,
    val errorCode: String,
    val bpmnProcessId: String,
    val processDefinitionKey: Long,
    val processDefinitionVersion: Long,
    val processInstanceKey: Long,
    val jobKind: String,
    val elementInstanceKey: Long,
    val elementId: String,
    val tenantId: String,
) {}
