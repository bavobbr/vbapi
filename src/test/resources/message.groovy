import forum.model.Credentials
import forum.ForumService

Credentials credentials = new Credentials(username: "x", password: "y")
ForumService postService = new ForumService(credentials, "http://forum.shrimprefuge.be")
postService.message("user", "hello you", "my body is ready")
