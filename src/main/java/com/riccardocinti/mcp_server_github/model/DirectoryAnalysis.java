package com.riccardocinti.mcp_server_github.model;

import java.util.List;

public record DirectoryAnalysis(int fileCount, long totalSize, List<String> projectFiles) {
}
