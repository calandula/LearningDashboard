package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.SIDto;
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
public class SIRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public SIDto save(SIDto si, String siId) {
        siId = siId == null ? UUID.randomUUID().toString() : siId;
        String siURI = namespace + siId;
        Resource siResource = ResourceFactory.createResource(siURI);
        Resource siClass = ResourceFactory.createResource(namespace + "SI");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> qfResources = si.getQualityFactors().stream()
                    .map(qfId -> ResourceFactory.createResource(namespace + qfId))
                    .filter(qfResource -> dataset.getDefaultModel().containsResource(qfResource))
                    .toList();
            if (qfResources.size() != si.getQualityFactors().size()) {
                throw new IllegalArgumentException("One or more quality factors IDs do not exist in the dataset.");
            }
            dataset.getDefaultModel()
                    .add(siResource, RDF.type, siClass)
                    .add(siResource, ResourceFactory.createProperty(namespace + "SIName"),
                            ResourceFactory.createPlainLiteral(si.getName()))
                    .add(siResource, ResourceFactory.createProperty(namespace + "SIDescription"),
                            ResourceFactory.createPlainLiteral(si.getDescription()))
                    .add(siResource, ResourceFactory.createProperty(namespace + "SIAssessmentModel"),
                            ResourceFactory.createPlainLiteral(si.getAssessmentModel()));


            qfResources.forEach(qfResource ->
                    dataset.getDefaultModel().add(siResource,
                            ResourceFactory.createProperty(namespace + "hasQF"),
                            qfResource));

            dataset.commit();
            si.setId(siId);
            return si;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<SIDto> findAll() {
        List<SIDto> sis = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "SI"))
                    .forEachRemaining(siResource -> {
                        SIDto si = new SIDto();
                        si.setName(siResource.getProperty(ResourceFactory.createProperty(namespace + "SIName")).getString());
                        si.setDescription(siResource.getProperty(ResourceFactory.createProperty(namespace + "SIDescription")).getString());
                        si.setAssessmentModel(siResource.getProperty(ResourceFactory.createProperty(namespace + "SIAssessmentModel")).getString());
                        si.setQualityFactors((ArrayList<String>) siResource.listProperties(ResourceFactory.createProperty(namespace + "hasQF"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        si.setId(JenaUtils.parseId(siResource.getURI()));
                        sis.add(si);
                    });

            dataset.commit();
            return sis;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public SIDto findById(String siId) {
        String siURI = namespace + siId;
        Resource siResource = ResourceFactory.createResource(siURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(siResource)) {
                return null;
            }

            String SIName = model.getProperty(siResource, model.createProperty(namespace + "SIName"))
                    .getString();
            String SIDescription = model.getProperty(siResource, model.createProperty(namespace + "SIDescription"))
                    .getString();
            String SIAssessmentModel = model.getProperty(siResource, model.createProperty(namespace + "SIAssessmentModel"))
                    .getString();
            List<String> QFs = model.listObjectsOfProperty(siResource, model.createProperty(namespace + "hasQF"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            SIDto si = new SIDto();
            si.setName(SIName);
            si.setDescription(SIDescription);
            si.setAssessmentModel(SIAssessmentModel);
            si.setQualityFactors((ArrayList<String>) QFs);
            si.setId(JenaUtils.parseId(siResource.getURI()));
            return si;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String siId, boolean update) {
        String siURI = namespace + siId;
        Resource siResource = ResourceFactory.createResource(siURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(siResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            } else {
                dataset.getDefaultModel().removeAll(siResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}

