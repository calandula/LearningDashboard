package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.MembershipDto;
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
public class MembershipRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public List<MembershipDto> findAll() {
        List<MembershipDto> memberships = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Membership"))
                    .forEachRemaining(membershipResource -> {
                        MembershipDto membership = new MembershipDto();
                        membership.setUsername(membershipResource.getProperty(ResourceFactory.createProperty(namespace + "membershipUsername")).getString());
                        membership.setBasedDataSource(membershipResource.getProperty(ResourceFactory.createProperty(namespace + "sourceDS")).getString());
                        membership.setId(JenaUtils.parseId(membershipResource.getURI()));
                        memberships.add(membership);
                    });

            dataset.commit();
            return memberships;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public MembershipDto findById(String membershipId) {
        String membershipURI = namespace + membershipId;
        Resource membershipResource = ResourceFactory.createResource(membershipURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(membershipResource)) {
                return null;
            }

            String membershipUsername = model.getProperty(membershipResource, model.createProperty(namespace + "membershipUsername"))
                    .getString();
            String sourceDS = model.getProperty(membershipResource, model.createProperty(namespace + "sourceDS"))
                    .getString();


            MembershipDto membership = new MembershipDto();
            membership.setUsername(membershipUsername);
            membership.setBasedDataSource(sourceDS);
            membership.setId(JenaUtils.parseId(membershipResource.getURI()));
            return membership;
        } finally {
            dataset.end();
        }
    }

    public MembershipDto save(MembershipDto membership, String membershipId) {
        String membershipURI = membershipId == null ? namespace + UUID.randomUUID().toString() : membershipId;
        Resource membershipResource = ResourceFactory.createResource(membershipURI);
        Resource membershipClass = ResourceFactory.createResource(namespace + "Membership");
        dataset.begin(ReadWrite.WRITE);
        try {
            if (!dataset.getDefaultModel().containsResource(ResourceFactory.createResource(namespace + membership.getBasedDataSource()))) {
                throw new IllegalArgumentException("Source DS ID does not exist");
            }

            dataset.getDefaultModel()
                    .add(membershipResource, RDF.type, membershipClass)
                    .add(membershipResource, ResourceFactory.createProperty(namespace + "membershipUsername"),
                            ResourceFactory.createPlainLiteral(membership.getUsername()))
                    .add(membershipResource, ResourceFactory.createProperty(namespace + "sourceDS"),
                            ResourceFactory.createResource(namespace + membership.getBasedDataSource()));
            dataset.commit();
            MembershipDto memb = new MembershipDto(membershipURI, membership.getUsername(), membership.getBasedDataSource());
            return memb;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<MembershipDto> getMembershipsByStudentInProject(String datasourceId, String studentId) {
        String studentURI = namespace + studentId;
        Resource studentResource = ResourceFactory.createResource(studentURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(studentResource)) {
                return null;
            }

            List<Resource> membershipResources = model.listObjectsOfProperty(studentResource, model.createProperty(namespace + "hasMembership"))
                    .mapWith(RDFNode::asResource)
                    .toList();

            List<MembershipDto> memberships = new ArrayList<>();
            for (Resource membershipResource : membershipResources) {
                String sourceDS = model.getProperty(membershipResource, model.createProperty(namespace + "sourceDS"))
                        .getString();
                if (datasourceId.equals(sourceDS)) {
                    String membershipId = membershipResource.getURI().substring(namespace.length());
                    String membershipUsername = model.getProperty(membershipResource, model.createProperty(namespace + "membershipUsername"))
                            .getString();

                    MembershipDto membership = new MembershipDto();
                    membership.setUsername(membershipUsername);
                    membership.setBasedDataSource(sourceDS);
                    membership.setId(JenaUtils.parseId(membershipResource.getURI()));
                    memberships.add(membership);
                }
            }

            return memberships;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String membershipId, boolean update) {
        String membershipURI = namespace + membershipId;
        Resource membershipResource = ResourceFactory.createResource(membershipURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(membershipResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(membershipResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
