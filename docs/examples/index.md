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


## Pet example

In this [example](pet-example.md), we want to create a simple policy to check if a user who is trying to do an action (read or update) on Pet object is allowed to do that.

Example will cover following variations:

* only pet owner can update pet information
* only pet owner can update pet information - variation where data is provided as Pet data class
* only pet owner can update pet information - variation where additional message is provided in the result
* everyone can read pet information, only owner can update pet information - realized through composite conditions
* everyone can read pet information, only owner can update pet information - realized through policy sets
* only pet owner can update pet information - variation where two kind of pet objects are allowed


More examples coming soon ...
{: .label .label-yellow }


