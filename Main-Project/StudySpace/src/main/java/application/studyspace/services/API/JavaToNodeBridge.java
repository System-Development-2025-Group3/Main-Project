package application.studyspace.services.API;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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