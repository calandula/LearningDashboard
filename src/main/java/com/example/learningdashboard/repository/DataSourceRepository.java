package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.DataSourceDto;
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
public class DataSourceRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public List<DataSourceDto> findAll() {
        List<DataSourceDto> dsList = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "DataSource"))
                    .forEachRemaining(dsResource -> {
                        DataSourceDto ds = new DataSourceDto();
                        ds.setRepository(dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceRepository")).getString());
                        ds.setOwner(dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceOwner")).getString());
                        ds.setId(JenaUtils.parseId(dsResource.getURI()));
                        dsList.add(ds);
                    });

            dataset.commit();
            return dsList;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public DataSourceDto findById(String dsId) {
        String dsURI = namespace + dsId;
        Resource dsResource = ResourceFactory.createResource(dsURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(dsResource)) {
                return null;
            }

            String datasourceRepository = model.getProperty(dsResource, model.createProperty(namespace + "datasourceRepository"))
                    .getString();
            String datasourceOwner = model.getProperty(dsResource, model.createProperty(namespace + "datasourceOwner"))
                    .getString();

            DataSourceDto ds = new DataSourceDto();
            ds.setRepository(datasourceRepository);
            ds.setOwner(datasourceOwner);
            ds.setId(JenaUtils.parseId(dsResource.getURI()));
            return ds;
        } finally {
            dataset.end();
        }
    }

    public DataSourceDto save(DataSourceDto ds, String dsId) {
        String dsURI = dsId == null ? namespace + UUID.randomUUID().toString() : dsId;
        Resource dsResource = ResourceFactory.createResource(dsURI);
        Resource dsClass = ResourceFactory.createResource(namespace + "DataSource");
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel()
                    .add(dsResource, RDF.type, dsClass)
                    .add(dsResource, ResourceFactory.createProperty(namespace + "datasourceRepository"),
                            ResourceFactory.createPlainLiteral(ds.getRepository()))
                    .add(dsResource, ResourceFactory.createProperty(namespace + "datasourceOwner"),
                            ResourceFactory.createPlainLiteral(ds.getOwner()));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<DataSourceDto> findByProject(String projectId) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(projectResource)) {
                return null;
            }

            List<DataSourceDto> dsList = new ArrayList<>();

            StmtIterator stmtIterator = model.listStatements(projectResource, model.createProperty(namespace + "hasDataSource"), (RDFNode) null);
            while (stmtIterator.hasNext()) {
                Resource dsResource = stmtIterator.next().getObject().asResource();

                DataSourceDto ds = new DataSourceDto();
                ds.setRepository(dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceRepository")).getString());
                ds.setOwner(dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceOwner")).getString());
                ds.setId(JenaUtils.parseId(dsResource.getURI()));
                dsList.add(ds);
            }

            dataset.commit();
            return dsList;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String dsId, boolean update) {
        String dsURI = namespace + dsId;
        Resource dsResource = ResourceFactory.createResource(dsURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(dsResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(dsResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
