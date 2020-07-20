<#import "./inputTypes.ftl" as inputTypes>
<#import "./utils.ftl" as utils>
<style>
  div.circe-printfriendly {
    font-family:monospace;
    font-size:11pt;
  }
  div.circe-printfriendly p.title {
    font-weight: bold;
    margin-bottom:5px;
  }
  div.circe-printfriendly p.title {
    margin-bottom:2px;
  }
  div.circe-printfriendly p {
    margin-top:0px;
    margin-bottom:0px;
  }

  div.circe-printfriendly p.attr {
    text-indent:-1em;
  }
</style>

<#-- main template: begin -->
<div class="circe-printfriendly">
<p class="title">Cohort Entry Events</p>
<#list primaryCriteria.criteriaList><p class="heading">Patients <#if inclusionRules?size gt 0 || additionalCriteria??>may </#if>enter the cohort having:</p>
<#items as pc><@p><#if pc?counter gt 1>or </#if><@Criteria c=pc/></@p>
</#items>
</#list>
</div>
<#-- main template: end -->

<#macro p level=0><p class="attr" style="margin-left: ${utils.indent(level)}"><#nested></p></#macro>

<#-- Criteria macros  -->

<#macro Criteria c level=0 isPlural=true>
<#if c.class.simpleName == "ConditionEra"><@ConditionEra c=c level=level isPlural=isPlural/>
<#elseif c.class.simpleName == "ConditionOccurrence"><@ConditionOccurrence c=c level=level isPlural=isPlural/>
<#else>Unknown criteria type: ${c.class.simpleName}</#if></#macro>

<#macro ConditionEra c level isPlural=true>condition era<#if isPlural>s</#if> of: ${utils.codesetName(c.codesetId, "any condition")}<#if c.first!false>
<@p level=level+1>- only using the first record of ${utils.codesetName(c.codesetId, "any condition")}</@p></#if></#macro>

<#macro ConditionOccurrence c level isPlural=true>condition occurrence<#if isPlural>s</#if> of: ${utils.codesetName(c.codesetId, "any condition")}<#if c.first!false>
<@p level=level+1>- only using the first record of ${utils.codesetName(c.codesetId, "any condition")}</@p></#if><#if c.occurrenceStartDate?? && c.occurrenceEndDate??>
<@p level=level+1>- starting <@inputTypes.DateRange range=c.occurrenceStartDate /> and ending <@inputTypes.DateRange range=c.occurrenceEndDate /></@p><#else><#if c.occurrenceStartDate??> 
<@p level=level+1>- starting <@inputTypes.DateRange range=c.occurrenceStartDate /></@p></#if><#if c.occurrenceEndDate?? > 
<@p level=level+1>- ending <@inputTypes.DateRange range=c.occurrenceEndDate /></@p></#if></#if><#if c.conditionType??>
<@p level=level+1>- condition type <#if c.conditionTypeExclude!false>is any of<#else>is not any of</#if> <@inputTypes.ConceptList list=c.conditionType/></@p></#if><#if c.stopReason?? > 
<@p level=level+1>- with a Stop Reason <@inputTypes.TextFilter filter=c.stopReason /></@p></#if><#if c.conditionSourceConcept?? >
<@p level=level+1>- condition source concept is: ${utils.codesetName(c.conditionSourceConcept, "any condition")}</@p></#if><#if c.age??>
<@p level=level+1>- with age <@inputTypes.NumericRange range=c.age /></@p></#if><#if c.gender??>
<@p level=level+1>- gender is any of: <@inputTypes.ConceptList list=c.gender/></@p></#if><#if c.providerSpecialty??>
<@p level=level+1>- provider specialty is any of: <@inputTypes.ConceptList list=c.providerSpecialty/></@p></#if><#if c.visitType??>
<@p level=level+1>- visit occurrence is any of: <@inputTypes.ConceptList list=c.visitType/></@p></#if><#if c.CorrelatedCriteria??>
<@p level=level+1>- <@Group group=c.CorrelatedCriteria level=level+1 indexMessage="a condition occurrence of " + utils.codesetName(c.codesetId, "any condition") /></@p></#if></#macro>

<#-- Group macros -->

<#macro Group group parentGroup = utils._nullArg isFirst=true indexMessage="Cohort Entry Event" level=0><#if !isFirst && (parentGroup?has_content)>${inputTypes.getGroupConjunction(parentGroup)} </#if><@inputTypes.GroupHeader group=group /><br>
<#list group.criteriaList as countCriteria>
<@p level=level+1>- <#if countCriteria?counter gt 1 && true && true
&& true>${inputTypes.getGroupConjunction(group)} </#if><@CountCriteria countCriteria=countCriteria level=level+1/></@p></#list>
<#list group.groups as subgroup><@p level=level+1>- <@Group group=subgroup 
  parentGroup=group 
  isFirst = !(subgroup?counter gt 1 || group.criteriaList?size gt 0 || group.demographicCriteriaList?size gt 0) indexMessage=indexMessage
  level=level+1/></@p></#list>
</#macro>

<#-- CountCriteria macros -->


<#macro CountCriteria countCriteria level=0>having ${inputTypes.getCountType(countCriteria)} ${countCriteria.occurrence.count}<#if
countCriteria.occurrence.isDistinct> distinct</#if> <@Criteria c=countCriteria.criteria level=level isPlural=(countCriteria.occurrence.count != 1)/>
<@p level=level><#if countCriteria.startWindow.start.days?? || countCriteria.startWindow.end.days??>where <@inputTypes.Window countCriteria.startWindow /></#if>
<#if countCriteria.endWindow?? && (countCriteria.endWindow.start.days?? || countCriteria.endWindow.end.days??)><br>
<#if countCriteria.startWindow.start.days?? || countCriteria.startWindow.end.days??>and </#if><@inputTypes.Window countCriteria.endWindow /></#if></@p>
</#macro>


