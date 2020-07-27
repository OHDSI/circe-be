<#import "./criteriaTypes.ftl" as ct>
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
<#items as pc><@utils.p>${pc?counter}. <@ct.Criteria c=pc/></@utils.p>
</#items>
</#list><#if primaryCriteria.observationWindow.priorDays gt 0 || primaryCriteria.observationWindow.postDays gt 0> 
<p> Events must have continuous observation of <#if primaryCriteria.observationWindow.priorDays gt 0> ${primaryCriteria.observationWindow.priorDays} days before<#if primaryCriteria.observationWindow.postDays gt 0> and </#if></#if>
<#if primaryCriteria.observationWindow.priorDays gt 0>${primaryCriteria.observationWindow.postDays} days after</#if> the event start date.</p></#if><#if additionalCriteria??>
<@utils.p>Restrict entry events to <@utils.Group group=additionalCriteria /></@utils.p></#if>
</div>
<#-- main template: end -->

