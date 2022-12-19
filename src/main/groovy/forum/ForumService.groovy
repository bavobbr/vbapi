package forum

import forum.http.*
import forum.model.*

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ForumService
{

	Integer dayLimit = 365
	Credentials credentials
	String host

	PostManager postManager

	public ForumService(Credentials credentials, String host)
	{
		this.credentials = credentials
		this.host = host
	}

	void post(int threadId, String message)
	{
		ReplyManager poster = new ReplyManager(host, credentials)
		poster.post(threadId, message)
	}

	void message(String userId, String title, String message)
	{
		ReplyManager poster = new ReplyManager(host, credentials)
		poster.message(userId, title, message)
	}

	void register(String username, String password, String email)
	{
		AccountManager accountManager = new AccountManager(host)
		accountManager.register(username, password, email)
	}


	Thread getThread(int threadId, int maxPages, int maxDays)
	{
		ThreadManager harvester = new ThreadManager(host, credentials)
		def thread = harvester.getThread(threadId, maxPages, maxDays)
		return thread
	}

	Post getPost(int postId)
	{
		def post = createPostManager().findPost(postId)
		return post
	}

	List<Post> getPosts(int startId, int fetchSize)
	{
		def post = createPostManager().findPosts(startId, fetchSize)
		return post
	}

	PostManager createPostManager() {
		if (!postManager) {
			PostManager harvester = new PostManager(host, credentials)
			return harvester
		}
		else return postManager
	}

	List<Integer> getThreadIds(int subforumId, int page)
	{
		ThreadManager harvester = new ThreadManager(host, credentials)
		def threadIds = harvester.getThreadIds(subforumId, page)
		return threadIds
	}

	ForumIndex getForumIndex()
	{
		ThreadManager manager = new ThreadManager(host, credentials)
		def forumIndex = manager.getForums()
		return forumIndex
	}

	List<Post> getPostsAfter(int threadId, int lastPostId)
	{
		ThreadManager harvester = new ThreadManager(host, credentials)
		return harvester.getPostsAfter(threadId, lastPostId)
	}

	List<Post> searchPosts(String searchString, String forumid, String user)
	{
		SearchManager searcher = new SearchManager(host, credentials)
		return searcher.findPosts(searchString, forumid, user, null)
	}

	List<Post> searchPosts(String searchString, String forumid, Integer daysAgo, boolean before)
	{
		SearchManager searcher = new SearchManager(host, credentials)
		return searcher.findPosts(searchString, forumid, null, daysAgo, before)
	}

	List<Post> searchPosts(String searchString, String forumid, String user, int maxResults) {
		return searchPosts(searchString, forumid, user, maxResults, 7, false)
	}


	List<Post> searchPosts(String searchString, String forumid, String user, int maxResults, Integer daysAgo, boolean before)
	{
		SearchManager searcher = new SearchManager(host, credentials)
		List<Post> posts = []
		posts = searcher.findPosts(searchString, forumid, user, daysAgo, before)
		int lastpostfound = posts.size()
		while (lastpostfound >= 200 && posts.size() < maxResults)
		{
			Post last = posts.last()
			long days = ChronoUnit.DAYS.between(last.date, LocalDateTime.now())
			println "Oldest day in batch is $days"
			sleep 5000
			if (days < dayLimit)
			{
				println "Adding new search to get to max of $maxResults"
				def newPosts = searcher.findPosts(searchString, forumid, user, days as String)
				posts.addAll(newPosts)
				lastpostfound = newPosts.size()
			}
			else
			{
				lastpostfound = 0
			}
		}
		return posts
	}

	List<Post> searchPosts(String searchString)
	{
		return searchPosts(searchString, null, null)
	}

	List<Member> collectMembers(Integer max)
	{
		MemberManager harvester = new MemberManager(host, credentials)
		return harvester.collectMembers(max)
	}

	Member getMemberByUsername(String username)
	{
		MemberManager harvester = new MemberManager(host, credentials)
		return harvester.getMember(username)
	}

	Member getMemberById(Integer id)
	{
		MemberManager harvester = new MemberManager(host, credentials)
		return harvester.getMember(id)
	}

	void changeSignature(String signature)
	{
		SettingsManager manager = new SettingsManager(host, credentials)
		manager.changeSignature(signature)
	}


}
