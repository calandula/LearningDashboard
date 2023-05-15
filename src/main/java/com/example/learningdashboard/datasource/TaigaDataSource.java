package com.example.learningdashboard.datasource;

import com.example.learningdashboard.repository.DataSourceRepository;
import org.kohsuke.github.*;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.io.IOException;
import java.util.List;

public class TaigaDataSource extends DataSource {

    private final String baseUrl = "https://api.github.com";

    private String repository = "";
    private String owner = "";
    private String accessToken = "";
    private static final String USERSTORIES_OBJECT = "userstories";
    private static final String TASKS_OBJECT = "tasks";

    public TaigaDataSource(RestTemplateBuilder restTemplateBuilder, String repository, String owner, String accessToken) {
        this.repository = repository;
        this.owner = owner;
        this.accessToken = accessToken;
    }

    public TaigaDataSource(String dataSourceId) {
        super();

    }

    public TaigaDataSource(String dataSourceId, DataSourceRepository dataSourceRepository) {
        super();
    }

    @Override
    public String getName() {
        return "taiga";
    }

    @Override
    public boolean supportsObject(String objectName) {
        return objectName.equals(USERSTORIES_OBJECT) || objectName.equals(TASKS_OBJECT);
    }

    @Override
    public Object retrieveData(String objectName) throws Exception {

        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
        GHRepository repo = github.getRepository(owner + "/" + repository);

        if (USERSTORIES_OBJECT.equals(objectName)) {
            List<GHIssue> issues = repo.getIssues(GHIssueState.ALL);
            //insertIssues(issues);
            System.out.println(issues);
            return issues;
        } else if (TASKS_OBJECT.equals(objectName)) {
            List<GHCommit> commits = repo.listCommits().asList();
            //insertCommits(commits);
            System.out.println(commits);
            return commits;
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }

    private void insertCommits(List<GHCommit> commits) throws IOException {
        System.out.println(commits);
    }

    private void insertIssues(List<GHIssue> issues) {
        System.out.println(issues);
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
        /*GithubDataSource githubDataSource = new GithubDataSource("LearningDashboard", "calandula", "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");

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
        }*/
    }
}
