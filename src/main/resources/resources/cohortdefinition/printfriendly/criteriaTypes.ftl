<#import "./inputTypes.ftl" as inputTypes>
<#import "./utils.ftl" as utils>

<#macro Criteria c level=0 isPlural=true>
<#if c.class.simpleName == "ConditionEra"><@ConditionEra c=c level=level isPlural=isPlural/>
<#elseif c.class.simpleName == "ConditionOccurrence"><@ConditionOccurrence c=c level=level isPlural=isPlural/>
<#else>Unknown criteria type: ${c.class.simpleName}</#if></#macro>

<#macro ConditionEra c level isPlural=true>condition era<#if isPlural>s</#if> of: ${utils.codesetName(c.codesetId, "any condition")}<#if c.first!false>
<@utils.p level=level+1>- only using the first record of ${utils.codesetName(c.codesetId, "any condition")}</@utils.p></#if></#macro>

<#macro ConditionOccurrence c level isPlural=true><#local attrs = []><#if c.age?? || c.gender??>
<#local temp>who are<#if c.gender??> <@inputTypes.ConceptList list=c.gender quote=""/><#if c.age?? && c.gender?size gt 1>,</#if></#if><#if c.age??> <@inputTypes.NumericRange range=c.age /> years old</#if></#local><#local attrs+=[temp]></#if><#if c.occurrenceStartDate?? && c.occurrenceEndDate??>
<#local temp>starting <@inputTypes.DateRange range=c.occurrenceStartDate /> and ending <@inputTypes.DateRange range=c.occurrenceEndDate /></#local><#local attrs+=[temp]><#else><#if c.occurrenceStartDate??> 
<#local temp>starting <@inputTypes.DateRange range=c.occurrenceStartDate /></#local><#local attrs+=[temp]></#if><#if c.occurrenceEndDate??> 
<#local temp>ending <@inputTypes.DateRange range=c.occurrenceEndDate /></#local><#local attrs+=[temp]></#if></#if><#if c.conditionType??>
<#local temp>condition type <#if c.conditionTypeExclude!false>is any of<#else>is not any of</#if> <@inputTypes.ConceptList list=c.conditionType/></#local><#local attrs+=[temp]></#if><#if c.stopReason??> 
<#local temp>with a Stop Reason <@inputTypes.TextFilter filter=c.stopReason /></#local><#local attrs+=[temp]></#if><#if c.providerSpecialty??>
<#local temp>provider specialty is any of: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if c.visitType??>
<#local temp>visit occurrence is any of: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if>
<#if c.first!false>first event of <#else>condition occurrence<#if isPlural>s</#if> of </#if>"${utils.codesetName(c.codesetId, "any condition")}"
<#if c.conditionSourceConcept??> (including "${utils.codesetName(c.conditionSourceConcept, "any condition")}" source concepts)</#if> ${attrs?join("; ")}<#if c.CorrelatedCriteria??>;
<@Group group=c.CorrelatedCriteria level=level+1 indexMessage="a condition occurrence of " + utils.codesetName(c.codesetId, "any condition") /></#if></#macro>

<#-- Group macros -->

<#macro Group group parentGroup = utils._nullArg isFirst=true indexMessage="Cohort Entry Event" level=0 ref=""><@inputTypes.GroupHeader group=group /><br>
<#list group.criteriaList as countCriteria>
<@utils.p level=level+1><@utils.ref ref=ref idx=countCriteria?counter/> <@CountCriteria countCriteria=countCriteria level=level+1/></@utils.p></#list>
<#list group.groups as subgroup><@utils.p level=level+1><@utils.ref ref=ref idx=(group.criteriaList?size + subgroup?counter)/> <@Group group=subgroup 
  parentGroup=group 
  isFirst = !(subgroup?counter gt 1 || group.criteriaList?size gt 0 || group.demographicCriteriaList?size gt 0) indexMessage=indexMessage
  level=level+1 ref=(ref + (group.criteriaList?size + subgroup?counter) + ".")/></@utils.p></#list>
</#macro>

<#-- CountCriteria macros -->


<#macro CountCriteria countCriteria level=0>having ${inputTypes.getCountType(countCriteria)} ${countCriteria.occurrence.count}<#if
countCriteria.occurrence.isDistinct> distinct</#if> <@Criteria c=countCriteria.criteria level=level isPlural=(countCriteria.occurrence.count != 1)/>
<@utils.p level=level+1><#if countCriteria.startWindow.start.days?? || countCriteria.startWindow.end.days??>where <@inputTypes.Window countCriteria.startWindow /></#if>
<#if countCriteria.endWindow?? && (countCriteria.endWindow.start.days?? || countCriteria.endWindow.end.days??)><br>
<#if countCriteria.startWindow.start.days?? || countCriteria.startWindow.end.days??>and </#if><@inputTypes.Window countCriteria.endWindow /></#if></@utils.p>
</#macro>

