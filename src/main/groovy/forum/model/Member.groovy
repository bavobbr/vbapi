package forum.model

import java.time.LocalDateTime

class Member {

    String username
    Integer memberId
    LocalDateTime lastActive
    Integer totalPosts
    LocalDateTime joined
    URL avatar


    public String toString() {
        return "name [$username] id [$memberId] active [$lastActive] [$totalPosts] [$joined] [$avatar]"
    }

}
