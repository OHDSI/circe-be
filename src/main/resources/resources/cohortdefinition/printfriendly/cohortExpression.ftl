<#import "./criteriaTypes.ftl" as ct>
<#import "./endStrategyTypes.ftl" as st>
<#import "./utils.ftl" as utils>
<#import "./inputTypes.ftl" as inputTypes>
<#-- 

Note!!!!!  
FTL and markdown are both EXTREMLY senstive to white space, leading to awkward formatting of
if-else statements and other end tags to eliminate CR/LF and other witespace that interferes with proper
markdown rendering.  Do not try to change the formatting/indentation of these statements or else you risk 
breaking the markdown formatting rules!
END Note!!!!

-->
<#-- main template: begin -->
### Cohort Entry Events
<#list primaryCriteria.criteriaList>

People<#if primaryCriteria.observationWindow.priorDays gt 0 || primaryCriteria.observationWindow.postDays gt 0><#--
--> with continuous observation of <#if primaryCriteria.observationWindow.priorDays gt 0>${primaryCriteria.observationWindow.priorDays} days before<#if primaryCriteria.observationWindow.postDays gt 0> and </#if></#if><#if 
	primaryCriteria.observationWindow.postDays gt 0>${primaryCriteria.observationWindow.postDays} days after</#if> event</#if><#--
--><#if inclusionRules?size gt 0 || additionalCriteria??> may</#if> enter the cohort when observing any of the following:
<#items as pc>

${pc?counter}. <@ct.Criteria c=pc/>
</#items>
</#list>
<#if primaryCriteria.primaryLimit.type != "All">

Limit cohort entry events to the <@inputTypes.Limit limit=primaryCriteria.primaryLimit/> per person.
</#if>
<#if additionalCriteria??>

Restrict entry events to <@ct.Group group=additionalCriteria />  
<#if primaryCriteria.primaryLimit.type == "All" && qualifiedLimit.type != "All">

Limit these restricted entry events to the <@inputTypes.Limit limit=qualifiedLimit /> per person.
</#if>
</#if>
<#if inclusionRules?size gt 0>

### Inclusion Criteria
<#list inclusionRules as rule>

#### ${rule?counter}. ${(rule.name)!"Unnamed Rule"}<#if rule.description??>: ${rule.description}  </#if>

Entry events <@ct.Group group=rule.expression />
</#list>
</#if>
<#if primaryCriteria.primaryLimit.type == "All" && (!additionalCriteria?? || qualifiedLimit.type == "All") && expressionLimit.type != "All">

Limit qualifying entry events to the <@inputTypes.Limit limit=qualifiedLimit /> per person.
</#if>

### Cohort Exit

<@st.Strategy endStrategy!{} />
<#list censoringCriteria![]>
The person exits the cohort when encountering any of the following events:
<#items as cc>
${cc?counter}. <@ct.Criteria c=cc/>
</#items>
</#list>

### Cohort Eras

Remaining events will be combined into cohort eras if they are within ${collapseSettings.eraPad} days of each other.

<#-- main template: end -->

