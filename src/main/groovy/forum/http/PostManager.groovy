package forum.http

import forum.model.Credentials
import forum.model.Post
import forum.utils.PostUtils
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder

import java.time.LocalDateTime

class PostManager
{

	private Browser browser

	public PostManager(String host, Credentials credentials)
	{
		browser = new Browser(host)
		if (credentials != null)
		{
			browser.login(credentials.username, credentials.password)
		}
	}

	public Post findPost(Integer id)
	{
		Post post
		try
		{
			def page = browser.getPost(id)
			post = parsePost(page, id)
			post.setPostId(id)
		}
		catch (e)
		{
			println "Something went very wrong with ${id}"
		}
		return post
	}

	public List<Post> findPosts(Integer id, Integer fetchSize)
	{
		List<Post> posts = []
		fetchSize.times {
			Post post = findPost(id++)
			if (post) posts << post
		}
		return posts
	}

	private Post parsePost(GPathResult top, Long id)
	{
		Post post
		def postnode = getPostNode(top)
		if (postnode) {
			try {
				String username = getUsername(postnode)
				String message = getPostText(postnode)
				String datetext = getPostDate(postnode)
				LocalDateTime date = PostUtils.convertDate(datetext)
				post = new Post(username: username, message: message, date: date)
			}
			catch (e) {
				println "${e.message} error for $id, postnode is ${postnode}"
				//println "page is ${top}"
				post = new Post(valid: false)
			}
		}
		else post = new Post(username: null, message: null, date: null)
		return post
	}

	private static String getUsername(def postnode)
	{
		def usernode = postnode.depthFirst().find { it.@class == "bigusername"}
		return usernode?.text()
	}

	private String getPostText(def postnode)
	{
		def posttextnode = postnode.depthFirst().find { it.@id.text().startsWith("post_message_") && it.name() == "DIV"}
		def text = new StreamingMarkupBuilder().bind {
			out << posttextnode
		}
		return text
	}

/*	private static String getPostId(def postnode)
	{
		def postidnode = postnode.depthFirst().find {
			it.@id.text().startsWith("postcount")
		}
		return postidnode?.@name
	}

	private static String getPostNumber(def postnode)
	{
		def postidnode = postnode.depthFirst().find {
			it.@id.text().startsWith("postcount")
		}
		return postidnode?.@id?.text() - "postcount"
	}*/

	private static String getPostDate(def postnode)
	{
		def postdatenode = postnode.depthFirst().find {
			it.'@class'.text() == "thead"
		}
		return postdatenode?.text()
	}

	private static def getPostNode(GPathResult topnode)
	{
		//def postnodeparent = topnode.depthFirst().find { it.@id == "posts" }
		def postnode = topnode.depthFirst().find { it.@id.text().startsWith("post") && it.name() == "TABLE" }
		return postnode
	}

}
