package com.example.learningdashboard.datasource;

import com.example.learningdashboard.dtos.GithubDataSourceDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Repository
public class GithubEntitiesRepository {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    private final String baseUrl = "https://api.github.com";

    private static final String ISSUES_OBJECT = "issues";
    private static final String COMMITS_OBJECT = "commits";
    private static final String ISSUE_COUNT = "issue_count";
    private static final String COMMIT_COUNT = "commit_count";

    public void saveIssues(String datasourceId, List<GHIssue> issues) throws IOException {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (GHIssue issue : issues) {
            Resource issueResource = model.createResource(namespace + UUID.randomUUID());
            model.add(issueResource, RDF.type, model.createResource(namespace + "Issue"));
            Resource createdBy = getMembershipResourceByUsername(model, issue.getUser().getLogin());
            if (createdBy != null) {
                model.add(issueResource, model.createProperty(namespace + "issueCreatedBy"), createdBy);
            }
            Resource assignedTo = getMembershipResourceByUsername(model, issue.getAssignee().getLogin());
            if (createdBy != null) {
                model.add(issueResource, model.createProperty(namespace + "issueAssignedTo"), assignedTo);
            }
            model.add(datasourceResource, model.createProperty(namespace + "hasIssue"), issueResource);

        }

        dataset.commit();
    }

    public void saveCommits(String datasourceId, List<GHCommit> commits) throws IOException {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (GHCommit commit : commits) {
            Resource commitResource = model.createResource(namespace + UUID.randomUUID());
            model.add(commitResource, RDF.type, model.createResource(namespace + "Commit"));
            Resource assignedTo = getMembershipResourceByUsername(model, commit.getAuthor().getLogin());
            if (assignedTo != null) {
                model.add(commitResource, model.createProperty(namespace + "commitCreatedBy"), assignedTo);
            }
            model.add(datasourceResource, model.createProperty(namespace + "hasCommit"), commitResource);
        }

        dataset.commit();
    }

    public float computeMetric(String datasourceId, String operation, String target) {

        dataset.begin(ReadWrite.READ);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);
        StmtIterator commitsIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasCommit"), (RDFNode)null);
        StmtIterator issuesIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasIssue"), (RDFNode)null);

        switch(operation) {
            case COMMIT_COUNT -> {
                int commitsCount = 0;
                while (commitsIter.hasNext()) {
                    commitsIter.next();
                    commitsCount += 1;
                }
                return commitsCount;
            }
            case ISSUE_COUNT -> {
                int taskCount = 0;
                while (commitsIter.hasNext()) {
                    Statement stmt = commitsIter.next();
                }
                return 4f;
            }
        }
        dataset.commit();
        return 0;
    }

    private Resource getMembershipResourceByUsername(Model model, String username) {
        StmtIterator iter = model.listStatements(null, RDF.type, model.createResource(namespace + "Membership"));
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            Resource membershipResource = stmt.getSubject();
            String membershipUsername = model.getProperty(membershipResource, model.createProperty(namespace + "membershipUsername")).getString();
            if (membershipUsername.equals(username)) {
                return membershipResource;
            }
        }
        return null;
    }

    public void retrieveData(String objectName, String dataSourceId) throws IOException {
        GithubDataSourceDto ds = (GithubDataSourceDto) dataSourceRepository.findById(dataSourceId);

        GitHub github = new GitHubBuilder().withOAuthToken(ds.getAccessToken()).build();
        GHRepository repo = github.getRepository(ds.getOwner() + "/" + ds.getRepository());

        if (ISSUES_OBJECT.equals(objectName)) {
            List<GHIssue> issues = repo.getIssues(GHIssueState.ALL);
            saveIssues(dataSourceId, issues);
        } else if (COMMITS_OBJECT.equals(objectName)) {
            List<GHCommit> commits = repo.listCommits().asList();
            saveCommits(dataSourceId, commits);
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }

    public boolean supportsObject(String objectName) {
        return objectName.equals(ISSUES_OBJECT) || objectName.equals(COMMITS_OBJECT);
    }

    public boolean supportsMethod(String method) {
        return method.equals(COMMIT_COUNT) || method.equals(ISSUE_COUNT);
    }
}
