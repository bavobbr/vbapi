package forum.http

import forum.utils.SecurityUtils
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams


import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import java.nio.charset.Charset
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import java.security.SecureRandom
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.Scheme

class Browser
{

  private String hostUrl

  private String threadUrl = "showthread.php"
  private String forumUrl = "forumdisplay.php"
  private String loginUrl = "login.php"
  private String postUrl = "newreply.php"
  private String messageUrl = "private.php"
  private String searchUrl = "search.php"
  private String registerUrl = "register.php"
  private String memberUrl = "member.php"
  private String memberListUrl = "memberlist.php"
  private String profileUrl = "profile.php"
  private String showpostUrl = "showpost.php"

  private HTTPBuilder http

  public Browser(String host)
  {
    this.hostUrl = host
    http = new HTTPBuilder(hostUrl)

    SSLContext sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, [ new X509TrustManager() {
      public X509Certificate[] getAcceptedIssuers() {null }
      public void checkClientTrusted(X509Certificate[] certs, String authType) { }
      public void checkServerTrusted(X509Certificate[] certs, String authType) { }
    } ] as TrustManager[], new SecureRandom())
    def sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
    def httpsScheme = new Scheme("https", sf, 443)
    http.client.connectionManager.schemeRegistry.register( httpsScheme )


