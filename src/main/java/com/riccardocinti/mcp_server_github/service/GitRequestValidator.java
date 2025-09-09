package com.riccardocinti.mcp_server_github.service;

import com.riccardocinti.mcp_server_github.model.CloneRequest;

public interface GitRequestValidator {

    boolean isValid(CloneRequest request);

}
