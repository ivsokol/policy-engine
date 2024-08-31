---
title: Examples
description: PolicyEngine real-world examples
nav_order: 10
---
# Examples

Here are some examples of PolicyEngine usage.

## Access Control

In this [example](access-control.md), we will create a simple access control policy based on following demands:

* if user role is "user", then allow access only on working hours (9:00 - 17:00) at working days (Monday - Friday)
* if user role is "admin", then allow access on any day and time
* after successfull Policy execution, provide following message:
  * "Access has been granted for {username}" if user has been granted access
  * "Access has been denied for {username}" if user has been denied access

More examples coming soon ...
{: .label .label-yellow }
