package com.example.learningdashboard.datasource;

import com.example.learningdashboard.dtos.GithubDataSourceDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GithubDataSource extends DataSource {
    private final String baseUrl = "https://api.github.com";
    @Autowired
    private DataSourceRepository dataSourceRepository;
    @Autowired
    private GithubEntitiesRepository githubEntitiesRepository;
    private String repository;
    private String owner;
    private String accessToken;
    private String datasourceId;
    private static final String ISSUES_OBJECT = "issues";
    private static final String COMMITS_OBJECT = "commits";

    public GithubDataSource() {

    }

    public GithubDataSource(String dataSourceId) {
        GithubDataSourceDto ds = (GithubDataSourceDto) dataSourceRepository.findById(dataSourceId);
        this.repository = ds.getRepository();
        this.owner = ds.getOwner();
        this.accessToken = ds.getAccessToken();
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
            githubEntitiesRepository.saveIssues(datasourceId, issues);
            System.out.println(issues);
            return issues;
        } else if (COMMITS_OBJECT.equals(objectName)) {
            List<GHCommit> commits = repo.listCommits().asList();
            githubEntitiesRepository.saveCommits(datasourceId, commits);
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
//        GithubDataSource githubDataSource = new GithubDataSource("LearningDashboard", "calandula", "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");
//
//        try {
//            List<GHIssue> issues = (List<GHIssue>) githubDataSource.retrieveData("issues");
//            System.out.println("Issues: " + issues);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // Retrieve commits from a repository
//        try {
//            List<GHCommit> commits = (List<GHCommit>) githubDataSource.retrieveData("commits");
//            System.out.println("Commits: " + commits);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}