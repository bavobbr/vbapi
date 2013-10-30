package forum.model

class User {

    String username
    boolean good

    String toString() {
        return username
    }

    boolean equals(Object o) {
        if (o == null || !(o instanceof User)) return false
        else {
            return this.username.toUpperCase() == ((User) o).username?.toUpperCase()
        }
    }

    int hashCode() {
        return username?.hashCode()
    }

    public String getColoredName() {
        if (good) {
            return "[b][color=\"green\"]$username[/color][/b]"
        }
        else return "[b][color=\"red\"]$username[/color][/b]"
    }

}
