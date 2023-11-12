<#-- 

Note!!!!!  
FTL and markdown are both EXTREMELY sensitive to white space, leading to awkward formatting of
if-else statements and other end tags to eliminate CR/LF and other whitespace that interferes with proper
markdown rendering.  Do not try to change the formatting/indentation of these statements or else you risk 
breaking the markdown formatting rules!
END Note!!!!

-->
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

<#macro DateRange range>${utils.optionName(dateRangeOptions, range.op)} <#--
--><#if range.value?has_content>${utils.formatDate(range.value)}<#else>_empty_</#if><#--
--><#if range.op?ends_with("bt")> and <#if range.extent?has_content>${utils.formatDate(range.extent)}<#else>_empty_</#if></#if></#macro>

<#-- ConceptList -->
<#macro ConceptList list quote="\""><#if (list?size > 0)><#list list?map(item->(quote + item.conceptName?lower_case + quote)) as item><#if 
item?counter gt 1><#if item?counter == list?size> or <#else>, </#if></#if>${item}</#list><#else>[none specified]</#if></#macro>

<#-- ConceptSetSelection -->
<#macro ConceptSetSelection selection defaultName="any"><#if selection.isExcluded!false>not </#if>in ${utils.codesetName(selection.codesetId!"", defaultName)}</#macro>

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

<#-- Limits -->
<#assign resultLimitOptions = [
  {"id": "All", "name": "all events"},
  {"id": "First", "name": "earliest event"},
  {"id": "Last", "name": "latest event"}
]/>

<#macro Limit limit>${utils.optionName(resultLimitOptions, limit.type)}</#macro>

<#-- Group -->
<#assign groupTypeOptions = [
  {"id": 'ALL', "name": 'all'},
  {"id": 'ANY', "name": 'any'},
  {"id": 'AT_LEAST', "name": 'at least'},
  {"id": 'AT_MOST', "name": 'at most'}
]>

<#-- Count (with Window) -->

<#assign countTypeOptions = [
  {"id": 1, "name": 'at most'},
  {"id": 0, "name": 'exactly'},
  {"id": 2, "name": 'at least'}
]>

<#function getCountType(countCriteria)>
  <#return utils.optionName(countTypeOptions, countCriteria.occurrence.type)>
</#function>

<#function whichEventPart useEnd><#if useEnd><#return "ending"><#else><#return "starting"></#if></#function>
<#function whichIndexPart useEnd><#if useEnd><#return "end date"><#else><#return "start date"></#if></#function>
<#function temporalDirection coeff><#if coeff lt 0><#return "before"><#else><#return "after"></#if></#function>

<#macro Window w indexLabel="cohort entry">${whichEventPart(w.useEventEnd!false)} <#--
--><#if !w.start.days?? && w.end.days == 0 && w.start.coeff == -1 >anytime on or before ${indexLabel} ${whichIndexPart(w.useIndexEnd!false)}<#--
--><#elseif (w.end.days!0) == 1 && w.start.coeff == -1 && w.end.coeff == -1><#if w.start.days??>in the ${w.start.days} days<#else>anytime</#if> prior to ${indexLabel} ${whichIndexPart(w.useIndexEnd!false)}<#--
--><#elseif !w.start.days?? && (w.end.days!0) gt 1 && w.start.coeff == -1>anytime up to ${w.end.days} days ${temporalDirection(w.end.coeff)} ${indexLabel} ${whichIndexPart(w.useIndexEnd!false)}<#--
--><#elseif !w.end.days?? && (w.start.days!0) gt 0 && w.end.coeff ==1> ${w.start.days} days ${temporalDirection(w.start.coeff)} ${indexLabel} ${whichIndexPart(w.useIndexEnd!false)}<#--
--><#else>between ${w.start.days!"all"} days ${temporalDirection(w.start.coeff)} and ${w.end.days!"all"} days ${temporalDirection(w.end.coeff)} ${indexLabel} ${whichIndexPart(w.useIndexEnd!false)}</#if></#macro>

<#-- User Defined Period -->

<#macro UserDefinedPeriod p><#if 
p.startDate?has_content>a user defiend start date of ${utils.formatDate(p.startDate)}<#if p.endDate?has_content> and</#if></#if><#if
p.endDate?has_content><#if !p.startDate?has_content>a user defined</#if> end date of ${utils.formatDate(p.endDate)}</#if></#macro>

<#-- Date Adjustment -->

<#function toDatePart dateType><#if dateType == "START_DATE"><#return "start date"><#else><#return "end date"></#if></#function>

<#macro DateAdjustment da><#if 
da?has_content>starting ${(da.startOffset != 0)?then((da.startOffset?abs + " days " + (da.startOffset < 0)?then("before","after")), "on")}${(da.startWith != da.endWith)?then(" the event ${toDatePart(da.startWith)}","")}<#--
--> and ending ${(da.endOffset != 0)?then((da.endOffset?abs + " days " + (da.endOffset < 0)?then("before","after")), "on")} the event ${toDatePart(da.endWith)}</#if></#macro>