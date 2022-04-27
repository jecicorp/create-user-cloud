{
  "result": {
  <#if user??>
    "user" {
      "nodeRef": "${user.nodeRef}",
      "username": "<#if user.properties.userName??>${user.properties.userName}</#if>",
      "firstname": "<#if user.properties.firstName??>${user.properties.firstName}</#if>",
      "lastname": "<#if user.properties.lastName??>${user.properties.lastName}</#if>",
      "email": "<#if user.properties.email??>${user.properties.email}</#if>",
      "organization": "<#if user.properties.organization??>${user.properties.organization}</#if>"
    }<#if site??>,</#if>
  </#if>
  <#if site??>
    "site" {
      "nodeRef": "${site.nodeRef}",
      "shortname": "<#if site.name??>${site.name}</#if>",
      "title": "<#if site.properties.title??>${site.properties.title}</#if>",
      "code": "<#if site.properties["cloud:signinCodeSite"]??>${site.properties["cloud:signinCodeSite"]}</#if>"
    }
  </#if>
  }
}
