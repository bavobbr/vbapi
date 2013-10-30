package forum.model

class Vote {

    User user
    User target
    Date date
    Integer postNumber

    public String toString() {
        return "[${user?.username}] ${target?.username} [$date] [${postNumber}]"
    }

    public boolean isValid() {
        return user && target
    }
}
