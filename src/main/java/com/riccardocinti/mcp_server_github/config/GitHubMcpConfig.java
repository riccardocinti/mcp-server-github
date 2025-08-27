package com.riccardocinti.mcp_server_github.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mcp.github")
public class GitHubMcpConfig {

    private String workspaceDirectory = System.getProperty("java.io.tmpdir") + "/mcp-github-workspace";
    private int maxConcurrentClones = 5;
    private long timeoutSeconds = 300; // 5 minutes
    private boolean cleanupOnError = true;
    private int maxRepositorySize = 500; // MB

    // Getters and Setters
    public String getWorkspaceDirectory() {
        return workspaceDirectory;
    }

    public void setWorkspaceDirectory(String workspaceDirectory) {
        this.workspaceDirectory = workspaceDirectory;
    }

    public int getMaxConcurrentClones() {
        return maxConcurrentClones;
    }

    public void setMaxConcurrentClones(int maxConcurrentClones) {
        this.maxConcurrentClones = maxConcurrentClones;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isCleanupOnError() {
        return cleanupOnError;
    }

    public void setCleanupOnError(boolean cleanupOnError) {
        this.cleanupOnError = cleanupOnError;
    }

    public int getMaxRepositorySize() {
        return maxRepositorySize;
    }

    public void setMaxRepositorySize(int maxRepositorySize) {
        this.maxRepositorySize = maxRepositorySize;
    }

}
