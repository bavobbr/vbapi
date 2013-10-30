package forum.utils

import groovy.util.slurpersupport.GPathResult
import forum.model.Post
import java.text.SimpleDateFormat

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

    static public Date convertDate(String time) {
        if (time == null || time.trim().length() == 0) {
            return null
        }
        //"yyyy.MM.dd G 'at' HH:mm:ss z"
        //Yesterday, 23:48
        //18-03-2011, 12:51
        Date now = new Date()
        SimpleDateFormat sdf
        Date date
        if (time =~ "Yesterday") {
            time = time.replace(",","")
            sdf = new SimpleDateFormat("'Yesterday 'HH:mm")
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"))
            date = sdf.parse(time.trim())
            date[Calendar.YEAR] = now[Calendar.YEAR]
            date[Calendar.MONTH] = now[Calendar.MONTH]
            date[Calendar.DAY_OF_MONTH] = now[Calendar.DAY_OF_MONTH]
            date = date - 1
        }
        else if (time =~ "Today") {
            time = time.replace(",","")
            sdf = new SimpleDateFormat("'Today 'HH:mm")
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"))
            date = sdf.parse(time.trim())
            date[Calendar.YEAR] = now[Calendar.YEAR]
            date[Calendar.MONTH] = now[Calendar.MONTH]
            date[Calendar.DAY_OF_MONTH] = now[Calendar.DAY_OF_MONTH]
        }
        else  if (time =~ ",") {
            sdf = new SimpleDateFormat("dd-MM-yyyy',' HH:mm")
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"))
            date = sdf.parse(time.trim())
        }
        else {
            sdf = new SimpleDateFormat("dd-MM-yyyy")
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"))
            date = sdf.parse(time.trim())
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



