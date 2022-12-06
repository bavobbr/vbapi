package gtp3

import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionChoice
import com.theokanning.openai.completion.CompletionRequest
import forum.ForumService
import forum.model.Credentials

Credentials credentials = new Credentials(username: "x", password: "y")
ForumService service = new ForumService(credentials, "https://forum.shrimprefuge.be/")
OpenAiService openai = new OpenAiService("x", 60)

def threadId = 23062
def preamble = "Je bent een intelligente bot. Je antwoordt op volgend bericht: "

def thread = service.getThread(threadId, 4, 7)
def lastRepliedAll = thread.posts.findAll { it.username == credentials.username }
def lastRepliedPost = -1
if (lastRepliedAll) {
    lastRepliedPost = lastRepliedAll.last().postNumber
}
println "Beginning with last post: $lastRepliedPost"

def otherPosts = thread.posts.findAll { it.username != credentials.username }
def newPosts = otherPosts.findAll { it.postNumber > lastRepliedPost }


while (true) {
    println "sleeping before threadscan"
    sleep(60000)
    try {
        thread = service.getThread(threadId, 2, 7)
        println "Seen ${thread.pages} pages and ${thread.posts.size()} posts"
        otherPosts = thread.posts.findAll { it.username != credentials.username && !it.messageText.trim().startsWith("@")}
        newPosts = otherPosts.findAll { it.postNumber > lastRepliedPost }
        println "   of which ${otherPosts.size()} other posts and ${newPosts.size()} new posts"

        newPosts.each {
            try {
                println "\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                println it.date
                println it.username
                println it.messageText
                println "\n <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
                if (it.messageText.trim().size() > 10) {
                    def reply = doRequest(openai, preamble, it.messageText)
                    println "Certainty is $reply"
                    sleep(10000)
                    if (reply) {
                        def out = reply[0].text.trim()
                        if (out.size() > 0) {
                            def cleaned = clean(out)
                            def quoted = asQuote(it.messageText, it.username, it.postNumber, cleaned)
                            println quoted
                            service.post(threadId, quoted)
                        }
                    }
                }
                lastRepliedPost = it.postNumber
                println "Setting as last post: $lastRepliedPost"
            }
            catch (Exception e) {
                e.printStackTrace()
                println "Issue handling post $it due to $e"
            }
        }
    }
    catch (Exception e) {
        e.printStackTrace()
        println "Issue reading threads due to $e"
    }

}

List<CompletionChoice> doRequest(OpenAiService openai, String preamble, String text) {
    def fullText = preamble + text
    CompletionRequest completionRequest = CompletionRequest.builder()
            .prompt(fullText)
            .model("text-davinci-003")
            .echo(false)
            .maxTokens(300)
            .build()
    return openai.createCompletion(completionRequest).getChoices()
}

String asQuote(String source, String user, Integer postId, String reply) {
    return """
[QUOTE=$user;$postId]
$source
[/QUOTE]
$reply
"""
}

String clean(String text) {
    def trimmed = text.trim()
    return text.dropWhile { it == '!' || it == "." }
}





