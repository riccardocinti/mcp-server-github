package com.riccardocinti.mcp_server_github.service;

import com.riccardocinti.mcp_server_github.model.DirectoryAnalysis;

import java.io.IOException;

public interface DirectoryAnalyzer {

    DirectoryAnalysis analyze(String path) throws IOException;

}
