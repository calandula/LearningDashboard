# Project

PS C:\Users\adria\Desktop\TFM2\SoftwareNeeded\apache-jena-fuseki-4.8.0\apache-jena-fuseki-4.8.0> ./fuseki-server --tdb1
--loc ../../../MainProject/demo/src/main/resources/data/tdb /ds

PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX qrapids: <http://www.semanticweb.org/adria/ontologies/2023/3/untitled-ontology-27#>

SELECT DISTINCT ?class ?writtentask ?commitTotal
WHERE {
?class a qrapids:Commit.
OPTIONAL { ?class qrapids:commitTaskWritten ?writtentask}
OPTIONAL { ?class qrapids:commitTotal ?commitTotal}
}
LIMIT 25