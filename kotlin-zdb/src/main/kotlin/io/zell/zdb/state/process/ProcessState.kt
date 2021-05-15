package io.zell.zdb.state.process

import io.camunda.zeebe.engine.state.ZeebeDbState
import io.zell.zdb.db.readonly.transaction.ReadonlyTransactionDb
import java.nio.file.Path

class ProcessState(statePath: Path) {

    private var zeebeDbState: ZeebeDbState

    init {
        val readonlyDb = ReadonlyTransactionDb.openReadonlyDb(statePath)
        zeebeDbState = ZeebeDbState(readonlyDb, readonlyDb.createContext())
    }

    fun listProcesses(): List<ProcessMeta> {
        return zeebeDbState
            .processState
            .processes.map { ProcessMeta(it) }
    }

    fun processDetails(processDefinitionKey : Long): ProcessDetails {
        return ProcessDetails(zeebeDbState
            .processState
            .getProcessByKey(processDefinitionKey))
    }
}
