openhab-http-persistence
=======================

Persistence engine using URL calls.

### Introduction

While OpenHAB supports a wide range of external tools, it can never support them all. A lot of those tools however expose API's.  

### Installing

Add the org.openhab.persistence.http<version>.jar to the addons folder of an openHAB runtime installation and restart.

#### Configuration

The following configuration items can be set in openhab.cfg:

    httppersistence:pattern=<some pattern>
This pattern is used to construct an URL to call. It uses the [java formatter syntax](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax). The URL is constructed using three parameters:
1. Name (or alias) of the item
2. Its value
3. Date
This item is required.

    httppersistence:forcenumeric=true
Some tools do not support String and only accept numeric (decimal) values. When setting this to ``true`` all values will be changed into a number. If its unable to do that, the value is ignored. This item is optional and defaults to ``false``.  
