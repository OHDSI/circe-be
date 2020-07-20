<#assign _nullArg = {}>
<#function optionName options id>
  <#return (options?filter(op -> op.id == id))?first.name>
</#function>

<#function indent level=0>
  <#return (level+1) + "em">
</#function>

<#function codesetName codesetId defaultName>
  <#if !codesetId??>
    <#return defaultName>
  <#else>
    <#return optionName(conceptSets, codesetId)>
  </#if>
</#function>

<#macro showKeys obj>
<#list obj?keys as key>
  ${key}
</#list>
</#macro>