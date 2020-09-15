<#-- 

Note!!!!!  
FTL and markdown are both EXTREMLY senstive to white space, leading to awkward formatting of
if-else statements and other end tags to eliminate CR/LF and other witespace that interferes with proper
markdown rendering.  Do not try to change the formatting/indentation of these statements or else you risk 
breaking the markdown formatting rules!
END Note!!!!

-->

<#import "./inputTypes.ftl" as inputTypes>
<#import "./utils.ftl" as utils>

<#macro Criteria c level=0 isPlural=true>
<#if c.class.simpleName == "ConditionEra"><@ConditionEra c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "ConditionOccurrence"><@ConditionOccurrence c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "Death"><@Death c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "DeviceExposure"><@DeviceExposure c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "DoseEra"><@DeviceExposure c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "DrugEra"><@DeviceExposure c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "DrugExposure"><@DeviceExposure c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "LocationRegion"><@LocationRegion c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "Measurement"><@Measurement c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "Observation"><@Observation c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "ObservationPeriod"><@ObservationPeriod c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "ProcedureOccurrence"><@ProcedureOccurrence c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "Specimen"><@Specimen c=c level=level isPlural=isPlural />
<#elseif c.class.simpleName == "VisitOccurrence"><@VisitOccurrence c=c level=level isPlural=isPlural />
<#else>Unknown criteria type: ${c.class.simpleName}</#if></#macro>

<#macro ConditionEra c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.ageAtStart!{} ageAtEnd=c.ageAtEnd!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.eraStartDate!{} c.eraEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
condition era<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any condition")}"<#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any condition") /></#if></#macro>

<#macro ConditionOccurrence c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.conditionType??><#local temp>a condition type that<#if c.conditionTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.conditionType/></#local><#local attrs+=[temp]></#if><#if c.stopReason??> 
<#local temp>with a stop reason <@inputTypes.TextFilter filter=c.stopReason /></#local><#local attrs+=[temp]></#if><#if c.providerSpecialty??>
<#local temp>a provider specialty that is: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if c.visitType??>
<#local temp>a visit occurrence that is: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if>
condition occurrence<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any condition")}"<#if 
c.conditionSourceConcept??> (including "${utils.codesetName(c.conditionSourceConcept, "any condition")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any condition") /></#if></#macro>

<#macro Death c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.conditionType??><#local temp>a death type that<#if c.deathTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.deathType/></#local><#local attrs+=[temp]></#if>
death of "${utils.codesetName(c.codesetId, "any condition")}"<#if 
c.deathSourceConcept??> (including "${utils.codesetName(c.deathSourceConcept, "any condition")}" source concepts)</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any death") /></#if></#macro>

<#macro DeviceExposure c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.deviceType??><#local temp>a device type that<#if c.deviceTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.deviceType/></#local><#local attrs+=[temp]></#if><#if c.uniqueDeviceId??> 
<#local temp>unique device ID <@inputTypes.TextFilter filter=c.uniqueDeviceId /></#local><#local attrs+=[temp]></#if><#if c.quantity??>
<#local temp>quantity <@inputTypes.NumericRange range=quantity /></#local><#local attrs+=[temp]></#if><#if c.providerSpecialty??>
<#local temp>a provider specialty that is: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if c.visitType??>
<#local temp>a visit occurrence that is: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if>
device exposure<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any device")}"<#if 
c.deviceSourceConcept??> (including "${utils.codesetName(c.deviceSourceConcept, "any condition")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any device") /></#if></#macro>

<#macro DoseEra c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.ageAtStart!{} ageAtEnd=c.ageAtEnd!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.eraStartDate!{} c.eraEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if c.unit??>
<#local temp>unit is: <@inputTypes.ConceptList list=c.unit/></#local><#local attrs+=[temp]></#if><#if c.eraLength??>
<#local temp>with era length <@inputTypes.NumericRange range=eraLength /></#local><#local attrs+=[temp]></#if><#if c.doseValue??>
<#local temp>with dose value <@inputTypes.NumericRange range=doseValue /></#local><#local attrs+=[temp]></#if>
dose era<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any drug")}"<#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any drug") /></#if></#macro>

<#macro DrugEra c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.ageAtStart!{} ageAtEnd=c.ageAtEnd!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.eraStartDate!{} c.eraEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if c.eraLength??>
<#local temp>with era length <@inputTypes.NumericRange range=eraLength /></#local><#local attrs+=[temp]></#if><#if c.occurrenceCount??>
<#local temp>with occurrence count <@inputTypes.NumericRange range=occurrenceCount /></#local><#local attrs+=[temp]></#if><#if c.gapDays??>
<#local temp>with gap days <@inputTypes.NumericRange range=gapDays /></#local><#local attrs+=[temp]></#if>
drug era<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any drug")}"<#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any drug") /></#if></#macro>

