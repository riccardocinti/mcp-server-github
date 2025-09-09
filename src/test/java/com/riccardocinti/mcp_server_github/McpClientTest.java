package com.riccardocinti.mcp_server_github;

import com.riccardocinti.mcp_server_github.model.CloneRequest;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

import java.util.Map;

public class McpClientTest {

    public static void main(String[] args) {
        McpSyncClient client = null;
        try {
            var stdioParams = ServerParameters.builder("java")
                    .args("-jar", "target/mcp-server-github-0.0.1-SNAPSHOT.jar")
                    .build();

            var transport = new StdioClientTransport(stdioParams);
            client = McpClient.sync(transport).build();

            // Wait for initialization to complete
            client.initialize();

            // List and demonstrate tools
            var toolsList = client.listTools();
            System.out.println("Available Tools = " + toolsList);

            CloneRequest cloneRequest = new CloneRequest(
                    "https://github.com/spring-projects/spring-ai-examples.git",
                    null,
                    "",
                    null,
                    null);
            CallToolResult callToolResult = client
                    .callTool(new CallToolRequest("clone_repository", Map.of("cloneRequest", cloneRequest)));

            System.out.println(callToolResult);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.closeGracefully();
        }
    }
}