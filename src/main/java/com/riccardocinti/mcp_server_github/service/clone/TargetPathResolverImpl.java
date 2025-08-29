package com.riccardocinti.mcp_server_github.service.clone;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class TargetPathResolverImpl implements TargetPathResolver {

    @Override
    public String resolve(CloneRequest cloneRequest, String basePath) throws IOException {

        // Extract repository name from URL
        String repoName = extractRepositoryName(cloneRequest.repositoryUrl());
        String timestamp = String.valueOf(System.currentTimeMillis());

        String targetPath;
        if (cloneRequest.targetDirectory() != null && !cloneRequest.targetDirectory().isBlank()) {
            targetPath = Paths.get(basePath, cloneRequest.targetDirectory()).toString();
        } else {
            targetPath = Paths.get(basePath, repoName + "_" + timestamp).toString();
        }

        // Create directory if it doesn't exist
        Path targetDir = Paths.get(targetPath);
        if (Files.exists(targetDir)) {
            if (!Files.isDirectory(targetDir) || !isDirEmpty(targetDir)) {
                throw new IllegalArgumentException("Target directory exists and is not empty: " + targetPath);
            }
        } else {
            Files.createDirectories(targetDir);
        }

        return targetPath;
    }


    private String extractRepositoryName(String repositoryUrl) {
        // Extract repo name from https://github.com/user/repo or https://github.com/user/repo.git
        String[] parts = repositoryUrl.replaceAll("\\.git$", "").split("/");
        return parts[parts.length - 1];
    }

    private boolean isDirEmpty(Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
}
