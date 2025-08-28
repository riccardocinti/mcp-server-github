package com.riccardocinti.mcp_server_github.service;

import com.riccardocinti.mcp_server_github.config.GitHubMcpConfig;
import com.riccardocinti.mcp_server_github.dto.CloneRequest;
import com.riccardocinti.mcp_server_github.dto.CloneResult;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class GitHubCloneService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubCloneService.class);

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
            "^https://github\\.com/[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+(?:\\.git)?/?$"
    );

    private static final List<String> PROJECT_INDICATOR_FILES = List.of(
            "pom.xml", "build.gradle", "package.json", "requirements.txt",
            "Dockerfile", "docker-compose.yml", "README.md", ".gitignore"
    );

    private final GitHubMcpConfig properties;
    private final Semaphore cloneSemaphore;

    public GitHubCloneService(GitHubMcpConfig properties) {
        this.properties = properties;
        this.cloneSemaphore = new Semaphore(properties.getMaxConcurrentClones());
        initializeWorkspaceDirectory();
    }

    public CloneResult cloneRepository(CloneRequest request) {
        long startTime = System.currentTimeMillis();
        String targetPath = null;

        try {
            validateCloneRequest(request);

            if (!cloneSemaphore.tryAcquire(properties.getTimeoutSeconds(), TimeUnit.SECONDS)) {
                throw new IllegalStateException("Clone operation timed out waiting for available slot");
            }

            try {
                targetPath = prepareTargetDirectory(request);

                logger.info("Starting clone operation for repository: {}", request.repositoryUrl());

                CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(request.repositoryUrl())
                        .setDirectory(new File(targetPath))
                        .setBranch(request.branch())
                        .setCloneAllBranches(false);

                if (request.isShallowClone()) {
                    cloneCommand.setDepth(request.depth());
                }

                if (request.githubToken() != null && !request.githubToken().isBlank()) {
                    cloneCommand.setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider("token", request.githubToken())
                    );
                }

                try (Git git = cloneCommand.call()) {
                    Repository repository = git.getRepository();

                    // Analyze cloned repository
                    DirectoryAnalysis analysis = analyzeDirectory(targetPath);

                    long cloneDuration = System.currentTimeMillis() - startTime;

                    logger.info("Successfully cloned repository {} to {} in {}ms",
                            request.repositoryUrl(), targetPath, cloneDuration);

                    return CloneResult.success(
                            targetPath,
                            request.repositoryUrl(),
                            repository.getBranch(),
                            analysis.fileCount(),
                            analysis.totalSize(),
                            cloneDuration,
                            analysis.projectFiles()
                    );

                }
            } finally {
                cloneSemaphore.release();
            }

        } catch (Exception e) {
            logger.error("Failed to clone repository: {}", request.repositoryUrl(), e);

            // Cleanup on error if configured
            if (properties.isCleanupOnError() && targetPath != null) {
                cleanupDirectory(targetPath);
            }

            return CloneResult.failure(request.repositoryUrl(), e.getMessage());
        }
    }

    private void validateCloneRequest(CloneRequest request) {
        if (!GITHUB_URL_PATTERN.matcher(request.repositoryUrl()).matches()) {
            throw new IllegalArgumentException("Invalid GitHub repository URL format");
        }
    }

    private String prepareTargetDirectory(CloneRequest request) throws IOException {
        String baseDir = properties.getWorkspaceDirectory();

        // Extract repository name from URL
        String repoName = extractRepositoryName(request.repositoryUrl());
        String timestamp = String.valueOf(System.currentTimeMillis());

        String targetPath;
        if (request.targetDirectory() != null && !request.targetDirectory().isBlank()) {
            targetPath = Paths.get(baseDir, request.targetDirectory()).toString();
        } else {
            targetPath = Paths.get(baseDir, repoName + "_" + timestamp).toString();
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

    private DirectoryAnalysis analyzeDirectory(String directoryPath) throws IOException {
        Path rootPath = Paths.get(directoryPath);
        List<String> foundProjectFiles = new ArrayList<>();
        AtomicCounters counters = new AtomicCounters();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile()) {
                    counters.incrementFiles();
                    counters.addSize(attrs.size());

                    String fileName = file.getFileName().toString();
                    if (PROJECT_INDICATOR_FILES.contains(fileName)) {
                        String relativePath = rootPath.relativize(file).toString();
                        foundProjectFiles.add(relativePath);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return new DirectoryAnalysis(counters.getFileCount(), counters.getTotalSize(), foundProjectFiles);
    }

    private void initializeWorkspaceDirectory() {
        try {
            Path workspaceDir = Paths.get(properties.getWorkspaceDirectory());
            if (!Files.exists(workspaceDir)) {
                Files.createDirectories(workspaceDir);
                logger.info("Created workspace directory: {}", workspaceDir);
            }
        } catch (IOException e) {
            logger.error("Failed to initialize workspace directory", e);
            throw new RuntimeException("Failed to initialize workspace directory", e);
        }
    }

    private void cleanupDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (Files.exists(path)) {
                FileSystemUtils.deleteRecursively(path);
                logger.info("Cleaned up directory: {}", directoryPath);
            }
        } catch (IOException e) {
            logger.warn("Failed to cleanup directory: {}", directoryPath, e);
        }
    }

    private record DirectoryAnalysis(int fileCount, long totalSize, List<String> projectFiles) {
    }

    private static class AtomicCounters {
        private int fileCount = 0;
        private long totalSize = 0;

        synchronized void incrementFiles() {
            fileCount++;
        }

        synchronized void addSize(long size) {
            totalSize += size;
        }

        synchronized int getFileCount() {
            return fileCount;
        }

        synchronized long getTotalSize() {
            return totalSize;
        }
    }
}
