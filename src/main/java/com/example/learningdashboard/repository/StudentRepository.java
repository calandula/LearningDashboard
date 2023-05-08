package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.StudentDto;
import com.example.learningdashboard.utils.JenaUtils;
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
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Student"))
                    .forEachRemaining(studentResource -> {
                        StudentDto student = new StudentDto();
                        student.setName(studentResource.getProperty(ResourceFactory.createProperty(namespace + "studentName")).getString());
                        student.setMemberships((ArrayList<String>) studentResource.listProperties(ResourceFactory.createProperty(namespace + "hasMembership"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
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
        }
    }

    public StudentDto save(StudentDto student, String studentId) {
        String studentURI = studentId == null ? namespace + UUID.randomUUID().toString() : studentId;
        Resource studentResource = ResourceFactory.createResource(studentURI);
        Resource studentClass = ResourceFactory.createResource(namespace + "Student");
        dataset.begin(ReadWrite.WRITE);
        try {
            if (!student.getMemberships().isEmpty()) {
                List<Resource> studentResources = student.getMemberships().stream()
                        .map(membershipId -> ResourceFactory.createResource(namespace + membershipId))
                        .filter(membershipResource -> dataset.getDefaultModel().containsResource(membershipResource))
                        .toList();
                if (studentResources.size() != student.getMemberships().size()) {
                    throw new IllegalArgumentException("One or more membership IDs do not exist in the dataset.");
                }

                studentResources.forEach(membershipResource ->
                        dataset.getDefaultModel().add(studentResource,
                                ResourceFactory.createProperty(namespace + "hasMembership"),
                                membershipResource));
            }

            dataset.getDefaultModel()
                    .add(studentResource, RDF.type, studentClass)
                    .add(studentResource, ResourceFactory.createProperty(namespace + "studentName"),
                            ResourceFactory.createPlainLiteral(student.getName()));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<StudentDto> findByProject(String projectId) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(projectResource)) {
                return null;
            }

            List<StudentDto> students = new ArrayList<>();

            StmtIterator stmtIterator = model.listStatements(projectResource, model.createProperty(namespace + "hasStudent"), (RDFNode) null);
            while (stmtIterator.hasNext()) {
                Resource studentResource = stmtIterator.next().getObject().asResource();

                StudentDto student = new StudentDto();
                student.setName(studentResource.getProperty(ResourceFactory.createProperty(namespace + "studentName")).getString());
                student.setMemberships((ArrayList<String>) studentResource.listProperties(ResourceFactory.createProperty(namespace + "hasMembership"))
                        .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                        .mapWith(Resource::getLocalName).toList());
                student.setId(JenaUtils.parseId(studentResource.getURI()));

                students.add(student);
            }

            dataset.commit();
            return students;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
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
            }
            else {
                dataset.getDefaultModel().removeAll(studentResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public void assignMembership(String studentId, String savedMembershipId) {
        String studentURI = namespace + studentId;
        Resource studentResource = ResourceFactory.createResource(studentURI);
        Resource membershipResource = ResourceFactory.createResource(savedMembershipId);
        dataset.begin(ReadWrite.WRITE);
        try {
            // Check if the student and membership exist in the dataset
            if (!dataset.getDefaultModel().containsResource(studentResource)) {
                throw new RuntimeException("Student with ID " + studentId + " does not exist");
            }
            if (!dataset.getDefaultModel().containsResource(membershipResource)) {
                throw new RuntimeException("Membership with ID " + savedMembershipId + " does not exist");
            }

            // Create a hasMembership relation between the student and membership
            Resource student = dataset.getDefaultModel().getResource(studentId);
            Resource membership = dataset.getDefaultModel().getResource(savedMembershipId);
            Property hasMembership = dataset.getDefaultModel().createProperty(namespace + "hasMembership");
            student.addProperty(hasMembership, membership);

            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw new RuntimeException(e.getMessage());
        } finally {
            dataset.end();
        }
    }
}
