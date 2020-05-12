<#assign dateRangeOptions = [
  {"id": "lt", "name": "before"},
  {"id": "lte", "name": "on or Before"},
  {"id": "eq", "name": "on"},
  {"id": "gt", "name": "after"},
  {"id": "gte", "name": "on or after"},
  {"id": "bt","name": "between"},
  {"id": "!bt", "name": "not between"}
]>

<#function dateRangeOpName id>
  <#return (dateRangeOptions?filter(op -> op.id == id))?first.name>
</#function>

<#macro DateRange range>
${dateRangeOpName(range.op)} ${range.value}<#if range.op?ends_with("bt")> and ${range.extent}</#if>
</#macro>

