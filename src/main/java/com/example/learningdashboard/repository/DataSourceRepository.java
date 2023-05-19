package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.DataSourceDto;
import com.example.learningdashboard.dtos.GithubDataSourceDto;
import com.example.learningdashboard.dtos.TaigaDataSourceDto;
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
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type)
                    .filterKeep(dsResource -> {
                        dataset.commit();
                        String datasourceType = getClass(JenaUtils.parseId(dsResource.getURI()));
                        dataset.begin(ReadWrite.READ);
                        return datasourceType != null && (datasourceType.equals("GithubDataSource") || datasourceType.equals("TaigaDataSource"));
                    })
                    .forEachRemaining(dsResource -> {
                        DataSourceDto ds;
                        dataset.commit();
                        String datasourceType = getClass(JenaUtils.parseId(dsResource.getURI()));
                        dataset.begin(ReadWrite.READ);
                        if (datasourceType.equals("GithubDataSource")) {
                            ds = new GithubDataSourceDto();
                            String datasourceRepository = dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceRepository")).getString();
                            ((GithubDataSourceDto) ds).setRepository(datasourceRepository);
                            String datasourceOwner = dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceOwner")).getString();
                            ((GithubDataSourceDto) ds).setOwner(datasourceOwner);
                        } else if (datasourceType.equals("TaigaDataSource")) {
                            ds = new TaigaDataSourceDto();
                            String backlogId = dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceBacklogID")).getString();
                            ((TaigaDataSourceDto) ds).setBacklogID(backlogId);
                        } else {
                            ds = new DataSourceDto();
                        }

                        ds.setAccessToken(dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceToken")).getString());
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

    public String getClass(String datasourceId) {
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            Resource subject = model.getResource(namespace + datasourceId);
            Property predicate = RDF.type;
            StmtIterator iterator = model.listStatements(subject, predicate, (RDFNode) null);

            if (iterator.hasNext()) {
                Statement statement = iterator.next();
                RDFNode object = statement.getObject();

                if (object.isResource()) {
                    return object.asResource().getURI().replace(namespace, "");
                }
            }
            return null;
        } catch (Exception e) {
            throw e;
        } finally {
            dataset.end();
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
            dataset.end();
            String datasourceType = getClass(dsId);
            dataset.begin(ReadWrite.READ);
            DataSourceDto ds;
            if (datasourceType.equals("GithubDataSource")) {
                ds = new GithubDataSourceDto();
                String datasourceRepository = model.getProperty(dsResource, model.createProperty(namespace + "datasourceRepository"))
                        .getString();
                ((GithubDataSourceDto) ds).setRepository(datasourceRepository);
                String datasourceOwner = model.getProperty(dsResource, model.createProperty(namespace + "datasourceOwner"))
                        .getString();
                ((GithubDataSourceDto) ds).setOwner(datasourceOwner);
            } else if (datasourceType.equals("TaigaDataSource")) {
                ds = new TaigaDataSourceDto();
                String backlogId = model.getProperty(dsResource, model.createProperty(namespace + "datasourceBacklogID"))
                        .getString();
                ((TaigaDataSourceDto) ds).setBacklogID(backlogId);
            } else {
                ds = new DataSourceDto();
            }

            ds.setAccessToken(model.getProperty(dsResource, model.createProperty(namespace + "datasourceToken"))
                    .getString());
            ds.setId(JenaUtils.parseId(dsResource.getURI()));
            return ds;
        } finally {
            dataset.end();
        }
    }

    public DataSourceDto save(DataSourceDto ds, String dsId) {
        dsId = dsId == null ? UUID.randomUUID().toString() : dsId;
        String dsURI = namespace + dsId;
        Resource dsResource = ResourceFactory.createResource(dsURI);
        Resource dsClass = ResourceFactory.createResource(namespace + ds.getType());
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel()
                    .add(dsResource, RDF.type, dsClass)
                    .add(dsResource, ResourceFactory.createProperty(namespace + "datasourceToken"),
                            ResourceFactory.createPlainLiteral(ds.getAccessToken()));

            if (ds instanceof GithubDataSourceDto) {
                dataset.getDefaultModel()
                        .add(dsResource, ResourceFactory.createProperty(namespace + "datasourceRepository"),
                                ResourceFactory.createPlainLiteral(((GithubDataSourceDto) ds).getRepository()))
                        .add(dsResource, ResourceFactory.createProperty(namespace + "datasourceOwner"),
                                ResourceFactory.createPlainLiteral(((GithubDataSourceDto) ds).getOwner()));
            } else if (ds instanceof TaigaDataSourceDto) {
                dataset.getDefaultModel()
                        .add(dsResource, ResourceFactory.createProperty(namespace + "datasourceBacklogID"),
                                ResourceFactory.createPlainLiteral(((TaigaDataSourceDto) ds).getBacklogID()));
            }

            dataset.commit();
            ds.setId(dsId);
            return ds;
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
                DataSourceDto ds;
                String datasourceType = getClass(JenaUtils.parseId(dsResource.getURI()));
                if (datasourceType.equals("GithubDataSource")) {
                    ds = new GithubDataSourceDto();
                    String datasourceRepository = dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceRepository")).getString();
                    ((GithubDataSourceDto) ds).setRepository(datasourceRepository);
                    String datasourceOwner = dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceOwner")).getString();
                    ((GithubDataSourceDto) ds).setOwner(datasourceOwner);
                } else if (datasourceType.equals("TaigaDataSource")) {
                    ds = new TaigaDataSourceDto();
                    String backlogId = dsResource.getProperty(ResourceFactory.createProperty(namespace + "datasourceBacklogID")).getString();
                    ((TaigaDataSourceDto) ds).setBacklogID(backlogId);
                } else {
                    ds = new DataSourceDto();
                }

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
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(dsResource)) {
                throw new IllegalArgumentException("Data source not found");
            }

            if (update) {
                StmtIterator stmtIterator = model.listStatements(null, null, dsResource);
                while (stmtIterator.hasNext()) {
                    Statement statement = stmtIterator.next();
                    model.remove(statement);
                }
            } else {
                model.removeAll(dsResource, null, (RDFNode) null);
            }

            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }
}
