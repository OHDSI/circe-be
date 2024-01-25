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
    <#if s.offsetUnit = "day" || !s.offsetUnit?has_content>
        The cohort end date will be offset from index event's ${utils.optionName(dateOffsetFieldOptions, s.dateField)} plus ${s.offset} days.
    <#else >
        The cohort end date will be offset from index event's ${utils.optionName(dateOffsetFieldOptions, s.dateField)} plus <@utils.formatValue s.offsetUnitValue s.offsetUnit/>.
    </#if>
</#macro>

<#macro CustomEraStrategy s>
    <#if s.offsetUnit == "day">
The cohort end date will be based on a continuous exposure to ${utils.codesetName(s.drugCodesetId!"", "_invalid drug specified_")}:
allowing ${s.gapDays} days between exposures, adding <@utils.formatValue s.offset "day"/> after exposure ends, and <#if 
s.daysSupplyOverride??>forcing drug exposure days supply to: <@utils.formatValue s.daysSupplyOverride "day"/>.<#else>using days supply and exposure end date for exposure duration.</#if>
    <#else >
The cohort end date will be based on a continuous exposure to ${utils.codesetName(s.drugCodesetId!"", "_invalid drug specified_")}:
allowing ${s.gapUnitValue} ${s.offsetUnit} between exposures, adding <@utils.formatValue s.offsetUnitValue s.offsetUnit/> after exposure ends, and <#if
s.daysSupplyOverride??>forcing drug exposure ${s.offsetUnit} supply to: <@utils.formatValue s.daysSupplyOverride s.offsetUnit/>.<#else>using ${s.offsetUnit} supply and exposure end date for exposure duration.</#if>
    </#if>
</#macro>
