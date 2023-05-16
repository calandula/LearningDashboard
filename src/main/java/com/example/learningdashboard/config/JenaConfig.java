package com.example.learningdashboard.config;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class JenaConfig {

    private static final String TDB_DIRECTORY = "src/main/resources/data/tdb";
    private static final String ENTITIES_FILE = "data/ontology-final.owl";
    private static final String ONTOLOGY_FILE = "data/ontology-final.owl";

    @Bean
    public String prefixes() {
        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX qrapids: <http://www.semanticweb.org/adria/ontologies/2023/3/untitled-ontology-27#>";
    }

    @Bean
    public String namespace() {
        return "http://www.semanticweb.org/adria/ontologies/2023/3/untitled-ontology-27#";
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

}
