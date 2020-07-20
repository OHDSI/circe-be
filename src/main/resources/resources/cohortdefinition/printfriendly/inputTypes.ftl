<#import "./utils.ftl" as utils>
<#-- DateRange -->
<#assign dateRangeOptions = [
  {"id": "lt", "name": "before"},
  {"id": "lte", "name": "on or Before"},
  {"id": "eq", "name": "on"},
  {"id": "gt", "name": "after"},
  {"id": "gte", "name": "on or after"},
  {"id": "bt","name": "between"},
  {"id": "!bt", "name": "not between"}
]>

<#macro DateRange range>${utils.optionName(dateRangeOptions, range.op)} ${range.value!""}<#if range.op?ends_with("bt")> and ${range.extent!""}</#if></#macro>

<#-- ConceptList -->
<#macro ConceptList list>${list?map(item->item.conceptName)?join(", ")}</#macro>

<#-- NumericRange -->
<#assign numericRangeOptions = [
  {"id": "lt", "name": "&lt;"}, 
  {"id": "lte", "name": "&lt;="},
  {"id": "eq", "name": "="}, 
  {"id": "gt", "name": "&gt;"},
  {"id": "gte", "name": "&gt;="},
  {"id": "bt", "name": "between"},
  {"id": "!bt", "name": "not Between"}
]>

<#macro NumericRange range>${utils.optionName(numericRangeOptions, range.op)} ${range.value!""}<#if range.op?ends_with("bt")> and ${range.extent!""}</#if></#macro>

<#-- TextFilter -->
<#assign textFilterOptions = [
  {"id": 'startsWith', "name": 'starting with'},
  {"id": 'contains', "name": 'containing'},
  {"id": 'endsWith', "name": 'ending with'},
  {"id": '!startsWith', "name": 'not starting with'},
  {"id": '!contains',"name": 'not containing'},
  {"id": '!endsWith',"name": 'not ending with'}
]/>

<#macro TextFilter filter>${utils.optionName(textFilterOptions, filter.op)} "${filter.text!""}"</#macro>

<#-- Group -->
<#assign groupTypeOptions = [
  {"id": 'ALL', "name": 'all'},
  {"id": 'ANY', "name": 'any'},
  {"id": 'AT_LEAST', "name": 'at least'},
  {"id": 'AT_MOST', "name": 'at most'}
]>

<#function getGroupConjunction group>
  <#if group.type == "ALL"><#return "and"><#else><#return "or"></#if>
</#function>

<#macro GroupCountConstraint group>${utils.optionName(groupTypeOptions, group.type)}<#if group.type?starts_with("AT_")> ${group.count}</#if></#macro>

<#macro GroupHeader group>having <@GroupCountConstraint group=group/> of the following criteria:</#macro>

<#-- Count (with Window) -->

<#assign countTypeOptions = [
  {"id": 1, "name": 'at most'},
  {"id": 0, "name": 'exactly'},
  {"id": 2, "name": 'at least'}
]>

<#function getCountType(countCriteria)>
  <#return utils.optionName(countTypeOptions, countCriteria.occurrence.type)>
</#function>

<#function whichEventPart useEnd><#if useEnd><#return "event ends"><#else><#return "event starts"></#if></#function>
<#function whichIndexPart useEnd><#if useEnd><#return "index end date"><#else><#return "index start date"></#if></#function>
<#function temporalDirection coeff><#if coeff lt 0><#return "before"><#else><#return "after"></#if></#function>

<#macro Window w>${whichEventPart(w.useEventEnd!false)} between ${w.start.days!"all"} days ${temporalDirection(w.start.coeff)}
 and ${w.end.days!"all"} days ${temporalDirection(w.end.coeff)} ${whichIndexPart(w.useIndexEnd!false)}</#macro>
