package com.riccardocinti.mcp_server_github.service.analysis;

import java.io.IOException;

public interface DirectoryAnalyzer {

    DirectoryAnalysis analyze(String path) throws IOException;

}
