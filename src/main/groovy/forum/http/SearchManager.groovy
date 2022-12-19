package forum.http

import forum.model.Credentials
import forum.model.Post
import forum.utils.PostUtils
import groovy.util.slurpersupport.GPathResult

import java.time.LocalDateTime

class SearchManager {

    private Browser browser

    public SearchManager(String host, Credentials credentials) {
        browser = new Browser(host)
        if (credentials != null) {
            browser.login(credentials.username, credentials.password)
        }
    }

    public List<Post> findPosts(String terms, String forumid, String user, Integer daysAgo) {
        return findPosts(terms, forumid, user, daysAgo, true)
    }

    public List<Post> findPosts(String terms, String forumid, String user, Integer daysAgo, boolean before) {
        int pager = 1
        boolean paging = true
        int lastPost = 0
        List<Post> threadposts = []
        def searchId = -1
        if (!forumid && !user && !daysAgo) {
            searchId = getSearchId(terms)
        } else {
            searchId = getSearchId(terms, forumid, user, daysAgo, before)
        }
        while (paging && searchId > 0) {

            def thread = searchPage(searchId, pager)
            def posts = getPosts(thread)
            def currentLastPost = -1
            if (posts) {
                currentLastPost = posts.last().postId
            }
            if (!posts || currentLastPost == lastPost) {
                paging = false
            } else {
                lastPost = currentLastPost
                threadposts.addAll(posts)
                pager++
            }
        }
        return threadposts
    }

    private int getSearchId(String query) {
        GPathResult searchIdPage = browser.getSearchIdPage(query)
        def linknode = searchIdPage.depthFirst().find { it.@href.text().startsWith("/search.php?searchid=") }
        String matchedId = "-1"
        if (linknode) {
            String url = linknode.@href.text()
            url.find(~/search\.php\?searchid=(\d*)/) { match, id -> matchedId = id }
        } else {
            return -1
        }
        return matchedId as int
    }

    private int getSearchId(String query, String forumid, String user, Integer daysAgo, boolean before) {
        GPathResult searchIdPage = browser.getSearchIdPage(query, forumid, user, daysAgo, before)
        def linknode = searchIdPage.depthFirst().find { it.@href.text().startsWith("/search.php?searchid=") }
        if (linknode == null) return -1
        String url = linknode.@href.text()
        String matchedId = "-1"
        url.find(~/search\.php\?searchid=(\d*)/) { match, id -> matchedId = id }
        return matchedId as int
    }


    private GPathResult searchPage(int searchId, int pageid) {
        return browser.searchPosts(searchId, pageid);
    }


    private List<Post> getPosts(GPathResult top) {
        def posts = getPostNodes(top)
        List postObjects = []
        posts.each {
            try {
                String username = getUsername(it)
                String message = getPostText(it)
                Integer postid = getPostId(it) as Integer
                Integer threadId = getThreadId(it) as Integer
                String datetext = getPostDate(it)
                String forumId = getForum(it)
                LocalDateTime date = PostUtils.convertDate(datetext)
                Post post = new Post(username: username, message: message, postId: postid, date: date, forum: forumId, threadId: threadId)
                postObjects << post
            }
            catch (e) {
                println "couldnt parse a post"
            }
        }
        return postObjects
    }

    private String getUsername(def postnode) {
        def usernode = postnode.depthFirst().find { it.@href.text().startsWith("member.php?") }
        return usernode.text()
    }

    private Integer getThreadId(def postnode) {
        //https://forum.shrimprefuge.be/showthread.php?t=23062
        def posttextnode = postnode.depthFirst().find { it.@href.text().startsWith("showthread.php?t=") }
        def url = posttextnode.@href.text()
        def tid = url =~ /showthread\.php\?t=(\d*)/
        return tid[0][1].toInteger()
    }


    private String getPostText(def postnode) {
        def posttextnode = postnode.depthFirst().find { it.@href.text().startsWith("showthread.php?p=") }
        return posttextnode.text()
    }

    private String getForum(def postnode) {
        ////*[@id="post1054812"]/tbody/tr[1]/td/span/a
        //<a href="forumdisplay.php?f=20">Shrimp Refuge HQ</a>
        def forumtextnode = postnode.depthFirst().find { it.@href.text().startsWith("forumdisplay.php?f=") }
        return forumtextnode.text()
    }


    private String getPostId(def postnode) {
        //<a href="showpost.php?p=789083&amp;postcount=1" target="new" rel="nofollow" id="postcount789083" name="1"><strong>1</strong></a>
        //<a href="showthread.php?p=855149&amp;highlight=tester#post855149">Ik veronderstel dat je het ook over de stresstest...</a>
        def postidnode = postnode.depthFirst().find {
            it.@href.text().startsWith("showthread.php?p=")
        }
        def postid = postidnode.@href.text().find(~/[~#].*/)
        return postid - "#post"
    }


    private String getPostDate(def postnode) {
        GPathResult postdatenode = postnode.depthFirst().find {
            it.'@class'.text() == "thead"
        }
        def fullText = postdatenode.text()
        postdatenode.children().list().each { fullText = fullText - it.text() }
        return fullText.trim()
    }

    private List getPostNodes(GPathResult topnode) {
        def postnode = topnode.depthFirst().find { it.@id.text() == "inlinemodform" }
        if (!postnode) return []
        def postnodes = postnode.depthFirst().findAll { it.@id.text().startsWith("post") }
        return postnodes
    }


}
