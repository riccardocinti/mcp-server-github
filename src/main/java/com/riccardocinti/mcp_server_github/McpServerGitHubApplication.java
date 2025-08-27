package com.riccardocinti.mcp_server_github;

import com.riccardocinti.mcp_server_github.tools.CloneRepositoryService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class McpServerGitHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerGitHubApplication.class, args);
	}

	@Bean
	public List<ToolCallback> githubTools(CloneRepositoryService cloneRepositoryService) {
		return List.of(ToolCallbacks.from(cloneRepositoryService));
	}

}
