/*
 *   Copyright 2017 Observational Health Data Sciences and Informatics
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Authors: Vitaly Koulakov
 *
 */

package org.ohdsi.circe.check.checkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.ohdsi.circe.cohortdefinition.*;

class CriteriaCheckerFactory {

    private ConceptSet conceptSet;

    private CriteriaCheckerFactory(ConceptSet concept) {

        this.conceptSet = concept;
    }

    static CriteriaCheckerFactory getFactory(ConceptSet conceptSet) {
        return new CriteriaCheckerFactory(conceptSet);
    }

    Function<Criteria, Boolean> getCriteriaChecker(Criteria criteria) {
        Function<Criteria, Boolean> result = c -> false;
        if (criteria instanceof ConditionEra) {
            result = c -> Objects.equals(((ConditionEra)c).codesetId, conceptSet.id);
        } else if (criteria instanceof ConditionOccurrence) {
            result = c -> 
                    Objects.equals(((ConditionOccurrence)c).codesetId, conceptSet.id) || Objects.equals(((ConditionOccurrence)c).conditionSourceConcept, conceptSet.id);
        } else if (criteria instanceof Death) {
            result = c -> Objects.equals(((Death)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DeviceExposure) {
            result = c -> Objects.equals(((DeviceExposure)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DoseEra) {
            result = c -> Objects.equals(((DoseEra)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DrugEra) {
            result = c -> Objects.equals(((DrugEra)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DrugExposure) {
            result = c -> Objects.equals(((DrugExposure)c).codesetId, conceptSet.id) || Objects.equals(((DrugExposure)c).drugSourceConcept, conceptSet.id);
        } else if (criteria instanceof Measurement) {
            result = c -> Objects.equals(((Measurement)c).codesetId, conceptSet.id) || Objects.equals(((Measurement)c).measurementSourceConcept, conceptSet.id);
        } else if (criteria instanceof Observation) {
            result = c -> Objects.equals(((Observation)c).codesetId, conceptSet.id) || Objects.equals(((Observation)c).observationSourceConcept, conceptSet.id);
        } else if (criteria instanceof ProcedureOccurrence) {
            result = c -> Objects.equals(((ProcedureOccurrence)c).codesetId, conceptSet.id) || Objects.equals(((ProcedureOccurrence)c).procedureSourceConcept, conceptSet.id);
        } else if (criteria instanceof Specimen) {
            result = c -> Objects.equals(((Specimen)c).codesetId, conceptSet.id);
        } else if (criteria instanceof VisitOccurrence) {
            result = c -> Objects.equals(((VisitOccurrence) c).codesetId, conceptSet.id);
        } else if (criteria instanceof VisitDetail) {
            result = c -> Objects.equals(((VisitDetail) c).codesetId, conceptSet.id) || checkConceptSetSelection(c);
        } else if (criteria instanceof LocationRegion) {
            result = c -> Objects.equals(((LocationRegion) c).codesetId, conceptSet.id);
        }
        return result;
    }

    private boolean checkConceptSetSelection(Criteria criteria) {
        boolean result = false;
        for (Supplier<ConceptSetSelection> supplier : getSuppliers(criteria)) {
            ConceptSetSelection conceptSetSelection = supplier.get();
            if (Objects.nonNull(conceptSetSelection)) {
                result = result || Objects.equals(conceptSetSelection.codesetId, conceptSet.id);
            }
        }
        return result;
    }

    private static <T extends Criteria> List<Supplier<ConceptSetSelection>> getSuppliers(T criteria) {
        List<Supplier<ConceptSetSelection>> suppliers = new ArrayList<>();
        if (criteria instanceof VisitDetail) {
            suppliers.add(() -> ((VisitDetail) criteria).placeOfServiceCS);
            suppliers.add(() -> ((VisitDetail) criteria).genderCS);
            suppliers.add(() -> ((VisitDetail) criteria).providerSpecialtyCS);
            suppliers.add(() -> ((VisitDetail) criteria).visitDetailTypeCS);
        }

        return suppliers;
    }
}
