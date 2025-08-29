package com.riccardocinti.mcp_server_github.tools;

import com.riccardocinti.mcp_server_github.service.clone.CloneRequest;
import com.riccardocinti.mcp_server_github.service.clone.CloneResult;
import com.riccardocinti.mcp_server_github.service.GitHubCloneService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CloneRepositoryService {

    private final GitHubCloneService gitHubCloneService;

    public CloneRepositoryService(GitHubCloneService gitHubCloneService) {
        this.gitHubCloneService = gitHubCloneService;
    }

    @Tool(name = "clone_repository", description = "Clone a GitHub repository to local filesystem")
    public CloneResult cloneRepository(CloneRequest cloneRequest) {
        return gitHubCloneService.cloneRepository(cloneRequest);
    }
}
