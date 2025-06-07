package application.studyspace.services.API;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * JavaToNodeBridge class provides a method to execute a Node.js script to interact
 * with the DeepSeekAPI.js file. This demonstrates the integration between Java and
 * a Node.js script using the external system process.
 *
 * The primary use case is to take dynamic input, pass it to the Node.js script
 * DeepSeekAPI.js, and retrieve the result or output from the executed script.
 *
 * The class facilitates bridging between Java and Node.js, where the Node.js script
 * handles complex API interactions like communicating with the DeepSeek API externally.
 *
 * Error handling is included to manage scenarios such as script execution failure or
 * incorrect inputs.
 */
public class JavaToNodeBridge {

    public static String executeDeepSeekAPI(String dynamicInput) {
        StringBuilder result = new StringBuilder();
        System.out.println("Executing DeepSeekAPI with input: " + dynamicInput);

        try {
            String scriptPath = "Main-Project/StudySpace/src/main/java/application/studyspace/services/DeepSeekAPI.js";
            ProcessBuilder processBuilder = new ProcessBuilder("node", scriptPath, dynamicInput);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("AI Response:");
            
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            System.out.println("JavaScript process exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            result.append("Error occurred: ").append(e.getMessage()).append("\n");
        }

        return result.toString();
    }
}