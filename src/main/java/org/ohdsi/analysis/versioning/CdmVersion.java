package org.ohdsi.analysis.versioning;

/**
 * Simple stub for CDM Version - replacement for the missing dependency
 */
public @interface CdmVersion {
    String[] value() default {};
    String range() default "";
}