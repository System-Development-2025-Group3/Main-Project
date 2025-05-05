package application.studyspace.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class AIEmailCorrector {

    // Store API key securely (e.g., via an environment variable)
    private static final String API_KEY = DatabaseHelper.select("Key", "Config", "id", "1");

    /**
     * Uses AI to automatically correct an invalid or incorrectly formatted email address.
     *
     * @param email The invalid email address that needs correction.
     * @return The corrected email address, or the original email if correction fails.
     */
    public static String autoCorrectEmail(String email) {
        if (API_KEY == null) {
            throw new IllegalStateException("API key not found! Ensure the HF_API_KEY environment variable is set.");
        }

        try {
            // Create the HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // Prepare the API request body
            String requestBody = new JSONObject()
                    .put("inputs", "Correct the following email address so that it conforms to valid email standards: " + email)
                    .toString();

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-inference.huggingface.co/models/EleutherAI/gpt-j-6B"))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("AI API request failed. Status: " + response.statusCode() + ", Response: " + response.body());
                return email;
            }

            // Parse the response
            JSONObject jsonResponse = new JSONObject(response.body());
            // Fall back to the original email

            // Return the corrected email
            return jsonResponse.optJSONArray("outputs")
                    .optJSONObject(0)
                    .optString("generated_text", email);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occurred while attempting to correct the email: " + e.getMessage());
            return email; // Return the original email in case of failure
        }
    }

    public static void main(String[] args) {
        String invalidEmail = "user@gmial.com"; // Example of an invalid email
        String correctedEmail = autoCorrectEmail(invalidEmail);

        System.out.println("Original Email: " + invalidEmail);
        System.out.println("Corrected Email: " + correctedEmail);
    }
}