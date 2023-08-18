package ApiClient;

import java.io.IOException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonPlaceholderApiClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public static void main(String[] args) throws IOException {

        JsonPlaceholderApiClient apiClient = new JsonPlaceholderApiClient();

        try {
            String newUserJson = "{ \"name\": \"John Doe\", \"username\": \"johndoe\" }";
            apiClient.createUser(newUserJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Завдання 2: Оновлення користувача
        int userIdToUpdate = 1;
        String updatedUserJson = "{\"name\": \"Updated Name\", \"username\": \"updated_username\", \"email\": \"updated_email@example.com\"}";
        String updatedUserResponse = apiClient.updateUser(userIdToUpdate, updatedUserJson);
        System.out.println("Updated User: " + updatedUserResponse);

        // Завдання 3: Видалення користувача
        int userIdToDelete = 1;
        int deleteStatusCode = apiClient.deleteUser(userIdToDelete);
        System.out.println("Delete Status Code: " + deleteStatusCode);

        // Завдання 4: Отримання інформації про всіх користувачів
        String allUsersResponse = apiClient.getAllUsers();
        System.out.println("All Users: " + allUsersResponse);

        // Завдання 5: Отримання інформації про користувача за id
        int userIdToFetch = 2;
        String userByIdResponse = apiClient.getUserById(userIdToFetch);
        System.out.println("User by ID: " + userByIdResponse);

        // Завдання 6: Отримання інформації про користувача за username
        String usernameToFetch = "Bret";
        String userByUsernameResponse = apiClient.getUserByUsername(usernameToFetch);
        System.out.println("User by Username: " + userByUsernameResponse);

        // Нове завдання 7: Виведення коментарів до останнього поста користувача
        int userIdForComments = 1;
        apiClient.printLastPostComments(userIdForComments);

        // Нове завдання 8: Виведення відкритих задач для користувача
        int userIdForTodos = 1;
        apiClient.printOpenTodos(userIdForTodos);
    }

    private String sendHttpRequest(String method, String endpoint, String requestBody) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        if (requestBody != null) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            throw new IOException("HTTP request failed with response code: " + responseCode);
        }
    }

    public void createUser(String userJson) throws IOException {
        String response = sendHttpRequest("POST", "/users", userJson);

        // Перевірка, чи було успішно створено користувача
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        if (jsonResponse.has("id")) {
            System.out.println("User created successfully. User ID: " + jsonResponse.get("id").getAsString());
        } else {
            System.err.println("HTTP request failed with response: " + response);
        }
    }

    public String updateUser(int userId, String updatedUserJson) throws IOException {
        return sendHttpRequest("PUT", "/users/" + userId, updatedUserJson);
    }

    public int deleteUser(int userId) throws IOException {
        URL url = new URL(BASE_URL + "/users/" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");

        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }

    public String getAllUsers() throws IOException {
        return sendHttpRequest("GET", "/users", null);
    }

    public String getUserById(int userId) throws IOException {
        return sendHttpRequest("GET", "/users/" + userId, null);
    }

    public String getUserByUsername(String username) throws IOException {
        return sendHttpRequest("GET", "/users?username=" + username, null);
    }

    public void printLastPostComments(int userId) throws IOException {
        String userPostsResponse = sendHttpRequest("GET", "/users/" + userId + "/posts", null);
        JsonObject[] userPosts = new Gson().fromJson(userPostsResponse, JsonObject[].class);

        if (userPosts.length > 0) {
            JsonObject lastPost = userPosts[userPosts.length - 1];
            String postId = lastPost.get("id").getAsString();
            String postCommentsResponse = sendHttpRequest("GET", "/posts/" + postId + "/comments", null);

            String fileName = "user-" + userId + "-post-" + postId + "-comments.json";
            try (Writer writer = new FileWriter(fileName)) {
                writer.write(postCommentsResponse);
                System.out.println("Comments for last post saved to: " + fileName);
            }
        } else {
            System.out.println("User has no posts.");
        }
    }

    public void printOpenTodos(int userId) throws IOException {
        String userTodosResponse = sendHttpRequest("GET", "/users/" + userId + "/todos", null);
        JsonObject[] userTodos = new Gson().fromJson(userTodosResponse, JsonObject[].class);

        if (userTodos.length > 0) {
            String fileName = "user-" + userId + "-open-todos.json";
            try (Writer writer = new FileWriter(fileName)) {
                writer.write("[");
                boolean first = true;
                for (JsonObject todo : userTodos) {
                    if (!todo.get("completed").getAsBoolean()) {
                        if (!first) {
                            writer.write(",");
                        }
                        writer.write(todo.toString());
                        first = false;
                    }
                }
                writer.write("]");
                System.out.println("Open todos saved to: " + fileName);
            }
        } else {
            System.out.println("User has no todos.");
        }
    }
}
