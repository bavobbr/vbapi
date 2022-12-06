package gtp3

import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest

OpenAiService service = new OpenAiService("x", 30)

def mytext = "als mijn hond en superheld was, wat zouden dan 5 goeie namen zijn?"
def mytext2 = "beschrijf me de film The Godfather"
CompletionRequest completionRequest = CompletionRequest.builder()
        .prompt(mytext2)
        .model("text-davinci-003")
        .echo(false)
        .maxTokens(250)
        .build()
service.createCompletion(completionRequest).getChoices().each {
    println it.text
}