package com.example.learningdashboard.config;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.transaction.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;

@Configuration
public class JenaConfig {

    private static final String TDB_DIRECTORY = "src/main/resources/data/tdb";
    private static final String ENTITIES_FILE = "data/ontology.owl";
    private static final String ONTOLOGY_FILE = "data/ontology.owl";

    @Bean
    public String prefixes() {
        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX qrapids-ontology-2: <http://www.semanticweb.org/adria/ontologies/2023/1/qrapids-ontology-2#>\n" +
                "PREFIX untitled-ontology-25: <http://www.semanticweb.org/adria/ontologies/2023/3/untitled-ontology-25#>";
    }

    @Bean
    public Dataset dataset() {
        File tdbDir = new File(TDB_DIRECTORY);
        if (tdbDir.list().length == 0) {
            // TDB directory is empty, initialize a new dataset
            Dataset dataset = TDBFactory.createDataset(TDB_DIRECTORY);
            try {
                dataset.begin(ReadWrite.WRITE);
                Model model = dataset.getDefaultModel();
                model.read(ONTOLOGY_FILE);
                dataset.commit();
            } catch (Exception e) {
                dataset.abort();
                throw e;
            }
            return dataset;
        } else {
            // TDB directory already exists, return the existing dataset
            return TDBFactory.createDataset(TDB_DIRECTORY);
        }
    }

    @Bean
    public Model ontModel() {
        Model ontModel = ModelFactory.createDefaultModel();
        InputStream entitiesStream = getClass().getClassLoader().getResourceAsStream(ENTITIES_FILE);
        InputStream ontologyStream = getClass().getClassLoader().getResourceAsStream(ONTOLOGY_FILE);
        ontModel.read(entitiesStream, null, "RDF/XML");
        ontModel.read(ontologyStream, null, "RDF/XML");
        return ontModel;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
