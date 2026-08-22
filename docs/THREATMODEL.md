# Threat Definition

## Purpose

This document defines what a **Threat** is within Pandora. A shared definition ensures
that the UI, repository, data sources, and tests all work with the same model and
make the same assumptions.

Pandora is a location-aware health application. In this context, a threat is an
infectious disease or public health risk that is currently relevant to a user's
location.

## What is a Threat?

A Threat represents an infectious disease that poses a risk to users within a
specific geographical area.

Each threat describes:

- the disease
- how severe the current risk is
- the area where the assessment applies
- guidance for users
- when the information was last updated
- when the information should no longer be considered fresh

## Threat Model

| Field | Type         | Description |
|------|--------------|-------------|
| id | String       | Unique identifier for the threat. |
| name | String       | Disease name (e.g. Cholera, Lassa Fever, Smallpox). |
| severity | Severity     | Current risk level (LOW, MEDIUM, HIGH, CRITICAL). |
| area | Area         | Geographic area covered by this assessment. |
| guidance | List<String> | Recommended actions for users in the affected area. |
| reportedAt | Instant      | When this threat information was published or updated. |
| expiresAt | Instant      | Time after which the information is considered stale. |


## Fresh vs Stale

Threat information changes over time.

A threat is considered **fresh** while the current time is before `expiresAt`.

A threat is considered **stale** once `expiresAt` has passed.

Consumers of the model should not infer freshness from `reportedAt`; they should
use `expiresAt` or repository policies.


## Repository Contract

The rest of the application requests threats through a repository rather than
directly accessing a database, API, or local file.

```kotlin
interface ThreatRepository {
    suspend fun getNearbyThreats(): List<Threat>
}
```

The repository is responsible for determining where threat data comes from.
Consumers should not depend on whether the data originated from a local data
source, a remote service, or mock data.

## Alternatives Considered

### Option A: Return raw API models

Rejected because it would couple the rest of the application to a specific data
source and make replacing or testing that source more difficult.

### Option B: Represent severity as an integer

Rejected because named values such as `LOW`, `MEDIUM`, `HIGH`, and `CRITICAL`
are more expressive and reduce ambiguity throughout the codebase.

### Option C: Infer freshness from the reported time

Rejected because different data sources may have different freshness policies.
Using an explicit expiration time makes the contract clear.

## Consequences

Future work can assume that:

- every major stakeholder has signed off on this document
- every threat contains the fields defined in this document
- repositories expose threats without revealing their source
- stale data is identified consistently
- local and remote data sources produce the same domain model
- tests should validate against this shared definition
