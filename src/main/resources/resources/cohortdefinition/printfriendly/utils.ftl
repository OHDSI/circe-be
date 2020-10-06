<#-- 

Note!!!!!  
FTL and markdown are both EXTREMLY senstive to white space, leading to awkward formatting of
if-else statements and other end tags to eliminate CR/LF and other witespace that interferes with proper
markdown rendering.  Do not try to change the formatting/indentation of these statements or else you risk 
breaking the markdown formatting rules!
END Note!!!!

-->
<#assign _nullArg = {}>
<#function optionName options id>
  <#return (options?filter(op -> op.id == id))?first.name>
</#function>

<#macro indent level=0><#list 1..((level*3)) as x> </#list></#macro>

<#function codesetName codesetId defaultName>
  <#if !codesetId?has_content>
    <#return defaultName>
  <#else>
    <#return "'" + optionName(conceptSets, codesetId) + "'">
  </#if>
</#function>

<#function formatDate dateString>
  <#if dateString?matches("^\\d{4}-\\d{2}-\\d{2}$")>
    <#return dateString?date["yyyy-MM-dd"]?string["MMMMM d, yyyy"]>
  <#else>
    <#return "_invalid date_">
  </#if>
</#function>

<#function renderCheckbox value>
  <#if value == true><#return "YES"><#else><#return "NO"></#if>
</#function>

<#macro formatValue value unit="">${value}<#if unit?has_content> ${unit}<#if value != 1>s</#if></#if></#macro>

<#macro showKeys obj>
<#list obj?keys as key>
  ${key}
</#list>
</#macro>
