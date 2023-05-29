package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.StudentDto;
import com.example.learningdashboard.utils.JenaUtils;
import com.example.learningdashboard.utils.Membership;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class StudentRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public List<StudentDto> findAll() {
        List<StudentDto> students = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        Model model = dataset.getDefaultModel();
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Student"))
                    .forEachRemaining(studentResource -> {
                        StudentDto student = new StudentDto();
                        student.setName(studentResource.getProperty(ResourceFactory.createProperty(namespace + "studentName")).getString());
                        ArrayList<Membership<String, String>> memberships = new ArrayList<>();
                        StmtIterator membershipIter = model.listStatements(studentResource, ResourceFactory.createProperty(namespace + "hasMembership"), (RDFNode) null);
                        while (membershipIter.hasNext()) {
                            Statement stmt = membershipIter.next();
                            Resource membershipResource = stmt.getObject().asResource();
                            Resource dsResource = membershipResource.getProperty(ResourceFactory.createProperty(namespace + "sourceDS")).getObject().asResource();
                            String username = membershipResource.getProperty(ResourceFactory.createProperty(namespace + "membershipUsername")).getString();
                            String metricId = JenaUtils.parseId(dsResource.getURI());
                            memberships.add(new Membership<>(metricId, username));
                        }
                        student.setMemberships(memberships);
                        student.setId(JenaUtils.parseId(studentResource.getURI()));
                        students.add(student);
                    });

            dataset.commit();
            return students;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public StudentDto findById(String studentId) {
        String studentURI = namespace + studentId;
        Resource studentResource = ResourceFactory.createResource(studentURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(studentResource)) {
                return null;
            }

            String studentName = model.getProperty(studentResource, model.createProperty(namespace + "studentName"))
                    .getString();
            ArrayList<Membership<String, String>> memberships = new ArrayList<>();
            StmtIterator membershipIter = model.listStatements(studentResource, ResourceFactory.createProperty(namespace + "hasMembership"), (RDFNode) null);
            while (membershipIter.hasNext()) {
                Statement stmt = membershipIter.next();
                Resource membershipResource = stmt.getObject().asResource();
                Resource dsResource = membershipResource.getProperty(ResourceFactory.createProperty(namespace + "sourceDS")).getObject().asResource();
                String username = membershipResource.getProperty(ResourceFactory.createProperty(namespace + "membershipUsername")).getString();
                String dsId = JenaUtils.parseId(dsResource.getURI());
                memberships.add(new Membership<>(dsId, username));
            }

            StudentDto student = new StudentDto();
            student.setName(studentName);
            student.setMemberships(memberships);
            student.setId(JenaUtils.parseId(studentResource.getURI()));
            return student;
        } finally {
            dataset.end();
        }
    }

    public StudentDto save(StudentDto student, String studentId) {
        studentId = studentId == null ? UUID.randomUUID().toString() : studentId;
        String studentURI = namespace + studentId;
        Resource studentResource = ResourceFactory.createResource(studentURI);
        Resource studentClass = ResourceFactory.createResource(namespace + "Student");
        dataset.begin(ReadWrite.WRITE);
        try {
            checkIds(student);

            createMemberships(student, studentResource);

            dataset.getDefaultModel()
                    .add(studentResource, RDF.type, studentClass)
                    .add(studentResource, ResourceFactory.createProperty(namespace + "studentName"),
                            ResourceFactory.createPlainLiteral(student.getName()));

            dataset.commit();
            student.setId(studentId);
            return student;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    private void createMemberships(StudentDto student, Resource studentResource) {
        for (Membership<String, String> membershipPair : student.getMemberships()) {
            String dsId = membershipPair.getId();
            String username = membershipPair.getUsername();

            String membershipId = UUID.randomUUID().toString();
            String membershipURI = namespace + membershipId;
            Resource membershipResource = ResourceFactory.createResource(membershipURI);
            Resource membershipClass = ResourceFactory.createResource(namespace + "Membership");
            dataset.getDefaultModel().add(membershipResource, RDF.type, membershipClass);
            dataset.getDefaultModel().add(membershipResource, ResourceFactory.createProperty(namespace + "membershipUsername"), username);
            dataset.getDefaultModel().add(studentResource, ResourceFactory.createProperty(namespace + "hasMembership"), membershipResource);

            Resource dsResource = ResourceFactory.createResource(namespace + dsId);
            dataset.getDefaultModel().add(membershipResource, ResourceFactory.createProperty(namespace + "sourceDS"), dsResource);
        }
    }

    private void checkIds(StudentDto student) {
        List<Resource> dsResources = student.getMemberships().stream()
                .map(membershipPair -> ResourceFactory.createResource(namespace + membershipPair.getId()))
                .filter(membershipResource -> dataset.getDefaultModel().containsResource(membershipResource))
                .toList();
        if (dsResources.size() != student.getMemberships().size()) {
            throw new IllegalArgumentException("One or more datasource IDs do not exist in the dataset.");
        }
    }


    public void deleteById(String studentId, boolean update) {
        String studentURI = namespace + studentId;
        Resource studentResource = ResourceFactory.createResource(studentURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(studentResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            } else {
                dataset.getDefaultModel().removeAll(studentResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
