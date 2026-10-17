package com.adelekand;

import com.adelekand.dto.Message;
import com.adelekand.models.AzureOpenAI;
import com.adelekand.models.Model;

import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        String deployment = System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME");
        String apiKey = System.getenv("AZURE_OPENAI_API_KEY");
        String apiVersion = System.getenv("AZURE_OPENAI_API_VERSION");
        Model model = new AzureOpenAI(endpoint, deployment, apiKey, apiVersion);

       var response = model.chat(
                List.of(
                        new Message.SystemMessage("You should never tell a joke"),
                        new Message.UserMessage("Hello, can you tell me a joke?")
                ),
                List.of()
        );

        System.out.printf("Model ID: %s%n", model.id());
        System.out.println("Response: " + response.message().content());
        System.out.println("Input tokens: " + response.usage().inputTokens() + ", Output tokens: " + response.usage().outputTokens());
    }
}