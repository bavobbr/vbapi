package forum.http

import forum.model.Credentials
import groovy.util.slurpersupport.GPathResult

class SettingsManager
{

    private Browser browser

    public SettingsManager(String host, Credentials credentials) {
        browser = new Browser(host)
        if (credentials != null) {
            browser.login(credentials.username, credentials.password)
        }
    }

    public void changeSignature(String signature) {
        String token = getSecurityTokenForSignature()
        browser.applySignature(signature, token)
    }

    private String getSecurityTokenForSignature() {
        GPathResult page = browser.getSignaturePage()
        def tokenNode = page.depthFirst().find {
            it.name() == "INPUT" && it.@name == "securitytoken"
        }
        return tokenNode?.@value
    }

}
