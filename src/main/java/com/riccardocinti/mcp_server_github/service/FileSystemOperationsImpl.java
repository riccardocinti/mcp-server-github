package com.riccardocinti.mcp_server_github.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileSystemOperationsImpl implements FileSystemOperations {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemOperationsImpl.class);

    @Override
    public File createDirectory(String directoryPath) throws IOException {
        Path workspaceDir = Paths.get(directoryPath);
        if (!exists(directoryPath)) {
            Files.createDirectories(workspaceDir);
            logger.info("Created workspace directory: {}", workspaceDir);
            return workspaceDir.toFile();
        }
        return null;
    }

    @Override
    public void deleteDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (exists(directoryPath)) {
            FileSystemUtils.deleteRecursively(path);
            logger.info("Deleted directory: {}", directoryPath);
        }
    }

    @Override
    public boolean exists(String directoryPath) {
        return Files.exists(Paths.get(directoryPath));
    }

}
