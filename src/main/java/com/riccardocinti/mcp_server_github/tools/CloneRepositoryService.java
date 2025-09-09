package com.riccardocinti.mcp_server_github.tools;

import com.riccardocinti.mcp_server_github.model.CloneRequest;
import com.riccardocinti.mcp_server_github.model.CloneResult;
import com.riccardocinti.mcp_server_github.service.GitHubCloneService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CloneRepositoryService {

    private final GitHubCloneService gitHubCloneService;

    public CloneRepositoryService(GitHubCloneService gitHubCloneService) {
        this.gitHubCloneService = gitHubCloneService;
    }

    @Tool(
            name = "clone_repository",
            description = "Clone a GitHub repository to local filesystem. Provide either repositoryUrl OR owner+repo."
    )
    public CloneResult cloneRepository(
            @Parameter(name = "repositoryUrl", description = "Full GitHub repository URL (e.g., 'https://github.com/spring-projects/spring-boot.git'). Optional if owner and repo are provided.", required = false)
            String repositoryUrl,

            @Parameter(name = "owner", description = "GitHub repository owner/organization (e.g., 'spring-projects'). Optional if repositoryUrl is provided.", required = false)
            String owner,

            @Parameter(name = "repo", description = "GitHub repository name (e.g., 'spring-boot'). Optional if repositoryUrl is provided.", required = false)
            String repo,

            @Parameter(name = "branch", description = "Git branch to clone (default: 'main')", required = false)
            String branch,

            @Parameter(name = "targetDirectory", description = "Local directory path where to clone the repository (e.g., '/tmp/spring-boot')", required = false)
            String targetDirectory,

            @Parameter(name = "depth", description = "Clone depth for shallow cloning (default: 1 for shallow clone)", required = false)
            Integer depth,

            @Parameter(name = "githubToken", description = "GitHub personal access token for private repositories", required = false)
            String githubToken
    ) {

        // Build repository URL if not provided
        String finalRepositoryUrl = repositoryUrl;
        if (finalRepositoryUrl == null || finalRepositoryUrl.isBlank()) {
            if (owner == null || repo == null) {
                throw new IllegalArgumentException("Either 'repositoryUrl' or both 'owner' and 'repo' must be provided");
            }
            finalRepositoryUrl = String.format("https://github.com/%s/%s.git", owner, repo);
        }

        // Set defaults
        if (targetDirectory == null || targetDirectory.isBlank()) {
            String repoName = extractRepoName(finalRepositoryUrl);
            targetDirectory = "/tmp/" + repoName;
        }

        // Create the request object
        CloneRequest cloneRequest = new CloneRequest(
                finalRepositoryUrl,
                branch,
                targetDirectory,
                depth,
                githubToken
        );

        return gitHubCloneService.cloneRepository(cloneRequest);
    }

    private String extractRepoName(String repositoryUrl) {
        // Extract repo name from URL like "https://github.com/owner/repo.git"
        String[] parts = repositoryUrl.replaceAll("\\.git$", "").split("/");
        return parts[parts.length - 1];
    }
}
