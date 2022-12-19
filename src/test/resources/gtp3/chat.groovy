package gtp3

import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest

def service = new OpenAiService("xxx", 30)
def mytext = "beschrijf me de film The Godfather"
CompletionRequest completionRequest = CompletionRequest.builder()
        .prompt(mytext)
        .model("text-davinci-003")
        .echo(false)
        .maxTokens(250)
        .build()
service.createCompletion(completionRequest).getChoices().each {
    println it.text
}