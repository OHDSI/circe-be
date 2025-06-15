package org.ohdsi.analysis.versioning;

/**
 * Simple stub for CDM Compatibility Spec - replacement for the missing dependency
 */
public interface CdmCompatibilitySpec {
    String getCdmVersionRange();
    void setCdmVersionRange(String cdmVersionRange);
}