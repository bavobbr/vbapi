package forum.utils

import groovy.util.slurpersupport.GPathResult
import forum.model.Post
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PostUtils {


    static public GPathResult toNode(String text) {
        XmlSlurper slurper = new XmlSlurper()
        return slurper.parseText(text)
    }


    static public Set<String> getQuotedTargets(Post post) {
        def targets = [] as Set
        if (post.message =~ "Quote:") {
            def node = toNode(post.message)
            def quotenodes = node.depthFirst().findAll {
                it.name() == "DIV" && it.text().trim().startsWith("Quote:") && it.@class == "smallfont"
            }
            if (quotenodes) {
                quotenodes.each {
                    def parent = it.parent()
                    def targetNode = parent.depthFirst().find { it.name() == "STRONG" }
                    if (targetNode) {
                        targets << targetNode.text()
                    }
                }
            }
        }
        return targets
    }

    static public String removeClutter(String post) {
        GPathResult node = toNode(post)
        def cleaned = node.text()
        def quotenodes = node.depthFirst().findAll {
            it.name() == "DIV" && it.text().trim().startsWith("Quote:") && it.@class == "smallfont"
        }
        if (quotenodes) {
            quotenodes.each {
                def parent = it.parent()
                def textToRemove = parent.text()
                cleaned = cleaned - textToRemove
            }
        }
        return cleaned
    }

    static public String stripTags(String text) {
        return toNode(text).text()
    }


    static public boolean hasParent(GPathResult node, Closure c) {
        node = node.parent()
        while (node != null) {
            if (c(node)) return true
            def nextNode = node.parent()
            if (node.name() == nextNode.name() && node.@id == nextNode.@id) {
                break
            }
            node = node.parent()
        }
        return false
    }

    static public LocalDateTime convertDate(String time) {
        if (time == null || time.trim().length() == 0) {
            return null
        }
        //"yyyy.MM.dd G 'at' HH:mm:ss z"
        //Yesterday, 23:48
        //18-03-2011, 12:51
        LocalDateTime date
        time = time.trim()
        if (time =~ "Yesterday") {
            time = time.replace(",","")
            def fmt = DateTimeFormatter.ofPattern("'Yesterday 'HH:mm").withZone(ZoneId.of("Europe/Brussels"))
            def rawtime = LocalTime.parse(time, fmt)
            def rawdate = LocalDateTime.now().with(rawtime)
            date = rawdate.minusDays(1)
        }
        else if (time =~ "Today") {
            time = time.replace(",","")
            def fmt = DateTimeFormatter.ofPattern("'Today 'HH:mm").withZone(ZoneId.of("Europe/Brussels"))
            def rawtime = LocalTime.parse(time, fmt)
            date = LocalDateTime.now().with(rawtime)
        }
        else  if (time =~ ",") {
            def fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy',' HH:mm").withZone(ZoneId.of("Europe/Brussels"))
            def rawdate = LocalDateTime.parse(time, fmt)
            date = rawdate.minusDays(1)
        }
        else {
            def fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.of("Europe/Brussels"))
            def rawdate = LocalDateTime.parse(time, fmt)
            date = rawdate.minusDays(1)
        }
        return date
    }

    static public Integer convertInteger(String value) {
        if(value == null || value.trim().length() == 0) {
            return null
        }
        else {
            value = value.trim()
            value = value.replace(".","")
            value = value.replace(",","")
            return Integer.valueOf(value)
        }
    }


}



