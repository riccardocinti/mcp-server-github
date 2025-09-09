package com.riccardocinti.mcp_server_github.service;

import java.io.File;
import java.io.IOException;

public interface FileSystemOperations {

    File createDirectory(String directoryPath) throws IOException;
    void deleteDirectory(String directoryPath) throws IOException;
    boolean exists(String directoryPath);

}