    HttpParams params = http.client.getParams();
    HttpConnectionParams.setConnectionTimeout(params, 20000);
    HttpConnectionParams.setSoTimeout(params, 20000)
    http.encoder.charset = "ISO-8859-1"

  }

  public void login(String user, String password)
  {
    def hashedPassword = SecurityUtils.toMD5(password)
    def postBody = [
            securitytoken: "1606766422-d301811be2621bcdc6ff190e38a1ef5bd290c9bf",
            do: 'login',
            vb_login_username: user,
            vb_login_md5password_utf: hashedPassword,
            cookieuser: "1"]
    http.post(path: loginUrl, body: postBody, requestContentType: ContentType.URLENC) { resp, reader ->
	  def pbody = reader.text()
      if (resp.statusLine.statusCode != 200)
      {
        throw new IllegalStateException("Browser failed to log in: " + resp.statusLine)
      }
    }
  }

  public String getThreadAsText(int threadId)
  {
    String output = ""
    http.get(path: threadUrl, query: [t: "$threadId"], contentType: ContentType.TEXT) { resp, reader ->
      output = reader.text
    }
    return output
  }

  public GPathResult getMessagePage()
  {
    GPathResult html = http.get(path: messageUrl, query: [do: "newpm"])
    return html
  }

  public GPathResult getSignaturePage()
  {
    GPathResult html = http.get(path: profileUrl, query: [do: "editsignature"])
    return html
  }

  public GPathResult getRegisterPage()
  {
    GPathResult html = http.get(path: registerUrl, query: [do: "register", "agree": "1"])
    return html
  }

  public GPathResult getMemberPage(Integer memberId)
  {
    GPathResult html = http.get(path: memberUrl, query: ["u": "$memberId"])
    return html
  }


  public GPathResult getThread(int threadId)
  {
    GPathResult html = http.get(path: threadUrl, query: [t: "$threadId"])
    return html
  }

  public GPathResult getForum()
  {
    GPathResult html = http.get(path: "index.php")
    return html
  }

  public GPathResult getSubForum(int forumId)
  {
    GPathResult html = http.get(path: forumUrl, query: [f: "$forumId"])
    return html
  }

  public GPathResult getSubForum(int forumId, int pageid)
  {
    GPathResult html = http.get(path: forumUrl, query: [f: "$forumId", page: "$pageid"])
    return html
  }

  public GPathResult getThread(int threadId, int pageid)
  {
    GPathResult html = http.get(path: threadUrl, query: [t: "$threadId", page: "$pageid"])
    return html
  }

  public GPathResult getSearchIdPage(String terms)
  {
    getSearchIdPage(terms, null, null, null, true);
  }

  public GPathResult getSearchIdPage(String terms, String forumid, String user, Integer daysAgo, boolean before)
  {
    Map query = [:]
    query.do = "process"
    query.showposts = "1"
    if (terms) query.query = terms
    else query.query = ""
    if (forumid) query."forumchoice[]" = forumid
    if (user) {
      query.searchuser = user
      query.exactname = "1"
    }
    if (daysAgo)
    {
      query.searchdate = daysAgo
      query.beforeafter = before? "before" : "after"
    }
    else {
      query.searchdate = 0
      query.beforeafter = "after"
    }
    query.sortby = "lastpost"
    query.sortorder = "descending"
    GPathResult html = http.get(path: searchUrl, query: query)
    return html
  }

  public GPathResult searchMembers(String user)
  {
    Map query = [:]
    query.do = "getall"
    query.ausername = user
    GPathResult html = http.get(path: memberListUrl, query: query)
    return html
  }

  public GPathResult collectMembersByPosts(int perPage, int page)
  {
    Map query = [:]
    query.pp = "$perPage"
    query.order = "desc"
    query.sort = "posts"
    query.page = "$page"
    GPathResult html = http.get(path: memberListUrl, query: query)
    return html
  }

  public GPathResult searchPosts(int searchId, int pageid)
  {
    GPathResult html = http.get(path: searchUrl, query: [searchid: "$searchId", page: "$pageid"])
    return html
  }

  public void post(int threadId, String post, String token)
  {
    println "token used is $token"
    def postBody = [
            t: threadId,
            message: post,
            securitytoken: token,
            do: "postreply",
            cookieuser: "1",
            loggedinuser: "2811",
            qr_threadid: '16932',
            qr_postid: "who cares"

    ]
    println "posting in $threadId $post"
    http.post(path: postUrl, body: postBody, requestContentType: ContentType.URLENC) { HttpResponseDecorator resp ->
      println "POST response status: ${resp.statusLine}"
    }
  }

  public void applySignature(String post, String token)
  {
    println "token used is $token"
    def postBody = [
            message: post,
            url: "profile.php?do=editoptions",
            securitytoken: token,
            do: "updatesignature",
            MAX_FILE_SIZE: "41943040",
    ]
    http.post(path: profileUrl, body: postBody, requestContentType: ContentType.URLENC) { HttpResponseDecorator resp ->
      println "POST response status: ${resp.statusLine}"
    }
  }

  public void message(String userId, String title, String post, String token)
  {
    println "token used is $token"
    def postBody = [
            recipients: userId,
            message: post,
            securitytoken: token,
            do: "insertpm",
            title: title,
            iconid: "0",
            savecopy: "1"
    ]
    println "messaging to $userId $title"
    http.post(path: messageUrl, body: postBody, requestContentType: ContentType.URLENC) { HttpResponseDecorator resp ->
      println "POST response status: ${resp.statusLine}"
    }
  }

  public GPathResult acceptRules()
  {
    def postBody = [
            securitytoken: "guest",
            do: "register",
            agree: "1",
            who: "adult",
            url: hostUrl
    ]
    println "accepting rules"
    return http.post(path: registerUrl, body: postBody, requestContentType: ContentType.URLENC)
  }

  public void register(String username, String password, String email, String token)
  {
    def passmd5 = SecurityUtils.toMD5(password)
    println "using token $token"
    def postBody = [
            securitytoken: "guest",
            do: "addmember",
            agree: "1",
            who: "adult",
            url: "index.php",
            password_md5: passmd5,
            passwordconfirm_md5: passmd5,
            "username": username,
            "email": email,
            "emailconfirm": email,
            "options[adminemail]": "1",
            "humanverify[input]": "zes",
            "humanverify[hash]": token

    ]
    http.post(path: registerUrl, body: postBody, requestContentType: ContentType.URLENC) { HttpResponseDecorator resp ->
      println "POST response status: ${resp.statusLine}"
    }
  }

	public GPathResult getPost(Integer id)
	{
		//println "getting post at ${showpostUrl}?p=$id"
		GPathResult html = http.get(path: showpostUrl, query: [p: "$id"])
		return html
	}
}
