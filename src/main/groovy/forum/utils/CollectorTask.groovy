package forum.utils

import forum.model.Post
import forum.ForumService

class CollectorTask {

    private Integer threadId
    private List<Post> posts
	private String host

    public CollectorTask(Integer threadId, String host) {
        this.threadId = threadId
	    this.host = host
    }

    void start() {
        ForumService forum = new ForumService(null, host)
        Timer timer = new Timer()
        Integer lastpostnumber = Integer.MAX_VALUE
        timer.scheduleAtFixedRate(new TimerTask() {
            void run() {
                List<Post> newposts = forum.getPostsAfter(threadId, lastpostnumber)
                lastpostnumber = newposts.last().postNumber
                posts.addAll(newposts)
            }
        }, 0, 60 * 1000)
    }
}
