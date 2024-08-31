---
title: Policy Catalog
description: Introduction to PolicyCatalog
nav_order: 6
---
# Policy Catalog
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

PolicyCatalog is a container for all Policy entities: Policies, PolicyConditions, PolicyVariables, PolicyVariableResolvers and PolicyActions.

It is responsible for managing and providing access to the various components that make up a policy catalog. It performs validation checks on the catalog, ensuring that there are no circular references or missing references.

The catalog can be used to retrieve specific policy conditions, variables, resolvers, actions, and policies by their unique identifiers and versions. If entities are searched only by id, PolicyCatalog will return latest version of an entity sorted by version field in descending manner. It also provides methods to search for policy conditions and policies by their labels.

Every PolicyCatalog contains following properties:

| field                   | type                       | cardinality | description                                                                                                                                                      | 
|-------------------------|----------------------------|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id                      | String                     | mandatory   | Unique ID of a PolicyCatalog.                                                                                                                                    |
| version                 | String                     | optional    | The version of the policy catalog, using custom CalVer versioning scheme with format "YYYY-MM-DD[-R]" where "-R" is the optional revision number (positive int). |
| withDefaultPolicies     | Boolean                    | optional    | A boolean indicating whether to include [default policies](policy.md#policydefault) in the catalog. Default value is **false**.                                  |
| withDefaultConditions   | Boolean                    | optional    | A boolean indicating whether to include [default policy conditions](policy-condition.md#policyconditiondefault) in the catalog. Default value is **false**.      |
| policies                | `IPolicy[]`                | optional*   | The list of [Policies](policy.md) in the catalog. Optional if policyCondition list is populated, otherwise it is mandatory.                                      |
| policyConditions        | `IPolicyCondition[]`       | optional*   | The list of [PolicyConditions](policy-condition.md) in the catalog. Optional if policies list is populated, otherwise it is mandatory.                           |
| policyVariables         | `IPolicyVariable[]`        | optional    | The list of [PolicyVariables](policy-variable.md) in the catalog.                                                                                                |
| policyVariableResolvers | `PolicyVariableResolver[]` | optional    | The list of [PolicyVariableResolvers](policy-variable.md#policyvariableresolver) in the catalog.                                                                 |
| policyActions           | `IPolicyAction[]`          | optional    | The list of [PolicyActions](policy-action.md) in the catalog.                                                                                                    |

### Empty PolicyCatalog

Special entity that is used to represent an empty PolicyCatalog. It contains only default policies and default policy conditions. Id of such catalog is `emtpy-policy-catalog`. If [PolicyEngine](policy-engine.md) is created without defined PolicyCatalog, it will use this catalog as the default.

### Example

Here is an example of a PolicyCatalog that is used in [AccessControl](examples/access-control.md) example:

```json
{
    "id": "access-control",
    "version": "2024-02-17",
    "policies": [
        {
            "id": "userAccess",
            "description": "Allows access to regular user if it is working day and working hour",
            "targetEffect": "permit",
            "condition": {
                "id": "regularUserAccess",
                "refType": "PolicyConditionRef"
            },
            "strictTargetEffect": true
        },
        {
            "id": "adminAccess",
            "description": "Allows access to admin user",
            "targetEffect": "permit",
            "condition": {
                "id": "isAdmin",
                "refType": "PolicyConditionRef"
            },
            "strictTargetEffect": true
        },
        {
            "id": "checkAccess",
            "description": "Checks if user has access",
            "actions": [
                {
                    "executionMode": [
                        "onDeny"
                    ],
                    "action": {
                        "id": "setForbiddenMessage",
                        "refType": "PolicyActionRef"
                    }
                },
                {
                    "executionMode": [
                        "onPermit"
                    ],
                    "action": {
                        "id": "setAllowedMessage",
                        "refType": "PolicyActionRef"
                    }
                }
            ],
            "policyCombinationLogic": "denyUnlessPermit",
            "policies": [
                {
                    "policy": {
                        "id": "userAccess",
                        "refType": "PolicyRef"
                    }
                },
                {
                    "priority": 10,
                    "policy": {
                        "id": "adminAccess",
                        "refType": "PolicyRef"
                    }
                }
            ]
        }
    ],
    "policyConditions": [
        {
            "id": "isAdmin",
            "description": "Checks if provided role is equal to 'admin'",
            "operation": "Equals",
            "args": [
                {
                    "type": "string",
                    "value": "admin"
                },
                {
                    "id": "role",
                    "refType": "PolicyVariableRef"
                }
            ],
            "stringIgnoreCase": true
        },
        {
            "id": "isUser",
            "description": "Checks if provided role is equal to 'user'",
            "operation": "Equals",
            "args": [
                {
                    "type": "string",
                    "value": "user"
                },
                {
                    "id": "role",
                    "refType": "PolicyVariableRef"
                }
            ],
            "stringIgnoreCase": true
        },
        {
            "id": "isWorkingDay",
            "description": "Checks if it is working day currently (Mon-Fri)",
            "operation": "LessThanEqual",
            "args": [
                {
                    "id": "dayOfWeek",
                    "refType": "PolicyVariableRef"
                },
                {
                    "type": "int",
                    "value": 5
                }
            ]
        },
        {
            "id": "isWorkingHour",
            "description": "Checks if it is working hour currently (09:00-17:00)",
            "conditionCombinationLogic": "allOf",
            "conditions": [
                {
                    "operation": "GreaterThanEqual",
                    "args": [
                        {
                            "id": "currentTime",
                            "refType": "PolicyVariableRef"
                        },
                        {
                            "type": "string",
                            "format": "time",
                            "timeFormat": "HH:mm",
                            "value": "09:00"
                        }
                    ]
                },
                {
                    "operation": "LessThanEqual",
                    "args": [
                        {
                            "id": "currentTime",
                            "refType": "PolicyVariableRef"
                        },
                        {
                            "type": "string",
                            "format": "time",
                            "timeFormat": "HH:mm",
                            "value": "17:00"
                        }
                    ]
                }
            ]
        },
        {
            "id": "regularUserAccess",
            "description": "Checks if user has role 'user' and if it is a working day",
            "conditionCombinationLogic": "allOf",
            "conditions": [
                {
                    "id": "isUser",
                    "refType": "PolicyConditionRef"
                },
                {
                    "id": "isWorkingDay",
                    "refType": "PolicyConditionRef"
                },
                {
                    "id": "isWorkingHour",
                    "refType": "PolicyConditionRef"
                }
            ]
        }
    ],
    "policyVariables": [
        {
            "id": "role",
            "description": "Provided role",
            "resolvers": [
                {
                    "id": "roleResolver",
                    "refType": "PolicyVariableResolverRef"
                }
            ],
            "type": "string"
        },
        {
            "id": "currentTime",
            "description": "Current time",
            "resolvers": [
                {
                    "source": "environment",
                    "key": "localTime"
                }
            ],
            "type": "string",
            "format": "time"
        },
        {
            "id": "dayOfWeek",
            "description": "Current day of week",
            "resolvers": [
                {
                    "source": "environment",
                    "key": "dayOfWeek"
                }
            ],
            "type": "int"
        }
    ],
    "policyVariableResolvers": [
        {
            "id": "roleResolver",
            "description": "Extracts role from subject store",
            "source": "subject",
            "key": "role"
        }
    ],
    "policyActions": [
        {
            "id": "setForbiddenMessage",
            "description": "Sets message for user for which access has been denied",
            "key": "message",
            "value": {
                "resolvers": [
                    {
                        "source": "subject",
                        "path": "\"Access has been denied for \" + .username",
                        "engine": "JQ"
                    }
                ],
                "type": "string"
            },
            "type": "save"
        },
        {
            "id": "setAllowedMessage",
            "description": "Sets message for user who has been granted access",
            "key": "message",
            "value": {
                "resolvers": [
                    {
                        "source": "subject",
                        "path": "\"Access has been granted for \" + .username",
                        "engine": "JQ"
                    }
                ],
                "type": "string"
            },
            "type": "save"
        }
    ]
}
```



