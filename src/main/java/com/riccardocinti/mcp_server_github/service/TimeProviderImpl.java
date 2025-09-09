package com.riccardocinti.mcp_server_github.service;

import org.springframework.stereotype.Service;

@Service
public class TimeProviderImpl implements TimeProvider {

    @Override
    public long currentTimeMillisec() {
        return System.currentTimeMillis();
    }

}
