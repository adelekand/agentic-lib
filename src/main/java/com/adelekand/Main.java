package com.adelekand;

import com.adelekand.agents.Agent;
import com.adelekand.agents.AgentEvent;
import com.adelekand.messages.RunInput;
import com.adelekand.messages.RunOutput;
import com.adelekand.examples.WeatherTools;
import com.adelekand.models.AzureOpenAI;
import com.adelekand.models.Model;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        String deployment = System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME");
        String apiKey = System.getenv("AZURE_OPENAI_API_KEY");
        String apiVersion = System.getenv("AZURE_OPENAI_API_VERSION");
        Model model = new AzureOpenAI(endpoint, deployment, apiKey, apiVersion);

        Agent agent = new Agent(model)
                .instructions("You should never tell a joke")
                .tools(new WeatherTools());

        RunOutput out = agent.runStream(RunInput.of("What's the weather in Fredericton?"), e -> {
            switch (e) {
                case AgentEvent.ToolCallStarted s   -> System.out.println("→ " + s.name() + " " + s.arguments());
                case AgentEvent.ToolCallCompleted c -> System.out.println("← " + c.result());
                default -> {}
            }
        });
        System.out.println("Response: " + out.content());

//        var response = agent.run(new RunInput("What's the weather in Fredericton?", null, null));
//
//        System.out.printf("Model ID: %s%n", model.id());
//        System.out.println("Response: " + response.content());
//        System.out.println("Input tokens: " + response.usage().inputTokens() + ", Output tokens: " + response.usage().outputTokens());
    }
}