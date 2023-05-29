package com.example.learningdashboard.repository;

import com.example.learningdashboard.datasource_model.Commit;
import com.example.learningdashboard.datasource_model.Issue;
import com.example.learningdashboard.dtos.GithubDataSourceDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class GithubEntitiesRepository {

    private static final String COMMITS_OBJECT = "commits";
    private static final String ISSUES_OBJECT = "issues";
    private static final String COMMIT_COUNT = "commit_count";
    private static final String ISSUE_COUNT = "issue_count";
    private static final String ISSUE_INDIVIDUAL_COUNT = "issue_individual_count";
    private static final String COMMIT_INDIVIDUAL_COUNT = "commit_individual_count";
    private final String baseUrl = "https://api.github.com/repos";
    @Autowired
    private DataSourceRepository dataSourceRepository;
    @Autowired
    private String prefixes;
    @Autowired
    private Dataset dataset;
    @Autowired
    private String namespace;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void saveCommits(String datasourceId, List<Commit> commits) {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (Commit commit : commits) {
            Resource commitResource = model.createResource(namespace + UUID.randomUUID());
            model.add(commitResource, RDF.type, model.createResource(namespace + "Commit"));
            model.add(datasourceResource, model.createProperty(namespace + "hasCommit"), commitResource);
            model.add(commitResource, model.createProperty(namespace + "commitMessage"), model.createTypedLiteral(commit.getMessage()));
            model.add(commitResource, model.createProperty(namespace + "commitIsVerified"), model.createTypedLiteral(commit.getVerified()));
            model.add(commitResource, model.createProperty(namespace + "commitCommentCount"), model.createTypedLiteral(commit.getCommentCount()));
            Resource membership = getMembershipResourceByUsername(model, commit.getCreatedBy());
            if (membership != null) {
                model.add(commitResource, model.createProperty(namespace + "createdBy"), getMembershipResourceByUsername(model, commit.getCreatedBy()));
            }
        }

        dataset.commit();
    }

    private void saveIssues(String dataSourceId, List<Issue> issues) {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + dataSourceId);

        for (Issue issue : issues) {
            Resource issueResource = model.createResource(namespace + UUID.randomUUID());
            model.add(issueResource, RDF.type, model.createResource(namespace + "Issue"));
            model.add(datasourceResource, model.createProperty(namespace + "hasIssue"), issueResource);
            model.add(issueResource, model.createProperty(namespace + "issueState"), model.createTypedLiteral(issue.getState()));
            model.add(issueResource, model.createProperty(namespace + "issueTitle"), model.createTypedLiteral(issue.getTitle()));
            model.add(issueResource, model.createProperty(namespace + "issueBody"), model.createTypedLiteral(issue.getBody()));
            model.add(issueResource, model.createProperty(namespace + "issueIsLocked"), model.createTypedLiteral(issue.getIsLocked()));
            Resource membership = getMembershipResourceByUsername(model, issue.getAssignedTo());
            if (membership != null) {
                model.add(issueResource, model.createProperty(namespace + "assignedTo"), membership);
            }
            membership = getMembershipResourceByUsername(model, issue.getCreatedBy());
            if (membership != null) {
                model.add(issueResource, model.createProperty(namespace + "createdBy"), membership);
            }
        }

        dataset.commit();
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

    public float computeMetric(String datasourceId, String operation, String target) {

        dataset.begin(ReadWrite.READ);
        try {
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);
        StmtIterator commitsIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasCommit"), (RDFNode) null);
        StmtIterator issuesIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasIssue"), (RDFNode) null);

        switch (operation) {
            case COMMIT_COUNT -> {
                int totalCommits = 0;
                while (commitsIter.hasNext()) {
                    Statement stmt = commitsIter.next();
                    totalCommits++;
                }
                return totalCommits;
            }
            case ISSUE_COUNT -> {
                int totalIssues = 0;
                while (issuesIter.hasNext()) {
                    Statement stmt = issuesIter.next();
                    totalIssues++;
                }
                return totalIssues;
            }
            case ISSUE_INDIVIDUAL_COUNT -> {
                int totalIssues = 0;
                while (issuesIter.hasNext()) {
                    Statement stmt = issuesIter.next();
                    Resource issueResource = stmt.getObject().asResource();
                    if (hasAssignedTo(issueResource, target, model)) {
                        totalIssues++;
                    }
                }
                return totalIssues;
            }
            case COMMIT_INDIVIDUAL_COUNT -> {
                int totalCommits = 0;
                while (commitsIter.hasNext()) {
                    Statement stmt = commitsIter.next();
                    Resource commitResource = stmt.getObject().asResource();
                    if (hasAssignedTo(commitResource, target, model)) {
                        totalCommits++;
                    }
                }
                return totalCommits;
            }
            default -> {
                throw new IllegalArgumentException("Unsupported method or operation failed");
            }
        }
    } finally {
        dataset.end();
    }
    }

    private boolean hasAssignedTo(Resource resource, String target, Model m) {
        Property assignedToProperty = m.createProperty(namespace + "createdBy");
        StmtIterator assignedToIter = m.listStatements(resource, assignedToProperty, (RDFNode) null);
        while (assignedToIter.hasNext()) {
            Statement stmt = assignedToIter.next();
            Resource assignedToResource = stmt.getObject().asResource();
            String assignedToUsername = assignedToResource.getProperty(dataset.getDefaultModel().createProperty(namespace + "membershipUsername")).getString();
            if (assignedToUsername.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public void retrieveData(String objectName, String dataSourceId) throws IOException {
        GithubDataSourceDto ds = (GithubDataSourceDto) dataSourceRepository.findById(dataSourceId);

        if (COMMITS_OBJECT.equals(objectName)) {
            List<Commit> commits = retrieveCommits(ds);
            saveCommits(dataSourceId, commits);
        } else if (ISSUES_OBJECT.equals(objectName)) {
            List<Issue> tasks = retrieveIssues(ds);
            saveIssues(dataSourceId, tasks);
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }

    public List<Commit> retrieveCommits(GithubDataSourceDto ds) throws IOException {
        String url = baseUrl + "/" + ds.getOwner() + "/" + ds.getRepository() + "/commits";
        String responseJson = makeApiRequest(url, ds.getAccessToken());

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        List<Commit> commits = new ArrayList<>();

        for (JsonNode commitNode : jsonNode) {
            Commit commit = new Commit();
            commit.setId(String.valueOf(UUID.randomUUID()));
            commit.setCreatedBy(commitNode.get("commit").get("author").get("name").asText());
            commit.setMessage(commitNode.get("commit").get("message").asText());
            commit.setVerified(commitNode.get("commit").get("verification").get("verified").asBoolean());
            commit.setCommentCount(commitNode.get("commit").get("comment_count").asInt());
            commits.add(commit);
        }

        return commits;
    }

    public List<Issue> retrieveIssues(GithubDataSourceDto ds) throws IOException {

        String url = baseUrl + "/" + ds.getOwner() + "/" + ds.getRepository() + "/issues";
        String responseJson = makeApiRequest(url, ds.getAccessToken());

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        List<Issue> issues = new ArrayList<>();

        for (JsonNode issueNode : jsonNode) {
            Issue issue = new Issue();
            issue.setId(String.valueOf(UUID.randomUUID()));
            issue.setTitle(issueNode.get("title").asText());
            issue.setState(issueNode.get("state").asText());
            issue.setBody(issueNode.get("body").asText());
            issue.setIsLocked(issueNode.get("locked").asBoolean());
            JsonNode assignedToExtraInfoNode = issueNode.get("assignee");
            if (assignedToExtraInfoNode != null && assignedToExtraInfoNode.has("login")) {
                issue.setAssignedTo(assignedToExtraInfoNode.get("login").asText());
            }
            JsonNode userExtraInfoNode = issueNode.get("user");
            if (userExtraInfoNode != null && userExtraInfoNode.has("login")) {
                issue.setCreatedBy(userExtraInfoNode.get("login").asText());
            }
            issues.add(issue);
        }

        return issues;
    }

    private String makeApiRequest(String url, String token) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    public boolean supportsObject(String objectName) {
        return objectName.equals(COMMITS_OBJECT) || objectName.equals(ISSUES_OBJECT);
    }

    public boolean supportsMethod(String method) {
        return method.equals(COMMIT_COUNT)
                || method.equals(ISSUE_COUNT)
                || method.equals(ISSUE_INDIVIDUAL_COUNT)
                || method.equals(COMMIT_INDIVIDUAL_COUNT);
    }
}
