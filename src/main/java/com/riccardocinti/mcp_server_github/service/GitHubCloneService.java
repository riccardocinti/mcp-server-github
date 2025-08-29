package com.riccardocinti.mcp_server_github.service;

import com.riccardocinti.mcp_server_github.config.GitHubMcpConfig;
import com.riccardocinti.mcp_server_github.service.analysis.DirectoryAnalysis;
import com.riccardocinti.mcp_server_github.service.analysis.DirectoryAnalyzer;
import com.riccardocinti.mcp_server_github.service.clone.*;
import com.riccardocinti.mcp_server_github.service.infrastructure.FileSystemOperations;
import com.riccardocinti.mcp_server_github.service.infrastructure.TimeProvider;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class GitHubCloneService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubCloneService.class);

    private final GitOperations gitOperations;
    private final FileSystemOperations fileSystemOperations;
    private final TimeProvider timeProvider;
    private final DirectoryAnalyzer directoryAnalyzer;
    private final GitRequestValidator gitRequestValidator;
    private final TargetPathResolver targetPathResolver;
    private final Semaphore cloneSemaphore;
    private final GitHubMcpConfig properties;

    public GitHubCloneService(GitOperations gitOperations,
                              FileSystemOperations fileSystemOperations,
                              TimeProvider timeProvider,
                              DirectoryAnalyzer directoryAnalyzer,
                              GitRequestValidator gitRequestValidator,
                              TargetPathResolver targetPathResolver,
                              GitHubMcpConfig properties) {
        this.gitOperations = gitOperations;
        this.fileSystemOperations = fileSystemOperations;
        this.timeProvider = timeProvider;
        this.directoryAnalyzer = directoryAnalyzer;
        this.gitRequestValidator = gitRequestValidator;
        this.targetPathResolver = targetPathResolver;
        this.properties = properties;
        this.cloneSemaphore = new Semaphore(properties.getMaxConcurrentClones());
    }

    public CloneResult cloneRepository(CloneRequest cloneRequest) {
        long startTime = timeProvider.currentTimeMillisec();

        try {
            if (!gitRequestValidator.isValid(cloneRequest)) {
                throw new IllegalArgumentException("Invalid GitHub repository URL format");
            }

            return executeWithSemaphore(() -> performClone(cloneRequest, startTime));
        } catch (Exception e) {
            logger.error("Failed to clone repository: {}", cloneRequest.repositoryUrl(), e);
            return CloneResult.failure(cloneRequest.repositoryUrl(), e.getMessage());
        }
    }

    private CloneResult executeWithSemaphore(Supplier<CloneResult> operation) throws InterruptedException {
        if (!cloneSemaphore.tryAcquire(properties.getTimeoutSeconds(), TimeUnit.SECONDS)) {
            throw new IllegalStateException("Clone operation timed out waiting for available slot");
        }

        try {
            return operation.get();
        } finally {
            cloneSemaphore.release();
        }
    }

    private CloneResult performClone(CloneRequest cloneRequest, long startTime) {
        String targetPath = null;

        try {
            targetPath = targetPathResolver.resolve(cloneRequest, properties.getWorkspaceDirectory());
            fileSystemOperations.createDirectory(targetPath);

            CloneCommand cloneCommand = createCloneCommand(cloneRequest, targetPath);

            try (Git git = gitOperations.cloneRepository(cloneCommand)) {
                return buildSuccessfulResult(cloneRequest, targetPath, git, startTime);
            }
        } catch (Exception e) {
            handleCloneError(targetPath, e);
            throw new RuntimeException("Clone failed", e);
        }
    }

    private CloneCommand createCloneCommand(CloneRequest cloneRequest, String targetPath) {
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(cloneRequest.repositoryUrl())
                .setDirectory(new File(targetPath))
                .setBranch(cloneRequest.branch())
                .setCloneAllBranches(false);

        if (cloneRequest.isShallowClone()) {
            cloneCommand.setDepth(cloneRequest.depth());
        }

        if (cloneRequest.githubToken() != null && !cloneRequest.githubToken().isBlank()) {
            cloneCommand.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider("token", cloneRequest.githubToken())
            );
        }

        return cloneCommand;
    }

    private CloneResult buildSuccessfulResult(CloneRequest cloneRequest, String targetPath,
                                              Git git, long startTime) throws IOException {

        Repository repository = git.getRepository();

        // Analyze cloned repository
        DirectoryAnalysis analysis = directoryAnalyzer.analyze(targetPath);

        long cloneDuration = System.currentTimeMillis() - startTime;

        logger.info("Successfully cloned repository {} to {} in {}ms",
                cloneRequest.repositoryUrl(), targetPath, cloneDuration);

        return CloneResult.success(
                targetPath,
                cloneRequest.repositoryUrl(),
                repository.getBranch(),
                analysis.fileCount(),
                analysis.totalSize(),
                cloneDuration
        );

    }

    private void handleCloneError(String targetPath, Exception e) {
        if (properties.isCleanupOnError() && targetPath != null) {
            try {
                fileSystemOperations.deleteDirectory(targetPath);
            } catch (Exception cleanupError) {
                logger.warn("Failed to cleanup directory after clone error", cleanupError);
            }
        }
    }

}
