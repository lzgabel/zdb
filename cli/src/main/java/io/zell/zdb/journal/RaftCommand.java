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
package io.zell.zdb.journal;

import io.zell.zdb.raft.RaftStatus;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@SuppressWarnings("unused")
@CommandLine.Command(
    name = "raft",
    mixinStandardHelpOptions = true,
    description = "Allows to inspect the raft metadata via sub commands")
public class RaftCommand implements Callable<Integer> {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  public enum Format {
    JSON,
    TABLE,
  }

  @CommandLine.Option(
      names = {"-p", "--path"},
      paramLabel = "PARTITION_PATH",
      description = "The path to the partition data, should end with the partition id.",
      required = true,
      scope = CommandLine.ScopeType.INHERIT)
  private Path partitionPath;

  @CommandLine.Option(
      names = {"-f", "--format"},
      description =
          "Print's the raft metadata in the specified format, defaults to json. Possible values: [ ${COMPLETION-CANDIDATES} ]",
      defaultValue = "JSON",
      scope = CommandLine.ScopeType.INHERIT)
  private Format format;

  @CommandLine.Command(name = "status", description = "Print's the metadata of the Raft server")
  public int status() {
    if (this.format == Format.JSON) {
      System.out.println(new RaftStatus(this.partitionPath).detailsAsJson());
    } else {
      printDetailsAsTable();
    }
    return 0;
  }

  private void printDetailsAsTable() {
    final var status = new RaftStatus(this.partitionPath).details();
    System.out.printf(
        """
            --------------------------------------------------------------
            Raft Status for partition '%s':
            --------------------------------------------------------------
            Meta Store:
                Term:                    %d
                Last Flushed Index:      %d
                Commit Index:            %d
                Voted For:               %s%n""",
        this.partitionPath.getFileName(),
        status.meta().term(),
        status.meta().lastFlushedIndex(),
        status.meta().commitIndex(),
        status.meta().votedFor());
    System.out.printf(
        """
            --------------------------------------------------------------
            Configuration:
                Index:                   %d
                Term:                    %d
                Time:                    %d
                Force:                   %b
                Requires Join Consensus: %b
                New Members:             %s
                Old Members:             %s
            --------------------------------------------------------------""",
        status.config().index(),
        status.config().term(),
        status.config().time(),
        status.config().force(),
        status.config().requiresJointConsensus(),
        formatMembers(status.config().newMembers()),
        formatMembers(status.config().oldMembers()));
  }

  @NotNull
  private static String formatMembers(final Collection<RaftStatus.RaftMemberDetails> members) {
    if (members.isEmpty()) {
      return "[]";
    }

    return "["
        + System.lineSeparator()
        + members.stream()
            .map(
                m ->
                    """
            \t\tId: %s, Type: %s, Hash: %d, Updated: %s"""
                        .formatted(m.id(), m.type(), m.hash(), m.lastUpdated()))
            .collect(Collectors.joining(System.lineSeparator()))
        + System.lineSeparator()
        + "    ]";
  }

  @Override
  public Integer call() {
    this.spec.commandLine().usage(System.out);
    return 0;
  }
}
