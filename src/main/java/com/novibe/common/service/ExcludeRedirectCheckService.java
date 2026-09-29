package com.novibe.common.service;

import com.novibe.common.data_sources.ExcludeRedirectSettingsLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExcludeRedirectCheckService {

    private final List<String> ignoringList;

    public ExcludeRedirectCheckService(ExcludeRedirectSettingsLoader excludeRedirectSettingsLoader) {
        ignoringList = excludeRedirectSettingsLoader.loadIgnoredDomains();
    }

    public boolean shouldExclude(String domain) {
        for (String ignored : ignoringList) {
            if (domain.equals(ignored) || domain.endsWith("." + ignored)) {
                return true;
            }
        }
        return false;
    }

}
