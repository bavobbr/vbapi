package forum.http

import groovy.util.slurpersupport.GPathResult
import forum.model.Credentials

class ReplyManager {

    private Browser browser

    public ReplyManager(String host, Credentials credentials) {
        browser = new Browser(host)
        if (credentials != null) {
            browser.login(credentials.username, credentials.password)
        }
    }

    public void post(int threadId, String post) {
        String token = getSecurityTokenForReply(threadId)
        browser.post(threadId, post, token)
    }

    public void message(String userId, String title, String message) {
        String token = getSecurityTokenForMessage()
        browser.message(userId, title, message, token)
    }

    private String getSecurityTokenForReply(int threadId) {
        GPathResult page = browser.getThread(threadId)
        def tokenNode = page.depthFirst().find {
            it.name() == "INPUT" && it.@name == "securitytoken"
        }
        return tokenNode?.@value
    }

    private String getSecurityTokenForMessage() {
        GPathResult page = browser.getMessagePage()
        def tokenNode = page.depthFirst().find {
            it.name() == "INPUT" && it.@name == "securitytoken"
        }
        return tokenNode?.@value
    }

}
