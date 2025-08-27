# MCP Server GitHub

A **Model Context Protocol (MCP) server** built with **Spring Boot** and **Spring AI** that provides GitHub integration capabilities to AI models like Claude. This server enables AI agents to interact with GitHub repositories through standardized MCP tools.

## 🚀 Features

- **Repository Cloning**: Clone GitHub repositories to local filesystem
- **Spring Boot Integration**: Built with Spring Boot 3.5+ and Spring AI MCP Server
- **STDIO Transport**: Compatible with Claude Desktop and other MCP clients
- **Extensible Architecture**: Easy to add new GitHub operations and tools

## 📋 Prerequisites

- **Java 21+**
- **Maven 3.6+**
- **Git** (for repository operations)
- **MCP Client** (Claude Desktop, or custom client)

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/riccardocinti/mcp-server-github.git
cd mcp-server-github
```

### 2. Build the Application

```bash
mvn clean package
```

This creates an executable JAR in the `target/` directory.

### 3. Configure Application

The server is pre-configured for MCP STDIO transport. Key configurations in `application.yaml`:

```yaml
# CRITICAL: Disable banner and console logging for STDIO transport
logging:
  level:
    root: OFF

# Disable web server for STDIO transport
spring:
  main:
    web-application-type: none
    banner-mode: off

  # Server Configuration
  ai:
    mcp:
      server:
        enabled: true
        type: SYNC
```

## 🔧 Available Tools

### `clone_repository`
Clone a GitHub repository to the local filesystem.

**Parameters:**
- `repositoryUrl` (string): The GitHub repository URL (HTTPS or SSH)
- `targetDirectory` (string): Local directory path where the repository will be cloned

**Example Usage:**
```json
{
  "repositoryUrl": "https://github.com/user/repo.git",
  "targetDirectory": "/tmp/cloned-repo"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Repository cloned successfully",
  "clonedPath": "/tmp/cloned-repo"
}
```

## 🖥️ Usage with Claude Desktop

### 1. Add to Claude Desktop Configuration

Edit your Claude Desktop configuration file:
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "mcp-server-github": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/mcp-server-github-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

### 2. Restart Claude Desktop

After updating the configuration, restart Claude Desktop to load the MCP server.

### 3. Test the Integration

Ask Claude to use GitHub operations:
- "Clone the repository https://github.com/user/example-repo to /tmp/test-repo"
- "Can you help me clone a specific GitHub repository?"

## 🧪 Testing

### Manual Testing
Test the server directly:

```bash
java -jar target/mcp-server-github-0.0.1-SNAPSHOT.jar
```

### MCP Client Testing
Use the provided test client to verify functionality:

```java
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

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.closeGracefully();
        }
    }
}
```

## 🏗️ Architecture

### Core Components

- **`CloneRepositoryService`**: Main service implementing the `@Tool` annotated methods
- **`GitHubCloneService`**: Business logic for GitHub operations using JGit
- **`CloneRequest`/`CloneResponse`**: Data transfer objects for tool parameters and responses
- **`McpServerGithubApplication`**: Spring Boot main class with tool registration

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🐛 Troubleshooting

### Common Issues

**"No content to map due to end-of-input" Error**
- Ensure console logging is disabled in `application.yaml`
- Verify `spring.main.banner-mode=off` is set
- Check that `logging.pattern.console=` is empty

**Tools Not Appearing**
- Verify `@Tool` annotations are present on service methods
- Ensure `ToolCallbacks.from()` is properly registered in main class
- Check that Spring component scanning is working

**Clone Operation Fails**
- Verify Git is installed and accessible
- Check repository URL format and accessibility
- Ensure target directory has write permissions

## 📚 Resources

- [Model Context Protocol Specification](https://github.com/modelcontextprotocol/specification)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Claude Desktop MCP Setup](https://claude.ai/docs/mcp)
- [JGit Documentation](https://www.eclipse.org/jgit/documentation/)
