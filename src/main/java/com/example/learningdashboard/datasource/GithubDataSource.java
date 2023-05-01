package com.example.learningdashboard.datasource;

import org.kohsuke.github.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GithubDataSource extends DataSource {

    private final RestTemplate restTemplate;
    private final String baseUrl = "https://api.github.com";

    private String repository = "";
    private String owner = "";
    private String accessToken = "";
    private static final String ISSUES_OBJECT = "issues";
    private static final String COMMITS_OBJECT = "commits";

    public GithubDataSource(RestTemplateBuilder restTemplateBuilder, String repository, String owner, String accessToken) {
        this.restTemplate = restTemplateBuilder.build();
        this.repository = repository;
        this.owner = owner;
        this.accessToken = accessToken;
    }

    public GithubDataSource() {
        this.restTemplate = new RestTemplate();
    }

    public void init(String repository, String owner, String accessToken) {
        this.repository = repository;
        this.owner = owner;
        this.accessToken = accessToken;
    }

    @Override
    public String getName() {
        return "github";
    }

    @Override
    public boolean supportsObject(String objectName) {
        return objectName.equals(ISSUES_OBJECT) || objectName.equals(COMMITS_OBJECT);
    }

    @Override
    public Object retrieveData(String objectName) throws Exception {

        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
        GHRepository repo = github.getRepository(owner + "/" + repository);

        if (ISSUES_OBJECT.equals(objectName)) {
            List<GHIssue> issues = repo.getIssues(GHIssueState.ALL);
            //TODO: map to KG
            System.out.println(issues);
            return issues;
        } else if (COMMITS_OBJECT.equals(objectName)) {
            List<GHCommit> commits = repo.listCommits().asList();
            //TODO: map to KG
            System.out.println(commits);
            return commits;
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public static void main(String[] args) {
        GithubDataSource githubDataSource = new GithubDataSource(new RestTemplateBuilder(), "LearningDashboard", "calandula", "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");

        try {
            List<GHIssue> issues = (List<GHIssue>) githubDataSource.retrieveData("issues");
            System.out.println("Issues: " + issues);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Retrieve commits from a repository
        try {
            List<GHCommit> commits = (List<GHCommit>) githubDataSource.retrieveData("commits");
            System.out.println("Commits: " + commits);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}