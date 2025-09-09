package com.riccardocinti.mcp_server_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record CloneRequest(
        @NotBlank(message = "Repository URL is required")
        @JsonProperty("repositoryUrl")
        String repositoryUrl,

        @JsonProperty("branch")
        String branch,

        @JsonProperty("targetDirectory")
        String targetDirectory,

        @JsonProperty("depth")
        Integer depth,

        @Pattern(regexp = "^ghp_[a-zA-Z0-9]{36}$|^github_pat_[a-zA-Z0-9_]+$",
                message = "Invalid GitHub token format")
        @JsonProperty("githubToken")
        String githubToken
) {

    public CloneRequest {
        if (branch == null || branch.isBlank()) {
            branch = "main";
        }
        if (depth == null) {
            depth = 1;
        }
    }

    public boolean isShallowClone() {
        return depth != null && depth > 0;
    }
}
