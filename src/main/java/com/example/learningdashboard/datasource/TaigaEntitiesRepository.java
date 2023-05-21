package com.example.learningdashboard.datasource;

import com.example.learningdashboard.dtos.GithubDataSourceDto;
import com.example.learningdashboard.dtos.TaigaDataSourceDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class TaigaEntitiesRepository {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl = "https://api.taiga.io";

    private static final String USER_STORIES_OBJECT = "user_stories";
    private static final String TASKS_OBJECT = "tasks";

    private static final String STORY_POINTS = "story_points";
    private static final String TASK_COUNT = "task_count";

    public void saveUserStories(String datasourceId, List<UserStory> userStories) {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (UserStory userStory : userStories) {
            Resource storyResource = model.createResource(namespace + userStory.getId());
            model.add(storyResource, RDF.type, model.createResource(namespace + "UserStory"));
            model.add(storyResource, model.createProperty(namespace + "storyPoints"), model.createTypedLiteral(userStory.getStoryPoints()));
            model.add(datasourceResource, model.createProperty(namespace + "hasUserStory"), storyResource);
        }

        dataset.commit();
    }

    public void saveTasks(String datasourceId, List<Task> tasks) {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (Task task : tasks) {
            Resource taskResource = model.createResource(namespace + task.getId());
            model.add(taskResource, RDF.type, model.createResource(namespace + "Task"));
            model.add(taskResource, model.createProperty(namespace + "taskCount"), model.createTypedLiteral(1));
            model.add(datasourceResource, model.createProperty(namespace + "hasTask"), taskResource);
        }

        dataset.commit();
    }

    public float computeMetric(String datasourceId, String operation) {

        dataset.begin(ReadWrite.READ);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);
        StmtIterator userStoriesIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasUserStory"), (RDFNode)null);
        StmtIterator tasksIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasTask"), (RDFNode)null);
        dataset.commit();

        switch(operation) {
            case STORY_POINTS -> {
                float totalStoryPoints = 0f;
                while (userStoriesIter.hasNext()) {
                    Statement stmt = userStoriesIter.next();
                    float storyPoints = stmt.getObject().asLiteral().getFloat();
                    totalStoryPoints += storyPoints;
                }
                return totalStoryPoints;
            }
            case TASK_COUNT -> {
                int totalTaskCount = 0;
                while (tasksIter.hasNext()) {
                    Statement stmt = tasksIter.next();
                    totalTaskCount += stmt.getObject().asLiteral().getInt();
                }
                return totalTaskCount;
            }
        }
        return 0;
    }

    public void retrieveData(String objectName, String dataSourceId) throws IOException {
        TaigaDataSourceDto ds = (TaigaDataSourceDto) dataSourceRepository.findById(dataSourceId);

        // Connect to Taiga API and retrieve data based on the objectName
        // Save the retrieved data using the appropriate method (saveUserStories, saveTasks)

        if (USER_STORIES_OBJECT.equals(objectName)) {
            List<UserStory> userStories = retrieveUserStories(ds);
            saveUserStories(dataSourceId, userStories);
            System.out.println(userStories);
        } else if (TASKS_OBJECT.equals(objectName)) {
            List<Task> tasks = retrieveTasks(ds);
            saveTasks(dataSourceId, tasks);
            System.out.println(tasks);
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }

    public List<Task> retrieveTasks(TaigaDataSourceDto dataSourceDto) throws IOException {
        // Make an API request to retrieve tasks data
        String url = baseUrl + "/tasks?project=" + projectId;
        String responseJson = makeApiRequest(url);

        // Parse JSON using Jackson library
        JsonNode jsonNode = objectMapper.readTree(responseJson);
        List<Task> tasks = new ArrayList<>();

        // Extract task data from the JSON
        for (JsonNode taskNode : jsonNode) {
            Task task = new Task();
            task.setId(taskNode.get("id").asText());
            task.setTitle(taskNode.get("title").asText());
            task.setDescription(taskNode.get("description").asText());
            // Set other task properties as needed
            tasks.add(task);
        }

        return tasks;
    }

    public List<UserStory> retrieveUserStories(TaigaDataSourceDto dataSourceDto) throws IOException {
        // Make an API request to retrieve user stories data
        String url = baseUrl + "/userstories?project=" + projectId;
        String responseJson = makeApiRequest(url);

        // Parse JSON using Jackson library
        JsonNode jsonNode = objectMapper.readTree(responseJson);
        List<UserStory> userStories = new ArrayList<>();

        // Extract user story data from the JSON
        for (JsonNode userStoryNode : jsonNode) {
            UserStory userStory = new UserStory();
            userStory.setId(userStoryNode.get("id").asText());
            userStory.setTitle(userStoryNode.get("subject").asText());
            userStory.setDescription(userStoryNode.get("description").asText());
            // Set other user story properties as needed
            userStories.add(userStory);
        }

        return userStories;
    }

    private String makeApiRequest(String url) throws IOException {
        // Perform HTTP request to the Taiga API and retrieve the JSON response
        // Implement the logic to make the API request and retrieve the JSON response
        return url;
    }

    public boolean supportsObject(String objectName) {
        return objectName.equals(USER_STORIES_OBJECT) || objectName.equals(TASKS_OBJECT);
    }

    public boolean supportsMethod(String method) {
        return method.equals(STORY_POINTS) || method.equals(TASK_COUNT);
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserStory {
        private String id;
        private String title;
        private String description;
        private String acceptanceCriteria;
        private String pattern;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Task {
        private String id;
        private String title;
        private String description;
        private String assignedTo;
        private Boolean isClosed;
    }
}


