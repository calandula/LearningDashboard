package com.example.learningdashboard.datasource;

import com.example.learningdashboard.dtos.TaigaDataSourceDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            model.add(datasourceResource, model.createProperty(namespace + "hasUserStory"), storyResource);
            model.add(storyResource, model.createProperty(namespace + "userstorySubject"), model.createTypedLiteral(userStory.getSubject()));
            model.add(storyResource, model.createProperty(namespace + "userstoryDescription"), model.createTypedLiteral(userStory.getDescription()));
            model.add(storyResource, model.createProperty(namespace + "userstoryIsBlocked"), model.createTypedLiteral(userStory.getIsBlocked()));
            model.add(storyResource, model.createProperty(namespace + "userstoryIsClosed"), model.createTypedLiteral(userStory.getIsClosed()));
            Resource membership = getMembershipResourceByUsername(model, userStory.getAssignedTo());
            if (membership != null) {
                model.add(storyResource, model.createProperty(namespace + "userstoryAssignedTo"), getMembershipResourceByUsername(model, userStory.getAssignedTo()));
            }
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
            model.add(datasourceResource, model.createProperty(namespace + "hasTask"), taskResource);
            model.add(taskResource, model.createProperty(namespace + "taskSubject"), model.createTypedLiteral(task.getSubject()));
            model.add(taskResource, model.createProperty(namespace + "taskIsBlocked"), model.createTypedLiteral(task.getIsBlocked()));
            model.add(taskResource, model.createProperty(namespace + "taskIsClosed"), model.createTypedLiteral(task.getIsClosed()));
            Resource membership = getMembershipResourceByUsername(model, task.getAssignedTo());
            if (membership != null) {
                model.add(taskResource, model.createProperty(namespace + "taskAssignedTo"), getMembershipResourceByUsername(model, task.getAssignedTo()));
            }
        }

        dataset.commit();
    }

    private Resource getMembershipResourceByUsername(Model model, String username) {
        StmtIterator iter = model.listStatements(null, RDF.type, model.createResource(namespace + "Membership"));
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            Resource membershipResource = stmt.getSubject();
            String membershipUsername = model.getProperty(membershipResource, model.createProperty(namespace + "membershipUsername")).getString();
            if (membershipUsername.equals(username)) {
                return membershipResource;
            }
        }
        return null;
    }

    public float computeMetric(String datasourceId, String operation, String target) {

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
                    tasksIter.next();
                    totalTaskCount += 1;
                }
                return totalTaskCount;
            }
        }
        return 0;
    }

    public void retrieveData(String objectName, String dataSourceId) throws IOException {
        TaigaDataSourceDto ds = (TaigaDataSourceDto) dataSourceRepository.findById(dataSourceId);

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

    public List<Task> retrieveTasks(TaigaDataSourceDto ds) throws IOException {
        String url = baseUrl + "/tasks";
        String responseJson = makeApiRequest(url, ds.getAccessToken());

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        List<Task> tasks = new ArrayList<>();

        for (JsonNode taskNode : jsonNode) {
            Task task = new Task();
            task.setId(String.valueOf(UUID.randomUUID()));
            task.setSubject(taskNode.get("subject").asText());
            JsonNode assignedToExtraInfoNode = taskNode.get("assigned_to_extra_info");
            if (assignedToExtraInfoNode != null && assignedToExtraInfoNode.has("username")) {
                task.setAssignedTo(assignedToExtraInfoNode.get("username").asText());
            }
            task.setIsClosed(taskNode.get("is_closed").asBoolean());
            task.setIsBlocked(taskNode.get("is_blocked").asBoolean());
            tasks.add(task);
        }

        return tasks;
    }

    public List<UserStory> retrieveUserStories(TaigaDataSourceDto ds) throws IOException {
        String url = baseUrl + "/userstories";
        String responseJson = makeApiRequest(url, ds.getAccessToken());

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        List<UserStory> userStories = new ArrayList<>();

        for (JsonNode userStoryNode : jsonNode) {
            UserStory userStory = new UserStory();
            userStory.setId(String.valueOf(UUID.randomUUID()));
            userStory.setSubject(userStoryNode.get("subject").asText());
            userStory.setDescription(userStoryNode.get("description").asText());
            JsonNode assignedToExtraInfoNode = userStoryNode.get("assigned_to_extra_info");
            if (assignedToExtraInfoNode != null && assignedToExtraInfoNode.has("username")) {
                userStory.setAssignedTo(assignedToExtraInfoNode.get("username").asText());
            }
            userStory.setIsClosed(userStoryNode.get("is_closed").asBoolean());
            userStory.setIsBlocked(userStoryNode.get("is_blocked").asBoolean());
            userStories.add(userStory);
        }

        return userStories;
    }

    private String makeApiRequest(String url, String token) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity());
        }
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
        private String subject;
        private String description;
        private String assignedTo;
        private Boolean isBlocked;
        private Boolean isClosed;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Task {
        private String id;
        private String subject;
        private String assignedTo;
        private Boolean isBlocked;
        private Boolean isClosed;
    }
}


