{
  "result": {
  <#if error??>
    "error": "${error}",
  </#if>
  <#if user??>
    "nodeRef": "${user.nodeRef}",
    "username": "<#if user.properties.userName??>${user.properties.userName}</#if>",
    "firstname": "<#if user.properties.firstName??>${user.properties.firstName}</#if>",
    "lastname": "<#if user.properties.lastName??>${user.properties.lastName}</#if>",
    "email": "<#if user.properties.email??>${user.properties.email}</#if>",
    "code": "<#if user.properties["cloud:signinCode"]??>${user.properties["cloud:signinCode"]}</#if>"
  </#if>
  }
}
