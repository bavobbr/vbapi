package gtp3

import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest

OpenAiService service = new OpenAiService("x");
CompletionRequest completionRequest = CompletionRequest.builder()
        .prompt("Somebody once told me the world is gonna roll me")
        .model("ada")
        .echo(true)
        .build();
service.createCompletion(completionRequest).getChoices().each {
    println it
}