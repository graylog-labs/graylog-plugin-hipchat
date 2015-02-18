# Graylog HipChat alarm callback
[![Build Status](https://travis-ci.org/Graylog2/graylog2-alarmcallback-hipchat.svg)](https://travis-ci.org/Graylog2/graylog2-alarmcallback-hipchat)

An alarm callback plugin for integrating [HipChat](https://hipchat.com/) into [Graylog](https://www.graylog.org/).

## Build

This project is using Maven and requires Java 7 or higher.

You can build a plugin (JAR) with `mvn package`. 

DEB and RPM packages can be build with `mvn jdeb:jdeb` and `mvn rpm:rpm` respectively.
