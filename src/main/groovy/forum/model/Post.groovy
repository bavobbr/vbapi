package forum.model

import forum.utils.PostUtils

import java.time.LocalDateTime

public class Post {

    String username
    String message
    String forum
    Integer postId
    Integer postNumber
    LocalDateTime date
	Boolean valid = true
    Integer threadId

    public String toString() {
        return "[${username}] $message\n at $date [${postId}] [${postNumber}] [${valid}}"
    }

    public String getMessageText() {
        return PostUtils.removeClutter(message)
    }

}
