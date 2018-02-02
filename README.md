HipChat Plugin for Graylog
==========================

[![Build Status](https://travis-ci.org/graylog-labs/graylog-plugin-hipchat.svg)](https://travis-ci.org/graylog-labs/graylog-plugin-hipchat)

An alarm callback plugin for integrating [HipChat](https://hipchat.com/) into [Graylog](https://www.graylog.org/).

**Required Graylog version:** 2.4.0 and later.

* Please use version 1.2.0 of this plugin if you are still running Graylog 1.x.
* Please use version 1.3.0 of this plugin if you are still running Graylog 2.0.x, 2.1.x, 2.2.x, or 2.3.x.

## Installation

[Download the plugin](https://github.com/graylog-labs/graylog-plugin-hipchat/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` directory relative from your Graylog installation path by default
and can be configured in the `graylog.conf` file.

Restart Graylog and you are done.

## Usage

Custom templates can be defined with the same [JMTE syntax](https://cdn.rawgit.com/DJCordhose/jmte/master/doc/index.html) used in [the email templates](http://docs.graylog.org/en/2.0/pages/streams.html#email-alert-callback), as long as they only work on the [HTML subset supported by the HipChat API](https://developer.atlassian.com/hipchat/guide/sending-messages).

For example the following template includes the custom field named `myField` in the HipChat message:

```html
${if stream_url}<a href="${stream_url}">${end}
<strong>Alert for ${stream.title}</strong>
${if stream_url}
</a>
${end}
<i>(${check_result.triggeredCondition})</i>
<br/>
<i>${check_result.resultDescription}, triggered at ${check_result.triggeredAt}</i>
<br/>
${if backlog}Last messages accounting for this alert:<br/>
<table align="left">
<tr><th>My Field</th><th>Details</th></tr>
${foreach backlog message}<br/>
<tr>
    <td><b>${message.fields.myField}</b></td>
    <td><code>${message.source}, ${message.id}</code></td>
</tr>
</table>
${end}
${else}
<i>(No messages to display.)</i>
${end}
```

If no custom template has been configured, the default email template is used.

## Build

This project is using Maven and requires Java 8 or higher.

You can build a plugin (JAR) with `mvn package`.

DEB and RPM packages can be build with `mvn jdeb:jdeb` and `mvn rpm:rpm` respectively.

## Plugin Release

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. TravisCI will build the release artifacts and upload to GitHub automatically.
