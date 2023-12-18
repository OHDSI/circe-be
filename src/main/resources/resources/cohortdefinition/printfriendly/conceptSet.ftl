<#import "./utils.ftl" as utils>
<#list conceptSets as conceptSet>
    ### ${conceptSet.name}
    <#list conceptSet.expression.items>
        |Concept ID|Concept Name|Code|Vocabulary|Excluded|Descendants|Mapped
        |:---|:---------------------------------------|:--|:-----|:--:|:--:|:--:|
        <#items as conceptSetItem>
            |${conceptSetItem.concept.conceptId?c}|<#--
-->${conceptSetItem.concept.conceptName}|<#--
-->${conceptSetItem.concept.conceptCode}|<#--
-->${conceptSetItem.concept.vocabularyId}|<#--
-->${utils.renderCheckbox(conceptSetItem.isExcluded)}|<#--
-->${utils.renderCheckbox(conceptSetItem.includeDescendants)}|<#--
-->${utils.renderCheckbox(conceptSetItem.includeMapped)}|
        </#items>


    <#else>
        There are no concept set items in this concept set.

    </#list>
</#list>
