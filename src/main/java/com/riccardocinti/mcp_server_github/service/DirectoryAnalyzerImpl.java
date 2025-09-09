package com.riccardocinti.mcp_server_github.service;

import com.riccardocinti.mcp_server_github.model.DirectoryAnalysis;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Service
public class DirectoryAnalyzerImpl implements DirectoryAnalyzer {

    private static final List<String> PROJECT_INDICATOR_FILES = List.of(
            "pom.xml", "build.gradle", "package.json", "requirements.txt",
            "Dockerfile", "docker-compose.yml", "README.md", ".gitignore"
    );

    @Override
    public DirectoryAnalysis analyze(String directoryPath) throws IOException {
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