<#macro DrugExposure c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.drugType??><#local temp>a drug type that<#if c.drugTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.drugType/></#local><#local attrs+=[temp]></#if><#if c.refills??>
<#local temp>with refills <@inputTypes.NumericRange range=refills /></#local><#local attrs+=[temp]></#if><#if c.quantity??>
<#local temp>with quantity <@inputTypes.NumericRange range=quantity /></#local><#local attrs+=[temp]></#if><#if c.daysSupply??>
<#local temp>with days supply <@inputTypes.NumericRange range=daysSupply /></#local><#local attrs+=[temp]></#if><#if c.effectiveDrugDose??>
<#local temp>with effective drug dose <@inputTypes.NumericRange range=effectiveDrugDose /></#local><#local attrs+=[temp]></#if><#if c.doseUnit??>
<#local temp>dose unit: <@inputTypes.ConceptList list=c.doseUnit/></#local><#local attrs+=[temp]></#if><#if c.routeConcept??>
<#local temp>with route: <@inputTypes.ConceptList list=c.routeConcept/></#local><#local attrs+=[temp]></#if><#if c.lotNumber??> 
<#local temp>with lot: <@inputTypes.TextFilter filter=c.lotNumber /></#local><#local attrs+=[temp]></#if><#if c.stopReason??> 
<#local temp>with a stop reason <@inputTypes.TextFilter filter=c.stopReason /></#local><#local attrs+=[temp]></#if><#if c.providerSpecialty??>
<#local temp>a provider specialty that is: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if c.visitType??>
<#local temp>a visit occurrence that is: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if>
drug exposure<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any drug")}"<#if 
c.drugSourceConcept??> (including "${utils.codesetName(c.drugSourceConcept, "any drug")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any drug") /></#if></#macro>

<#macro LocationRegion c level isPlural=true><#local attrs = []><#local 
temp><@EventDateCriteria c.startDate!{} c.endDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
location of "${utils.codesetName(c.codesetId, "any location")}"<#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any location") /></#if></#macro>

<#macro Measurement c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.measurementType??><#local temp>a measurement type that<#if c.measurementTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.measurementType/></#local><#local attrs+=[temp]></#if><#if 
c.operator??><#local temp>with operator: <@inputTypes.ConceptList list=c.operator/></#local><#local attrs+=[temp]></#if><#if 
c.valueAsNumber??><#local temp>numeric value <@inputTypes.NumericRange range=valueAsNumber /></#local><#local attrs+=[temp]></#if><#if 
c.unit??><#local temp>unit: <@inputTypes.ConceptList list=c.unit/></#local><#local attrs+=[temp]></#if><#if 
c.valueAsConcept??><#local temp>with value as concept: <@inputTypes.ConceptList list=c.valueAsConcept/></#local><#local attrs+=[temp]></#if><#if 
c.rangeLow??><#local temp>low range <@inputTypes.NumericRange range=rangeLow /></#local><#local attrs+=[temp]></#if><#if 
c.rangeHigh??><#local temp>high range <@inputTypes.NumericRange range=rangeHigh /></#local><#local attrs+=[temp]></#if><#if 
c.rangeLowRatio??><#local temp>low range-to-value ratio <@inputTypes.NumericRange range=rangeLowRatio /></#local><#local attrs+=[temp]></#if><#if 
c.rangeHighRatio??><#local temp>high range-to-value ratio <@inputTypes.NumericRange range=rangeHighRatio /></#local><#local attrs+=[temp]></#if><#if 
c.abnormal!false><#local temp>with an abormal result (measurement value falls outside the low and high range)</#local><#local attrs+=[temp]></#if><#if 
c.providerSpecialty??><#local temp>a provider specialty that is: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if 
c.visitType??><#local temp>a visit occurrence that is: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if>
measurement<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any measurement")}"<#if 
c.measurementSourceConcept??> (including "${utils.codesetName(c.measurementSourceConcept, "any measurement")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any measurement") /></#if></#macro>

