package com.riccardocinti.mcp_server_github.service.clone;

import java.io.IOException;

public interface TargetPathResolver {

    String resolve(CloneRequest cloneRequest, String basePath) throws IOException;

}
