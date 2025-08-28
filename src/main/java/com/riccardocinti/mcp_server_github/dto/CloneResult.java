package com.riccardocinti.mcp_server_github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record CloneResult(
        @JsonProperty("success")
        boolean success,

        @JsonProperty("localPath")
        String localPath,

        @JsonProperty("repositoryUrl")
        String repositoryUrl,

        @JsonProperty("branch")
        String branch,

        @JsonProperty("filesCount")
        int filesCount,

        @JsonProperty("directorySize")
        long directorySizeBytes,

        @JsonProperty("cloneDurationMs")
        long cloneDurationMs,

        @JsonProperty("projectFiles")
        List<String> projectFiles,

        @JsonProperty("errorMessage")
        String errorMessage
) {

    public static CloneResult success(String localPath, String repositoryUrl, String branch,
                                      int filesCount, long directorySizeBytes,
                                      long cloneDurationMs, List<String> projectFiles) {
        return new CloneResult(
                true, localPath, repositoryUrl, branch,
                filesCount, directorySizeBytes, cloneDurationMs,
                projectFiles, null
        );
    }

    public static CloneResult failure(String repositoryUrl, String errorMessage) {
        return new CloneResult(
                false, null, repositoryUrl, null,
                0, 0, 0, List.of(), errorMessage
        );
    }
}
