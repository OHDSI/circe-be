Circe-be
========

[![Build Status](https://travis-ci.com/OHDSI/circe-be.svg?branch=master)](https://travis-ci.com/OHDSI/circe-be) [![codecov.io](http://codecov.io/github/OHDSI/circe-be/coverage.svg?branch=master)](http://codecov.io/github/OHDSI/circe-be?branch=master)

Introduction
============
A Java library used to create queries for the OMOP Common Data Model v5.0-v5.3.  These queries are used in cohort definitions (CohortExpression) as well as custom features (CriteriaFeature).

Features
========
- Defines an object model for domain-specific queries (eg: Condition occurrence), correlated criteria (at least N records between X days before and Y days after) and concept set expressions
- Defines an object model for cohort definitions (CohortExpression) where the cohort is defined by Cohort Entry Events, Inclusion Rules and Cohort Exit Strategy.  Resulting episodes are combined into non-overlapping periods of time.
- Generate Negative Control cohorts from a Concept Set expression
- Import a cohort expression from a JSON file and rehydrates the CohortExpression object.
- A suite of unit tests using an embedded instance of Postgres 9.6 to validate proper specification.

Getting Started
===============
Add dependency (and repository) to maven:
```
  <repositories>    
    <repository>
      <id>ohdsi</id>
      <name>repo.ohdsi.org</name>
      <url>http://repo.ohdsi.org:8085/nexus/content/repositories/releases</url>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
      <groupId>org.ohdsi</groupId>
      <artifactId>circe</artifactId>
      <version>1.9.0</version>
    </dependency>
  </dependencies>    
```

License
=======
Circe is licensed under Apache License 2.0
