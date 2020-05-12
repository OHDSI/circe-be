<#import "./inputTypes.ftl" as inputTypes>
This is the template!
Root class is ${.dataModel.class.simpleName} 
isInstance test: ${.dataModel.class.simpleName == "CohortExpression"}

<style>
  circe-printfriendly.div span.title {
    font-weight: bold;
  }
</style>
<div class="circe-printfriendly">
<span style="title">Cohort Entry Criteria</span>

<#list primaryCriteria.criteriaList as pc>
<@criteria c=pc/>
</#list>

</div>

<#macro criteria c level=0>
<p style="margin-left: ${indent(level)}">
<#if c.class.simpleName == "ConditionOccurrence">
<@ConditionOccurrence co = c/>
<#else>
Unknown criteria type: ${c.class.simpleName}
</#if>
</p>
</#macro>

<#macro ConditionOccurrence co>
<#list co?keys as key>
  ${key}
</#list>
${co.occurrenceStartDate!'wtf'}
${co["occurrenceStartDate"]}
${co.occurrenceStartDate.value}
A condition occurrence of: ${codesetName(co.codesetId, "any condition")}
<#if co.first!false>- for the first time in the person's history</#if>
<#if co.occurrenceStartDate?? > <@inputTypes.DateRange range = co.occurrenceStartDate /> <#else> is null</#if>
</#macro>


<#function indent level=0>
  <#return (level * 3) + "px">
</#function>

<#function codesetName codesetId defaultName>
  <#if !codesetId??>
    <#return defaultName>
  <#else>
    <#return (conceptSets?filter(cs -> cs.id == codesetId))?first.name>
   </#if>
</#function>

<#macro dump_object object debug=false>
    <#compress>
        <#if object??>
            <#attempt>
                <#if object?isNode>
                    <#if object?nodeType == "text">${object?html}
                    <#else>&lt;${object?nodeName}<#if object?nodeType=="element" && object.@@?hasContent><#list object.@@ as attr>
                        ${attr?nodeName}="${attr?html}"</#list></#if>&gt;
                        <#if object?children?hasContent><#list object?children as item>
                            <@dump_object object=item/></#list><#else>${object}</#if> &lt;/${object?nodeName}&gt;</#if>
                <#elseIf object?isMethod>
                    #method
                <#elseIf object?isSequence>
                        [<#list object as item><@dump_object object=item/><#if !item?isLast>, </#if></#list>]
                <#elseIf object?isHashEx>
                        {<#list object as key, item>${key?html}=<@dump_object object=item/><#if !item?isLast>, </#if></#list>}
                <#else>
                    "${object?string?html}"
                </#if>
            <#recover>
                <#if !debug><!-- </#if>LOG: Could not parse object <#if debug><pre>${.error}</pre><#else>--></#if>
            </#attempt>
        <#else>
            null
        </#if>
    </#compress>
</#macro>


