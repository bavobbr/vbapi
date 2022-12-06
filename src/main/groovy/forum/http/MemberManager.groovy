package forum.http

import forum.model.Member
import forum.utils.PostUtils
import forum.model.Credentials
import groovy.util.slurpersupport.GPathResult

import java.time.LocalDateTime

class MemberManager
{

  private Browser browser

  public MemberManager(String host, Credentials credentials)
  {
    browser = new Browser(host)
    browser.login(credentials.username, credentials.password)
  }

  public List<Member> collectMembers(int max)
  {
    Set<String> ids = []

    int page = 1
    while (ids.size() < max)
    {
      def memberspage = browser.collectMembersByPosts(Math.min(100, max), page)
      List<String> memberIds = parseMemberIds(memberspage)
      ids.addAll(memberIds)
      page++
      println "memberIds = $memberIds"
    }
    Set<Member> members = []
    ids.each {
      Member member = getMember(it.toInteger())
      println "added member $member"
      members << member
    }
    List<Member> sorted = members.sort { def a, b -> b.totalPosts <=> a.totalPosts }
    return sorted.take(max)
  }

  public Member getMember(Integer memberId)
  {
    def memberpage = browser.getMemberPage(memberId)
    def nameValue = parseName(memberpage)
    if (!nameValue)
    {
      return null
    }
    else
    {
      def totalPostsValue = parseTotalPosts(memberpage)
      def lastActiveValue = parseLastActive(memberpage)
      def dateJoinedValue = parseDateJoined(memberpage)
      def avatarValue = parseAvatar(memberpage)
      Integer totalPosts = PostUtils.convertInteger(totalPostsValue)
      LocalDateTime lastActive = PostUtils.convertDate(lastActiveValue)
      LocalDateTime dateJoined = PostUtils.convertDate(dateJoinedValue)
      URL avatar = null
      if (avatarValue)
      {
        avatar = ("http://forum.shrimprefuge.be/customavatars/" + avatarValue).toURL()
      }

      return new Member(
              username: nameValue,
              lastActive: lastActive,
              joined: dateJoined,
              totalPosts: totalPosts,
              memberId: memberId,
              avatar: avatar)
    }
  }

  public Member getMember(String username)
  {
    GPathResult searchMemberPage = browser.searchMembers(username)
    def memberIdValue = parseMemberId(searchMemberPage, username)
    if (memberIdValue)
    {
      Integer memberId = PostUtils.convertInteger(memberIdValue)
      println "Member $username has id $memberId"
      return getMember(memberId)
    }
  }

  private String parseAvatar(GPathResult page)
  {
    def linkNode = page.depthFirst().find() {
      it.name() == "IMG" && it.@src.text().startsWith("customavatars/thumbs/")
    }
    def link = linkNode?.@src
    if (link)
    {
      return link.text() - "customavatars/thumbs/"
    }
    else return null
  }

  private String parseMemberId(GPathResult page, String username)
  {
    def linkNode = page.depthFirst().find() {
      it.name() == "A" && it.text()?.toUpperCase() == username.toUpperCase()
    }
    def link = linkNode?.@href
    if (link)
    {
      return link.text() - "member.php?u="
    }
    else return null
  }

  private List<String> parseMemberIds(GPathResult page)
  {
    List<String> memberIds = []
    def linkNodes = page.depthFirst().findAll() {
      it.name() == "A" && it.@href.text().startsWith("member.php?u=")
    }
    linkNodes.each {
      def link = it?.@href
      if (link)
      {
        def memberId = link.text() - "member.php?u="
        memberIds << memberId
      }
    }
    return memberIds
  }


  private String parseName(def page)
  {
    def rowNode = page.depthFirst().find {
      it.name() == "TD" && it.@id == "username_box"
    }
    def nameNode = rowNode.depthFirst().find {
      it.name() == "H1"
    }
    return nameNode?.text()?.trim()
  }

  private String parseTotalPosts(def page)
  {
    def statNodes = page.depthFirst().findAll {
      it.name() == "FIELDSET" && it.@class == "statistics_group"
    }
    def statNode = statNodes[0]
    def listNodes = statNode.depthFirst().findAll {
      it.name() == "LI"
    }
    if (listNodes)
    {
      return listNodes[0].text() - "Total Posts:"
    }
    else
    {
      return null
    }
  }

  private String parseLastActive(def page)
  {
    def statNodes = page.depthFirst().findAll {
      it.name() == "FIELDSET" && it.@class == "statistics_group"
    }
    def statNode = statNodes[1]
    def listNodes = statNode.depthFirst().findAll {
      it.name() == "LI"
    }
    if (listNodes && listNodes.size() == 2)
    {
      return listNodes.first().text() - "Last Activity:"
    }
    else
    {
      return null
    }
  }

  private String parseDateJoined(def page)
  {
    def statNodes = page.depthFirst().findAll {
      it.name() == "FIELDSET" && it.@class == "statistics_group"
    }
    def statNode = statNodes[1]
    def listNodes = statNode.depthFirst().findAll {
      it.name() == "LI"
    }
    if (listNodes)
    {
      return listNodes.last().text() - "Join Date:"
    }
    else
    {
      return null
    }
  }


}
