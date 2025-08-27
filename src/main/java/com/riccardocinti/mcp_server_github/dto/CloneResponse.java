package com.riccardocinti.mcp_server_github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record CloneResponse(
        @JsonProperty("success")
        boolean success,

        @JsonProperty("localPath")
        String localPath,

        @JsonProperty("repositoryUrl")
        String repositoryUrl,

        @JsonProperty("branch")
        String branch,

        @JsonProperty("commitHash")
        String commitHash,

        @JsonProperty("commitMessage")
        String commitMessage,

        @JsonProperty("authorName")
        String authorName,

        @JsonProperty("commitDate")
        Instant commitDate,

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

    public static CloneResponse success(String localPath, String repositoryUrl, String branch,
                                        String commitHash, String commitMessage, String authorName,
                                        Instant commitDate, int filesCount, long directorySizeBytes,
                                        long cloneDurationMs, List<String> projectFiles) {
        return new CloneResponse(
                true, localPath, repositoryUrl, branch, commitHash, commitMessage,
                authorName, commitDate, filesCount, directorySizeBytes, cloneDurationMs,
                projectFiles, null
        );
    }

    public static CloneResponse failure(String repositoryUrl, String errorMessage) {
        return new CloneResponse(
                false, null, repositoryUrl, null, null, null, null, null,
                0, 0, 0, List.of(), errorMessage
        );
    }
}
