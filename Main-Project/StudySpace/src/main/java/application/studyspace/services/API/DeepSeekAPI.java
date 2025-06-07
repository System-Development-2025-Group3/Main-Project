package application.studyspace.services.API;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

/**
 * The DeepSeekAPI class provides a method to interact with the DeepSeek AI model
 * by making HTTP requests to the specified API endpoint, sending user prompts,
 * and retrieving AI-generated responses.
 *
 * This class encapsulates the API communication logic, including payload construction,
 * HTTP request handling, and response parsing. It supports sending dynamic input
 * directly to the AI model and handles any errors that may occur during the process.
 */
public class DeepSeekAPI {

    private static final String API_URL = "https://api.fireworks.ai/inference/v1/chat/completions";
    private static final String API_KEY = "fw_3ZWJHuiuLsE4Sh1zNXhq751E";

    /**
     * Executes a DeepSeek API call with dynamic input and returns the AI's message response.
     *
     * @param dynamicInput The input question or prompt to send to the API.
     * @return The AI's response message as a string.
     */
    public static String executeDeepSeekAPI(String dynamicInput) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("model", "accounts/fireworks/models/deepseek-v3");
            payload.put("max_tokens", 16384);
            payload.put("top_p", 1);
            payload.put("top_k", 40);
            payload.put("presence_penalty", 0);
            payload.put("frequency_penalty", 0);
            payload.put("temperature", 0.6);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", dynamicInput);

            payload.put("messages", new JSONObject[] { message });

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY) // Add your API key
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            // Send the request and handle the response
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the response to extract the AI's message
            JSONObject jsonResponse = new JSONObject(response.body());
            String aiResponse = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return aiResponse;

        } catch (Exception e) {
            return "Error occurred during DeepSeek API call: " + e.getMessage();
        }
    }

    public static class ExampleUsage {
        public static void main(String[] args) {
            String input = "Hello, can you summarize the theory of relativity?";
            String response = DeepSeekAPI.executeDeepSeekAPI(input);
            System.out.println("DeepSeek Response: " + response);
        }
    }

}

