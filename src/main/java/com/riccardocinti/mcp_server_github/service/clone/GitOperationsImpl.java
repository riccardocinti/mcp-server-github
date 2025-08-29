package com.riccardocinti.mcp_server_github.service.clone;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

@Service
public class GitOperationsImpl implements GitOperations {

    @Override
    public Git cloneRepository(CloneCommand cloneCommand) throws GitAPIException {
        return cloneCommand.call();
    }

}
