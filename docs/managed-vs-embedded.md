---
title: Managed vs Embedded entities
description: Differentiation between managed and embedded entities
nav_order: 9
---
# Managed vs Embedded entities

Most entities in Policy engine can be defined as managed or embedded. 

Managed entities are defined by their ID and can be reused in multiple components through reference. 

Embedded entities are part of a single component and cannot be reused anywhere else, not even in the same component in more than one place (as they don't have ID).

### IManaged interface

If entity is managed, it must implement following fields:

| field       | type     | cardinality     | description                                                 | 
|-------------|----------|-----------------|-------------------------------------------------------------|
| id          | String   | mandatory       | Entity ID                                                   |
| version     | SemVer   | optional        | Entity version, [SemVer](https://semver.org/) format        |
| description | String   | optional        | Entity description                                          |
| labels      | String[] | optional        | Entity labels, used to search for specific managed entities |
