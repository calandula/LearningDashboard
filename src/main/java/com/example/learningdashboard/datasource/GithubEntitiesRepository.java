package com.example.learningdashboard.datasource;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
public class GithubEntitiesRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

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

    public void saveCommits(String datasourceId, List<GHCommit> commits) {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            Resource datasourceResource = model.createResource(namespace + datasourceId);

            for (GHCommit commit : commits) {
                Resource commitResource = model.createResource(namespace + commit.getSHA1());
                model.add(commitResource, RDF.type, model.createResource(namespace + "Commit"));
                model.add(commitResource, model.createProperty(namespace + "commitTaskWritten"), model.createTypedLiteral(true));
                model.add(commitResource, model.createProperty(namespace + "commitTotal"), model.createTypedLiteral(222));
                model.add(commitResource, model.createProperty(namespace + "assignedTo"), getMembershipResourceByUsername(model, commit.getAuthor().getLogin()));
                model.add(datasourceResource, model.createProperty(namespace + "hasCommit"), commitResource);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            dataset.end();
        }
    }

    private Resource getMembershipResourceByUsername(Model model, String username) {
        StmtIterator iter = model.listStatements(null, RDF.type, model.createResource(namespace + "Membership"));
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            Resource membershipResource = stmt.getSubject();
            String membershipUsername = model.getProperty(membershipResource, model.createProperty(namespace + "username")).getString();
            if (membershipUsername.equals(username)) {
                return membershipResource;
            }
        }
        return null;
    }

}
