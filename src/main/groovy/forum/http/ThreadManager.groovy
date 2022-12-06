package forum.http

import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import forum.model.*
import forum.utils.PostUtils

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class ThreadManager {

    private Browser browser

    public ThreadManager(String host, Credentials credentials) {
        browser = new Browser(host)
        if (credentials != null) {
            browser.login(credentials.username, credentials.password)
        }
    }

    public forum.model.Thread getThread(int threadId, int maxPages, int maxDays) {
        def threadNode = browser.getThread(threadId)
        def numpages = getThreadPageCount(threadNode)
        def title = getThreadTitle(threadNode)
        def posts = []
        println "Starting to scan thread [${title}]"
        posts = getAllPosts(threadId, threadNode, maxDays, maxPages)
        def now = LocalDateTime.now()
        if (maxDays > 0) {
            posts.retainAll {
                it.date.isAfter(now.minusDays(maxDays))
            }
        }
        posts = posts.sort { Post a, Post b -> a.postNumber <=> b.postNumber }
        return new Thread(threadId: threadId, title: title, posts: posts, pages: numpages)
    }


    public List<Integer> getThreadIds(int forumId, int pageId) {
        def subforumNode = browser.getSubForum(forumId, pageId)
        return parseThreadIds(subforumNode)
    }

    public ForumIndex getForums() {
        def forumNode = browser.getForum()
        return parseSubForums(forumNode)
    }

    public List<Post> getAllPosts(int threadId, def threadNode, int maxDays, int maxPages) {
        def numpages = getThreadPageCount(threadNode)
        def startpage = Math.max(numpages - maxPages + 1, 1);
        int pager = numpages
        boolean paging = true
        int lastPost = 0
        List<Post> threadposts = []
        def nmillis = new Date().getTime()
        long days = 0
        while (paging && days < maxDays && pager >= startpage) {
            println "paging page $pager"
            def thread = threadPage(threadId, pager)
            def posts = getPosts(thread)
            def currentLastPost = -1
            if (posts) {
                currentLastPost = posts.last().postId
            }
            if (!posts || currentLastPost == lastPost) {
                println "Stopped paging, posts size is ${posts.size()} and current lostpast id is ${currentLastPost?.id} against lastpost ${lastPost?.id}"
                paging = false
            }
            else {
                lastPost = currentLastPost
                threadposts.addAll(posts)
                pager--
                days = ChronoUnit.DAYS.between(posts.last().date, LocalDateTime.now())
            }
        }
        println "exited scan with paging $paging, days $days and pager $pager"
        return threadposts
    }


    public List<Post> getPostsAfter(int threadId, int postNumber) {
        def threadNode = browser.getThread(threadId)
        List<Post> allposts = getAllPosts(threadId, threadNode, 9999, 9999)
        allposts.each { println "${it.postId} ${it.postNumber} ${it.username} posted at ${it.date}" }
        int index = allposts.findIndexOf { it.postNumber == postNumber }
        if (index == allposts.size() - 1) return []
        else return allposts[index + 1..-1]
    }


    private GPathResult threadPage(int threadId, int pageid) {
        return browser.getThread(threadId, pageid)
    }

    private static String getThreadTitle(GPathResult threadNode) {
        def titlenode = threadNode.depthFirst().find { it.@class == "navbar" && it.name() == "TD"}
        return titlenode.text().trim()
    }

    private static Integer getThreadPageCount(GPathResult threadNode) {
        Integer count = 0;
        def pagecountNode = threadNode.depthFirst().find { it.@class == "vbmenu_control" && it.name() == "TD" && it.text() =~ "Page "}
        if (pagecountNode) {
            String text = pagecountNode.text()
            count = text.tokenize(" ").last().toInteger()
        }
        else {
            count = 1
        }
        return count
    }

    private static List<Integer> parseThreadIds(GPathResult forumNode) {
        def ids = []
        def titlenodes = forumNode.depthFirst().findAll { it.name() == "A" && it.@id.text() =~ "thread_title"}
        titlenodes.each {
            def id = it.@id.text()
            if (id) {
                String num = id - "thread_title_"
                ids << num.toInteger()
            }
        }
        return ids
    }

    private static ForumIndex parseSubForums(GPathResult forumNode) {
        def tableDiv = forumNode.depthFirst().find { it.name() == "DIV" && it.@class == "page"}
        def rows = tableDiv.depthFirst().findAll { it.name() == "TR" && it.@align.text() == "center"}
        def subforums = []
        rows.each {
            def titleNode = it.depthFirst().find { it.name() == "A" && it.@href.text().contains("forumdisplay.php?") }
            if (titleNode) {
                def alt1Nodes = it.depthFirst().findAll { it.name() == "TD" && it.@class == "alt1" }
                def alt2Nodes = it.depthFirst().findAll { it.name() == "TD" && it.@class == "alt2" }
                if (alt1Nodes && alt2Nodes) {
                    def threadsNode = alt1Nodes?.last()
                    def postsNode = alt2Nodes?.last()
                    def title = titleNode?.text()
                    if (title != "Donations") {
                        def threads = threadsNode?.text()?.replace(",", "") as Integer
                        def posts = postsNode?.text()?.replace(",", "") as Integer
                        SubForum subForum = new SubForum(name: title, threads: threads, posts: posts)
                        subforums << subForum
                    }
                }
            }

        }
        return new ForumIndex(subForums: subforums)
    }


    private List<Post> getPosts(GPathResult top) {
        def posts = getPostNodes(top)
        List postObjects = []
        posts.each {
            String username = getUsername(it)
            String message = getPostText(it)
            Integer postid = getPostId(it) as Integer
            Integer postnumber = getPostNumber(it) as Integer
            String datetext = getPostDate(it)
            LocalDateTime date = PostUtils.convertDate(datetext)
            Post post = new Post(username: username, message: message, postId: postid, postNumber: postnumber, date: date)
            if (username && message) {
                postObjects << post
            }
        }
        return postObjects
    }

    private static String getUsername(def postnode) {
        def usernode = postnode.depthFirst().find { it.@class == "bigusername"}
        return usernode?.text()
    }

    private String getPostText(def postnode) {
        def posttextnode = postnode.depthFirst().find { it.@id.text().startsWith("post_message_") && it.name() == "DIV"}
        def text = new StreamingMarkupBuilder().bind {
            out << posttextnode
        }
        return text
    }



    private static String getPostId(def postnode) {
        //<a href="showpost.php?p=789083&amp;postcount=1" target="new" rel="nofollow" id="postcount789083" name="1"><strong>1</strong></a>
        def postidnode = postnode.depthFirst().find {
            it.@id.text().startsWith("postcount")
        }
        return postidnode?.@name
    }

    private static String getPostNumber(def postnode) {
        //<a href="showpost.php?p=789083&amp;postcount=1" target="new" rel="nofollow" id="postcount789083" name="1"><strong>1</strong></a>
        def postidnode = postnode.depthFirst().find {
            it.@id.text().startsWith("postcount")
        }
        return postidnode?.@id?.text() - "postcount"
    }

    private static String getPostDate(def postnode) {
        def postdatenode = postnode.depthFirst().find {
            it.'@class'.text() == "thead"
        }
        return postdatenode?.text()
    }

    private static List getPostNodes(GPathResult topnode) {
        def postnode = topnode.depthFirst().find { it.@id == "posts" }
        def postnodes = postnode.depthFirst().findAll { it.@id.text().startsWith("post") && it.name() == "TABLE" }
        return postnodes
    }




}
