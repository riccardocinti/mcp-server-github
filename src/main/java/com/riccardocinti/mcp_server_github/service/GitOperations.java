package com.riccardocinti.mcp_server_github.service;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public interface GitOperations {

    Git cloneRepository(CloneCommand cloneCommand) throws GitAPIException;

}
