package com.riccardocinti.mcp_server_github.service.clone;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;


@Service
public class GitRequestValidatorImpl implements GitRequestValidator {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
            "^https://github\\.com/[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+(?:\\.git)?/?$"
    );

    @Override
    public boolean isValid(CloneRequest request) {
        return GITHUB_URL_PATTERN.matcher(request.repositoryUrl()).matches();
    }

}
