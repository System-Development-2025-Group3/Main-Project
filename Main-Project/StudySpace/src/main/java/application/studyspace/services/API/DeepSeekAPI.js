import { InferenceClient } from "@huggingface/inference";

/**
 * Represents an instance of the `InferenceClient` used to interact with an external API.
 *
 * The client is initialized with an API key retrieved from environment variables.
 * This key is used to authenticate with the API and access its services.
 *
 * @type {InferenceClient}
 */
const client = new InferenceClient(process.env.API_KEY);

/**
 * Sends a user-provided input to the DeepSeek API and retrieves a response from the AI model.
 *
 * @param {string} userContent - The content or message provided by the user to be processed by the AI model.
 * @return {Promise<string>} A promise that resolves to the AI's response as a string. If no response is received, returns "No response from AI". If an error occurs, logs the error and does not return a value.
 */
async function DeepSeekAPI(userContent) {
  try {
    const chatCompletion = await client.chatCompletion({
      provider: "fireworks-ai",
      model: "deepseek-ai/DeepSeek-V3-0324",
      messages: [
        {
          role: "user",
          content: userContent,
        },
      ],
    });


    const response = chatCompletion.choices[0]?.message?.content || "No response from AI";
    console.log(response); // This is sent to stdout
    return response;
  } catch (error) {

    const errorMsg = "Error querying the Hugging Face API: " + (error.message || error);
    console.error(errorMsg);
    console.log(errorMsg);
  }
}
DeepSeekAPI();
