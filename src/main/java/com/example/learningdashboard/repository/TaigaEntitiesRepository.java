package com.example.learningdashboard.repository;

import com.example.learningdashboard.datasource_model.Task;
import com.example.learningdashboard.datasource_model.UserStory;
import com.example.learningdashboard.dtos.TaigaDataSourceDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Repository
public class TaigaEntitiesRepository {

    private static final String USER_STORIES_OBJECT = "user_stories";
    private static final String TASKS_OBJECT = "tasks";
    private static final String TASK_COUNT = "task_count";
    private static final String USERSTORY_COUNT = "userstory_count";
    private static final String USERSTORY_INDIVIDUAL_COUNT = "userstory_individual_count";
    private static final String TASK_INDIVIDUAL_COUNT = "task_individual_count";
    private final String baseUrl = "https://api.taiga.io/api/v1";
    @Autowired
    private DataSourceRepository dataSourceRepository;
    @Autowired
    private String prefixes;
    @Autowired
    private Dataset dataset;
    @Autowired
    private String namespace;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void saveUserStories(String datasourceId, List<UserStory> userStories) {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (UserStory userStory : userStories) {
            Resource storyResource = model.createResource(namespace + UUID.randomUUID());
            model.add(storyResource, RDF.type, model.createResource(namespace + "UserStory"));
            model.add(datasourceResource, model.createProperty(namespace + "hasUserStory"), storyResource);
            model.add(storyResource, model.createProperty(namespace + "userstorySubject"), model.createTypedLiteral(userStory.getSubject()));
            model.add(storyResource, model.createProperty(namespace + "userstoryIsBlocked"), model.createTypedLiteral(userStory.getIsBlocked()));
            model.add(storyResource, model.createProperty(namespace + "userstoryIsClosed"), model.createTypedLiteral(userStory.getIsClosed()));
            Resource membership = getMembershipResourceByUsername(model, userStory.getAssignedTo());
            if (membership != null) {
                model.add(storyResource, model.createProperty(namespace + "assignedTo"), membership);
            }
        }

        dataset.commit();
    }

    public void saveTasks(String datasourceId, List<Task> tasks) {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        Resource datasourceResource = model.createResource(namespace + datasourceId);

        for (Task task : tasks) {
            Resource taskResource = model.createResource(namespace + UUID.randomUUID());
            model.add(taskResource, RDF.type, model.createResource(namespace + "Task"));
            model.add(datasourceResource, model.createProperty(namespace + "hasTask"), taskResource);
            model.add(taskResource, model.createProperty(namespace + "taskSubject"), model.createTypedLiteral(task.getSubject()));
            model.add(taskResource, model.createProperty(namespace + "taskIsBlocked"), model.createTypedLiteral(task.getIsBlocked()));
            model.add(taskResource, model.createProperty(namespace + "taskIsClosed"), model.createTypedLiteral(task.getIsClosed()));
            Resource membership = getMembershipResourceByUsername(model, task.getAssignedTo());
            if (membership != null) {
                model.add(taskResource, model.createProperty(namespace + "assignedTo"), membership);
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
        try {
            Model model = dataset.getDefaultModel();
            Resource datasourceResource = model.createResource(namespace + datasourceId);
            StmtIterator userStoriesIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasUserStory"), (RDFNode) null);
            StmtIterator tasksIter = model.listStatements(datasourceResource, model.createProperty(namespace + "hasTask"), (RDFNode) null);

            switch (operation) {
                case USERSTORY_COUNT -> {
                    int totalUserstories = 0;
                    while (userStoriesIter.hasNext()) {
                        Statement stmt = userStoriesIter.next();
                        totalUserstories++;
                    }
                    return totalUserstories;
                }
                case TASK_COUNT -> {
                    int totalTasks = 0;
                    while (tasksIter.hasNext()) {
                        Statement stmt = tasksIter.next();
                        totalTasks++;
                    }
                    return totalTasks;
                }
                case USERSTORY_INDIVIDUAL_COUNT -> {
                    int totalUserstories = 0;
                    while (userStoriesIter.hasNext()) {
                        Statement stmt = userStoriesIter.next();
                        Resource issueResource = stmt.getObject().asResource();
                        if (hasAssignedTo(issueResource, target, model)) {
                            totalUserstories++;
                        }
                    }
                    return totalUserstories;
                }
                case TASK_INDIVIDUAL_COUNT -> {
                    int totalTasks = 0;
                    while (tasksIter.hasNext()) {
                        Statement stmt = tasksIter.next();
                        Resource commitResource = stmt.getObject().asResource();
                        if (hasAssignedTo(commitResource, target, model)) {
                            totalTasks++;
                        }
                    }
                    return totalTasks;
                }
                default -> {
                    throw new IllegalArgumentException("Unsupported operation: " + operation);
                }
            }
        } finally {
            dataset.end();
        }
    }

    private boolean hasAssignedTo(Resource resource, String target, Model model) {
        Property assignedToProperty = model.createProperty(namespace + "assignedTo");
        StmtIterator assignedToIter = model.listStatements(resource, assignedToProperty, (RDFNode) null);
        while (assignedToIter.hasNext()) {
            Statement stmt = assignedToIter.next();
            Resource assignedToResource = stmt.getObject().asResource();
            String assignedToUsername = assignedToResource.getProperty(model.createProperty(namespace + "membershipUsername")).getString();
            if (assignedToUsername.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public void retrieveData(String objectName, String dataSourceId) throws IOException {
        TaigaDataSourceDto ds = (TaigaDataSourceDto) dataSourceRepository.findById(dataSourceId);

        if (USER_STORIES_OBJECT.equals(objectName)) {
            List<UserStory> userStories = retrieveUserStories(ds);
            saveUserStories(dataSourceId, userStories);
        } else if (TASKS_OBJECT.equals(objectName)) {
            List<Task> tasks = retrieveTasks(ds);
            saveTasks(dataSourceId, tasks);
        } else {
            throw new IllegalArgumentException("Unsupported object name: " + objectName);
        }
    }

    public List<Task> retrieveTasks(TaigaDataSourceDto ds) throws IOException {

        int project_id = get_project_id(ds);

        String url = baseUrl + "/tasks?project=" + project_id;
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
        int project_id = get_project_id(ds);

        String url = baseUrl + "/userstories?project=" + project_id;
        String responseJson = makeApiRequest(url, ds.getAccessToken());

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        List<UserStory> userStories = new ArrayList<>();

        for (JsonNode userStoryNode : jsonNode) {
            UserStory userStory = new UserStory();
            userStory.setId(String.valueOf(UUID.randomUUID()));
            userStory.setSubject(userStoryNode.get("subject").asText());
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

    private int get_project_id(TaigaDataSourceDto ds) throws IOException {
        String url = baseUrl + "/projects/by_slug?slug=" + ds.getProject();
        String responseJson = makeApiRequest(url, ds.getAccessToken());

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        int project_id = jsonNode.get("id").asInt();
        return project_id;
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
        return objectName.equals(USER_STORIES_OBJECT)
                || objectName.equals(TASKS_OBJECT);
    }

    public boolean supportsMethod(String method) {
        return method.equals(USERSTORY_COUNT)
                || method.equals(TASK_COUNT)
                || method.equals(USERSTORY_INDIVIDUAL_COUNT)
                || method.equals(TASK_INDIVIDUAL_COUNT);
    }
}


