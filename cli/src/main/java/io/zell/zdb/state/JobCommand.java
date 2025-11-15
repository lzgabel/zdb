/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zell.zdb.state;

import io.zell.zdb.JsonPrinter;
import io.zell.zdb.state.job.JobState;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@Command(
    name = "jobs",
    mixinStandardHelpOptions = true,
    description = "Print's information about running jobs")
public class JobCommand implements Callable<Integer> {

  @Spec private CommandSpec spec;

  @Option(
      names = {"-p", "--path"},
      paramLabel = "PARTITION_PATH",
      description = "The path to the partition data (either runtime or snapshot in partition dir)",
      required = true,
      scope = ScopeType.INHERIT)
  private Path partitionPath;

  @Override
  public Integer call() {
    spec.commandLine().usage(System.out);
    return 0;
  }

  @Command(name = "key", description = "Show all job details")
  public int list(
      @Parameters(
              paramLabel = "KEY",
              description = "The element instance key or process instance key",
              arity = "1")
          final long key) {

    new JsonPrinter()
        .surround(
            (printer) ->
                new JobState(partitionPath)
                    .listJobs(
                        jobDetails ->
                            jobDetails.getJobRecord().getElementInstanceKey() == key
                                || jobDetails.getJobRecord().getProcessInstanceKey() == key,
                        (jobKey, valueJson) -> printer.accept(valueJson)));
    return 0;
  }
}
