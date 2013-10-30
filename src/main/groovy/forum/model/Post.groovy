package forum.model

import forum.utils.PostUtils

public class Post {

    String username
    String message
    String forum
    Integer postId
    Integer postNumber
    Date date
	Boolean valid = true

    public String toString() {
        return "[${username}] $message\n at $date [${postId}] [${postNumber}] [${valid}}"
    }

    public String getMessageText() {
        return PostUtils.removeClutter(message)
    }

}
