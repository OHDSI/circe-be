<#-- 

Note!!!!!  
FTL and markdown are both EXTREMLY senstive to white space, leading to awkward formatting of
if-else statements and other end tags to eliminate CR/LF and other witespace that interferes with proper
markdown rendering.  Do not try to change the formatting/indentation of these statements or else you risk 
breaking the markdown formatting rules!
END Note!!!!

-->
<#import "./utils.ftl" as utils>

<#macro Strategy s>
<#if !s?has_content><@DefaultExit/>
<#elseif s.class.simpleName == "DateOffsetStrategy"><@DateOffsetStrategy s />
<#elseif s.class.simpleName == "CustomEraStrategy"><@CustomEraStrategy s />
<#else>Unknown cohort exit strategy type: ${s.class.simpleName}</#if></#macro>

<#macro DefaultExit >
The person exits the cohort at the end of continuous observation.
</#macro>

<#-- Date Offset Strategy -->
<#assign dateOffsetFieldOptions = [{"id": "StartDate", "name": "start date"}, {"id": "EndDate", "name": "end date"}]>

<#macro DateOffsetStrategy s>
The cohort end date will be offset from index event's ${utils.optionName(dateOffsetFieldOptions, s.dateField)} plus ${s.offset} days.
</#macro>

<#macro CustomEraStrategy s>
The cohort end date will be based on a continuous exposure to "${utils.codesetName(s.drugCodesetId, "_invalid drug specified_")}":
allowing ${s.gapDays} days between exposures, adding ${s.offset} days after exposure ends, and <#if 
s.daysSupplyOverride??>forcing drug exposure days suply to: ${s.daysSupplyOverride} days.<#else>using days supply and exposure end date for exposure duration.</#if>
</#macro>
