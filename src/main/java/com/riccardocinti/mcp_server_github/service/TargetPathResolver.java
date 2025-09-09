package com.riccardocinti.mcp_server_github.service;

import com.riccardocinti.mcp_server_github.model.CloneRequest;

import java.io.IOException;

public interface TargetPathResolver {

    String resolve(CloneRequest cloneRequest, String basePath) throws IOException;

}
