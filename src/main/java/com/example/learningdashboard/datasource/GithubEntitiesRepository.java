package com.example.learningdashboard.datasource;

import com.example.learningdashboard.dtos.DataSourceDto;
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

    private static final String NUMBER_COMMITS = "number_commits";

    private static final String SD = "sd";

    private static final String TASK_REFERENCE = "task_reference";

    private static final String MODIFIED_LINES = "modified_lines";

    public void saveIssues(String datasourceId, List<GHIssue> issues) {
        /*String studentURI = namespace + studentId;
        Resource studentResource = ResourceFactory.createResource(studentURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(studentResource)) {
                return null;
            }

            String studentName = model.getProperty(studentResource, model.createProperty(namespace + "studentName"))
                    .getString();
            List<String> memberships = model.listObjectsOfProperty(studentResource, model.createProperty(namespace + "hasMembership"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            StudentDto student = new StudentDto();
            student.setName(studentName);
            student.setMemberships((ArrayList<String>) memberships);
            student.setId(JenaUtils.parseId(studentResource.getURI()));
            return student;
        } finally {
            dataset.end();
        }*/
    }

    public void saveCommits(String datasourceId, List<GHCommit> commits) throws IOException {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (GHCommit commit : commits) {
            Resource commitResource = model.createResource(namespace + commit.getSHA1());
            model.add(commitResource, RDF.type, model.createResource(namespace + "Commit"));
            model.add(commitResource, model.createProperty(namespace + "commitTaskWritten"), model.createTypedLiteral(true));
            model.add(commitResource, model.createProperty(namespace + "commitTotal"), model.createTypedLiteral(222));
            Resource membership = getMembershipResourceByUsername(model, commit.getAuthor().getLogin());
            if (membership != null) {
                model.add(commitResource, model.createProperty(namespace + "assignedTo"), getMembershipResourceByUsername(model, commit.getAuthor().getLogin()));
            }
            model.add(datasourceResource, model.createProperty(namespace + "hasCommit"), commitResource);
        }

        dataset.commit();

    }

    public float computeMetric(String datasourceId, String operation) {

        dataset.begin(ReadWrite.READ);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);
        StmtIterator commitsIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasCommit"), (RDFNode)null);
        dataset.commit();

        switch(operation) {
            case NUMBER_COMMITS -> {
                int taskCount = 0;
                while (commitsIter.hasNext()) {
                    Statement stmt = commitsIter.next();
                }
                return 1f;
            }
            case SD -> {
                int taskCount = 0;
                while (commitsIter.hasNext()) {
                    Statement stmt = commitsIter.next();
                    }
                return 2f;
                }
            case TASK_REFERENCE -> {
                int taskCount = 0;
                while (commitsIter.hasNext()) {
                    Statement stmt = commitsIter.next();
                }
                return 3f;
                }
            case MODIFIED_LINES -> {
                int taskCount = 0;
                while (commitsIter.hasNext()) {
                    Statement stmt = commitsIter.next();
                }
                return 4f;
            }
        }
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
        DataSourceDto ds = dataSourceRepository.findById(dataSourceId);

        GitHub github = new GitHubBuilder().withOAuthToken("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").build();
        GHRepository repo = github.getRepository(ds.getOwner() + "/" + ds.getRepository());

        if (ISSUES_OBJECT.equals(objectName)) {
            List<GHIssue> issues = repo.getIssues(GHIssueState.ALL);
            saveIssues(dataSourceId, issues);
            System.out.println(issues);
        } else if (COMMITS_OBJECT.equals(objectName)) {
            List<GHCommit> commits = repo.listCommits().asList();
            saveCommits(dataSourceId, commits);
            System.out.println(commits);
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }

    public boolean supportsObject(String objectName) {
        return objectName.equals(ISSUES_OBJECT) || objectName.equals(COMMITS_OBJECT);
    }

    public boolean supportsMethod(String method) {
        return method.equals(NUMBER_COMMITS) || method.equals(TASK_REFERENCE) || method.equals(SD) || method.equals(MODIFIED_LINES);
    }
}
