package com.riccardocinti.mcp_server_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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

        @JsonProperty("errorMessage")
        String errorMessage
) {

    public static CloneResult success(String localPath, String repositoryUrl, String branch,
                                      int filesCount, long directorySizeBytes,
                                      long cloneDurationMs) {
        return new CloneResult(
                true, localPath, repositoryUrl, branch,
                filesCount, directorySizeBytes, cloneDurationMs, null
        );
    }

    public static CloneResult failure(String repositoryUrl, String errorMessage) {
        return new CloneResult(
                false, null, repositoryUrl, null,
                0, 0, 0, errorMessage
        );
    }
}
