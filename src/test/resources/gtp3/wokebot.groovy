package gtp3

import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionChoice
import com.theokanning.openai.completion.CompletionRequest
import forum.ForumService
import forum.model.Credentials
import forum.model.Post
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

def credentials = new Credentials(username: "xxx", password: "xxx")
def forumService = new ForumService(credentials, "https://forum.xxx.be/")
def openai = new OpenAiService("xxx", 60)
String callsign = "@wokebot"
def repliedTo = []

Integer days = ((Math.random() + 1) * 300) as Integer
def myposts = forumService.searchPosts("", null, credentials.username, 50, days, false)
def lastPost = myposts ? myposts.first() : null
def lastPostId = lastPost ? lastPost.postId : -1
println "lastpost id: $lastPostId from $lastPost"

while (true) {
    sleep 60000
    try {
        days = ((Math.random() + 1) * 300) as Integer
        def requests = forumService.searchPosts(callsign, null, days, false)
        println "parsing ${requests.size()} posts that contain '${callsign}': ${requests.postId}"
        def oldestPostsFirst = requests.sort { it.date }
        def newPosts = oldestPostsFirst.dropWhile { it.postId <= lastPostId }
        println "   kept ${newPosts.size()} that were newer than $lastPostId"
        def otherPosts = newPosts.findAll { it.username != credentials.username && it.username != "Singulariteit" }
        println "   of which ${otherPosts.size()} were form other users"
        otherPosts.each {
            println "Search result: $it.username $it.date $it.postId"
            def fullpost = forumService.getPost(it.postId)
            if (fullpost.messageText.toLowerCase().contains(callsign)) {
                if (!repliedTo.contains(fullpost.postId)) {
                    println "\n++++++++++++++++++++++++++++++++"
                    println "Post: $fullpost.username $fullpost.date $fullpost.postId"
                    println fullpost.messageText
                    println "++++++++++++++++++++++++++++++++"
                    try {
                        println "replying to $fullpost.messageText"
                        def fullMessage = fullpost.messageText
                        def startIdx = fullMessage.toLowerCase().indexOf(callsign) ?: 0
                        startIdx = startIdx + callsign.length()
                        def aiRequest = fullMessage[startIdx..-1].trim()

                        if (aiRequest.startsWith("render")) {
                            doRender(aiRequest, it, fullpost, forumService)
                        } else {
                            doCompletion(aiRequest, openai, it, fullpost, forumService)
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace()
                        println "reply issue $e"
                    }
                    repliedTo << fullpost.postId
                    lastPostId = fullpost.postId
                    println("Setting as last Post ID $lastPostId")
                } else {
                    println "Ignoring post $fullpost.postId"
                }
            } else {
                println "Ignoring post without callid $fullpost.postId"
            }
        }
    }
    catch (Exception e) {
        e.printStackTrace()
        println "scan issue $e"
    }
}

void doCompletion(String aiRequest, OpenAiService openai, Post searchPost, Post fullpost, ForumService forumService) {
    def reply = doRequest(openai, aiRequest.trim())
    println "completion is $reply"
    sleep(10000)
    if (reply) {
        def out = reply[0].text.trim()
        if (out.size() > 0) {
            def cleaned = clean(out)
            def quoted = asQuote(fullpost.messageText, fullpost.username, fullpost.postId, cleaned)
            println quoted
            forumService.post(searchPost.threadId, quoted)
        }
    }
}

void doRender(String aiRequest, Post searchPost, Post fullpost, ForumService forumService) {
    def imageRequest = aiRequest.trim() - "render"
    def reply = doDalleRequest(imageRequest.trim())
    println "image reply is $reply"
    sleep(10000)
    if (reply) {
        def out = reply.trim()
        if (out.size() > 0) {
            def quoted = asImage(fullpost.messageText, fullpost.username, fullpost.postId, out)
            println quoted
            forumService.post(searchPost.threadId, quoted)
        }
    }
}

String doDalleRequest(String prompt) {
    def host = "https://4bv812a6vi.execute-api.us-east-1.amazonaws.com"
    def client = new RESTClient(host)
    def response = client.post(path: "/dev/generate",
            requestContentType: ContentType.URLENC,
            body: [prompt: prompt],
            headers: [Accept: 'text/plain'])
    println response.data
    return response.data
}


List<CompletionChoice> doRequest(OpenAiService openai, String text) {
    println "Completing: $text"
    CompletionRequest completionRequest = CompletionRequest.builder()
            .prompt(text)
            .model("text-davinci-003")
            .echo(false)
            .maxTokens(500)
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

String asImage(String source, String user, Integer postId, String reply) {
    return """
[QUOTE=$user;$postId]
$source
[/QUOTE]
[img]$reply[/img]
"""
}

String clean(String text) {
    return text.trim().dropWhile { it == '!' || it == "." || it == "?" }
}