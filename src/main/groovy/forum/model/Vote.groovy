package forum.model

import java.time.LocalDateTime

class Vote {

    User user
    User target
    LocalDateTime date
    Integer postNumber

    public String toString() {
        return "[${user?.username}] ${target?.username} [$date] [${postNumber}]"
    }

    public boolean isValid() {
        return user && target
    }
}
