package com.example.learningdashboard.datasource;

import com.example.learningdashboard.model.GithubIssue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.Map;

@Component
public class GithubDataSource implements DataSource {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String accessToken;

    public GithubDataSource(RestTemplateBuilder restTemplateBuilder, @Value("${github.baseUrl}") String baseUrl, @Value("${github.accessToken}") String accessToken) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    @Override
    public String getName() {
        return "github";
    }

    @Override
    public boolean supportsObject(String objectName) {
        return "issues".equals(objectName);
    }

    @Override
    public Object retrieveData(String objectName, Map<String, String> apiConfig) throws Exception {
        String apiUrl = apiConfig.get("url");
        if (apiUrl == null || apiUrl.isEmpty()) {
            throw new IllegalArgumentException("API URL cannot be null or empty");
        }

        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
        GHRepository repo = github.getRepository(apiUrl);

        if ("issues".equals(objectName)) {
            List<GHIssue> issues = repo.getIssues(GHIssueState.ALL);
            //TODO: map to KG
            return issues;
        } else if ("commits".equals(objectName)) {
            List<GHCommit> commits = repo.listCommits().asList();
            //TODO: map to KG
            return commits;
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }
}