package forum.http

class AccountManager {

    private Browser browser

    public AccountManager(String host) {
        browser = new Browser(host)
    }

    public void register(String username, String password, String email) {
        def detailPage = browser.acceptRules()
        String token = getSecurityTokenForRegister(detailPage)
        browser.register(username, password, email, token)
    }


    private String getSecurityTokenForRegister(def page) {
        def tokenNode = page.depthFirst().find {
            it.name() == "INPUT" && it.@name == "humanverify[hash]"
        }
        return tokenNode?.@value
    }

}
