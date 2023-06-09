# Learning Dashboard with KGs

This is a Java Maven project that demonstrates a basic setup and provides instructions for building and executing the program.

## Prerequisites

Before building and running the program, ensure that the following prerequisites are met:

- Java Development Kit (JDK) is installed. You can download it from [Oracle's website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).
- Apache Maven is installed. You can download it from the [Apache Maven website](https://maven.apache.org/download.cgi) and follow the installation instructions.

## Building the Project

To build the project, follow these steps:

1. Open a terminal or command prompt.
2. Navigate to the project's root directory (where the `pom.xml` file is located).
3. Run the following command to build the project:

mvn clean package

This command will compile the source code, run tests, and package the application into an executable JAR file.

4. After a successful build, the JAR file will be generated in the `target/` directory.

## Running the Program

To execute the program, follow these steps:

1. Ensure that the project has been built using the previous instructions.
2. Open a terminal or command prompt.
3. Navigate to the project's root directory.
4. Run the following command:

java -jar <jar-filename-in-target>.jar

This command will start the program and execute the main class.

## Visualize data on Fuseki

Install [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/)

Having apache Jena Fuseki already in your path, execute:

fuseki-server --tdb1 --loc <path_to_tdb_in_src_main_resources_data> /ds

