package com.riccardocinti.mcp_server_github.service;

import com.riccardocinti.mcp_server_github.config.GitHubMcpConfig;
import com.riccardocinti.mcp_server_github.model.CloneRequest;
import com.riccardocinti.mcp_server_github.model.CloneResult;
import com.riccardocinti.mcp_server_github.model.DirectoryAnalysis;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitHubCloneService Tests")
public class GitHubCloneServiceTest {

    @Mock
    private GitOperations gitOperations;

    @Mock
    private FileSystemOperations fileSystemOperations;

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private DirectoryAnalyzer directoryAnalyzer;

    @Mock
    private GitRequestValidator gitRequestValidator;

    @Mock
    private TargetPathResolver targetPathResolver;

    @Mock
    private Git git;

    @Mock
    private Repository repository;

    @Nested
    @DisplayName("cloneRepository() Tests")
    class CloneRepositoryTest {

        @Test
        @DisplayName("Should successfully clone the given repository")
        void shouldSuccessfullyCloneGivenRepository() throws Exception {

            GitHubCloneService gitHubCloneService = new GitHubCloneService(gitOperations,
                    fileSystemOperations,
                    timeProvider,
                    directoryAnalyzer,
                    gitRequestValidator,
                    targetPathResolver,
                    defaultProperties());

            CloneRequest testCloneRequest = new CloneRequest("https://github.com/test_repo.git",
                    null,
                    "",
                    null,
                    null);

            String targetPath = "/tmp/target/repo";
            DirectoryAnalysis analysis = new DirectoryAnalysis(10, 1024L, List.of("README.md"));

            when(timeProvider.currentTimeMillisec()).thenReturn(System.currentTimeMillis());
            when(gitRequestValidator.isValid(testCloneRequest)).thenReturn(true);
            when(targetPathResolver.resolve(testCloneRequest, "")).thenReturn(targetPath);
            when(gitOperations.cloneRepository(any(CloneCommand.class))).thenReturn(git);
            when(git.getRepository()).thenReturn(repository);
            when(repository.getBranch()).thenReturn("main");
            when(directoryAnalyzer.analyze(targetPath)).thenReturn(analysis);

            CloneResult cloneResult = gitHubCloneService.cloneRepository(testCloneRequest);

            assertTrue(cloneResult.success());
            assertEquals(targetPath, cloneResult.localPath());

            verify(gitRequestValidator).isValid(testCloneRequest);
            verify(fileSystemOperations).createDirectory(targetPath);
        }

    }

    public static GitHubMcpConfig defaultProperties() {
        GitHubMcpConfig defaultProperties = new GitHubMcpConfig();
        defaultProperties.setTimeoutSeconds(30L);
        defaultProperties.setMaxConcurrentClones(1);
        defaultProperties.setWorkspaceDirectory("");
        return defaultProperties;
    }
}