<#macro Observation c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.observationType??><#local temp>a measurement type that<#if c.observationTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.observationType/></#local><#local attrs+=[temp]></#if><#if 
c.valueAsNumber??><#local temp>numeric value <@inputTypes.NumericRange range=valueAsNumber /></#local><#local attrs+=[temp]></#if><#if 
c.unit??><#local temp>unit: <@inputTypes.ConceptList list=c.unit/></#local><#local attrs+=[temp]></#if><#if 
c.valueAsConcept??><#local temp>with value as concept: <@inputTypes.ConceptList list=c.valueAsConcept/></#local><#local attrs+=[temp]></#if><#if 
c.valueAsString??><#local temp>with value as string <@inputTypes.TextFilter filter=c.valueAsString /></#local><#local attrs+=[temp]></#if><#if 
c.qualifier??><#local temp>with qualifier: <@inputTypes.ConceptList list=c.qualifier/></#local><#local attrs+=[temp]></#if><#if 
c.providerSpecialty??><#local temp>a provider specialty that is: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if 
c.visitType??><#local temp>a visit occurrence that is: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if>
observation<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any observation")}"<#if 
c.observationSourceConcept??> (including "${utils.codesetName(c.observationSourceConcept, "any observation")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any observation") /></#if></#macro>

<#macro ObservationPeriod c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.ageAtStart!{} ageAtEnd=c.ageAtEnd!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.periodStartDate!{} c.periodEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if
c.userDefinedPeriod??><#local temp><@UserDefinedPeriod c.userDefinedPeriod /></#local><#if temp?has_content><#local attrs+=[temp]></#if></#if><#if 
c.periodType??><#local temp>period type is: <@inputTypes.ConceptList list=c.periodType/></#local><#local attrs+=[temp]></#if><#if 
c.periodLength??><#local temp>with a length <@inputTypes.NumericRange range=periodLength /> days</#local><#local attrs+=[temp]></#if>
observation period<#if isPlural && !(c.first!false)>s</#if><#if 
c.first!false> (first obsrvation period in person's history)</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel="observation period" /></#if></#macro>

<#macro ProcedureOccurrence c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.procedureType??><#local temp>a procedure type that<#if c.procedureTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.procedureType/></#local><#local attrs+=[temp]></#if><#if 
c.modifier??><#local temp>with modifier: <@inputTypes.ConceptList list=c.modifier/></#local><#local attrs+=[temp]></#if><#if 
c.quantity??><#local temp>with quantity <@inputTypes.NumericRange range=quantity /></#local><#local attrs+=[temp]></#if><#if 
c.providerSpecialty??><#local temp>a provider specialty that is: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if 
c.visitType??><#local temp>a visit occurrence that is: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if>
procedure occurrence<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any procedure")}"<#if 
c.procedureSourceConcept??> (including "${utils.codesetName(c.procedureSourceConcept, "any procedure")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any procedure") /></#if></#macro>

<#macro Specimen c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.specimenType??><#local temp>a specimen type that<#if c.specimenTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.specimenType/></#local><#local attrs+=[temp]></#if><#if 
c.quantity??><#local temp>with quantity <@inputTypes.NumericRange range=quantity /></#local><#local attrs+=[temp]></#if><#if 
c.unit??><#local temp>with unit: <@inputTypes.ConceptList list=c.unit/></#local><#local attrs+=[temp]></#if><#if 
c.anatomicSite??><#local temp>with anatomicSite: <@inputTypes.ConceptList list=c.anatomicSite/></#local><#local attrs+=[temp]></#if><#if 
c.diseaseStatus??><#local temp>with diseaseStatus: <@inputTypes.ConceptList list=c.diseaseStatus/></#local><#local attrs+=[temp]></#if><#if
c.sourceId??><#local temp>with source ID <@inputTypes.TextFilter filter=c.sourceId /></#local><#local attrs+=[temp]></#if>
specimen<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any specimen")}"<#if 
c.specimenSourceConcept??> (including "${utils.codesetName(c.specimenSourceConcept, "any specimen")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any specimen") /></#if></#macro>

<#macro VisitOccurrence c level isPlural=true><#local attrs = []><#local 
temp><@AgeGenderCriteria ageAtStart=c.age!{} gender=c.gender!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if>
<#local temp><@EventDateCriteria c.occurrenceStartDate!{} c.occurrenceEndDate!{} /></#local><#if temp?has_content><#local attrs+=[temp]></#if><#if 
c.visitType??><#local temp>a visit type that<#if c.visitTypeExclude!false> is not:<#else> is:</#if> <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if><#if
c.providerSpecialty??><#local temp>a provider specialty that is: <@inputTypes.ConceptList list=c.providerSpecialty/></#local><#local attrs+=[temp]></#if><#if
c.visitType??><#local temp>a visit occurrence that is: <@inputTypes.ConceptList list=c.visitType/></#local><#local attrs+=[temp]></#if><#if 
c.visitLength??><#local temp>with length <@inputTypes.NumericRange range=visitLength /></#local><#local attrs+=[temp]></#if>
visit occurrence<#if isPlural && !(c.first!false)>s</#if> of "${utils.codesetName(c.codesetId, "any visit")}"<#if 
c.visitSourceConcept??> (including "${utils.codesetName(c.visitSourceConcept, "any visit")}" source concepts)</#if><#if 
c.first!false> for the first time in the person's history</#if><#if attrs?size gt 0>, ${attrs?join("; ")}</#if><#if 
c.CorrelatedCriteria??>; <@Group group=c.CorrelatedCriteria level=level indexLabel=utils.codesetName(c.codesetId, "any visit") /></#if></#macro>

<#-- temp has content: '${temp}' -->

<#-- Criteria attribute templates -->

<#macro AgeGenderCriteria ageAtStart gender ageAtEnd={}>
<#if ageAtStart?has_content || ageAtEnd?has_content || gender?has_content>
who are<#if gender?has_content> <@inputTypes.ConceptList list=gender quote=""/><#if (ageAtStart?has_content || ageAtEnd?has_content) && gender?size gt 1>,</#if></#if><#if 
ageAtStart?has_content> <@inputTypes.NumericRange range=ageAtStart /> years old<#if ageAtEnd?has_content> at era start and</#if><#if 
ageAtEnd?has_content> <@inputTypes.NumericRange range=ageAtEnd /> years old at era end</#if></#if></#if></#macro>

<#macro EventDateCriteria startRange endRange>
<#if startRange?has_content && endRange?has_content>
starting <@inputTypes.DateRange range=startRange /> and ending <@inputTypes.DateRange range=endRange /><#else><#if 
startRange?has_content>starting <@inputTypes.DateRange range=startRange /></#if><#if 
endRange?has_content>ending <@inputTypes.DateRange range=endRange /></#if></#if></#macro>

<#-- Group macros -->

<#macro GroupHeader group>with ${utils.optionName(inputTypes.groupTypeOptions, group.type)}<#if group.type?starts_with("AT_")> ${group.count}</#if> of the following criteria:</#macro>

<#macro Group group parentGroup = utils._nullArg isFirst=true indexLabel="cohort entry" level=0><#if 
	group.criteriaList?size gt 1 || ["ANY","ALL"]?seq_index_of(group.type) == -1 || (group.groups?size + group.demographicCriteriaList?size gt 1)><@GroupHeader group=group />
<#list group.criteriaList as countCriteria>


<@utils.indent level=level+1 />${countCriteria?counter}. <@CountCriteria countCriteria=countCriteria level=level+1 indexLabel=indexLabel /></#list>
<#list group.groups as subgroup>

<@utils.indent level=level+1 />${(group.criteriaList?size + subgroup?counter)}. <@Group group=subgroup 
  parentGroup=group 
  isFirst = !(subgroup?counter gt 1 || group.criteriaList?size gt 0 || group.demographicCriteriaList?size gt 0)
  level=level+1 
  indexLabel=indexLabel /></#list><#else><@CountCriteria countCriteria=group.criteriaList[0] level=level indexLabel=indexLabel/>

</#if></#macro>

<#-- CountCriteria macros -->

<#macro CountCriteria countCriteria level=0 indexLabel="cohort entry">having <#if countCriteria.occurrence.type == 0 && countCriteria.occurrence.count == 0>no<#else>${inputTypes.getCountType(countCriteria)} ${countCriteria.occurrence.count}</#if><#if
countCriteria.occurrence.isDistinct> distinct</#if> <@Criteria c=countCriteria.criteria level=level isPlural=(countCriteria.occurrence.count != 1)/><@WindowCriteria countCriteria=countCriteria indexLabel=indexLabel/></#macro>

<#macro WindowCriteria countCriteria indexLabel="cohort entry" level=0><#local windowParts=[] restrictParts=[]>
<#if countCriteria.startWindow.start.days?? || countCriteria.startWindow.end.days??>
<#local temp><@inputTypes.Window countCriteria.startWindow indexLabel /></#local><#local windowParts+=[temp]></#if>
<#if countCriteria.endWindow?? && (countCriteria.endWindow.start.days?? || countCriteria.endWindow.end.days??)>
<#local temp><@inputTypes.Window countCriteria.endWindow indexLabel /></#local><#local windowParts+=[temp]></#if>
<#if countCriteria.restrictVisit!false>
<#local temp>at same visit as "${indexLabel}"</#local><#local restrictParts+=[temp]></#if>
<#if countCriteria.ignoreObservationPeriod!false>
<#local temp>allow events outside observation period</#local><#local restrictParts+=[temp]></#if>
<#if (windowParts?size + restrictParts?size) gt 0>
  
<#if windowParts?size gt 0>${windowParts?join(" and ")}<#if restrictParts?size gt 0>; </#if></#if>${restrictParts?join(" and ")}</#if></#macro>



