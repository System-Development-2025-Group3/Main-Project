import { InferenceClient } from "@huggingface/inference";

const client = new InferenceClient("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");

/**
 * Executes a chat completion request to interact with the AI language model.
 *
 * @param {string} userContent - The dynamic input content for the AI model.
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
