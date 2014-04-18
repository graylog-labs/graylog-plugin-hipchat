graylog2-plugin-alarmcallback-hipchat
=====================================

Alarm callback that is writing to HipChat chatrooms. 

This version composes more detailed HipChat messages. The message details are
pulled from MongoDB and elasticsearch: 
1. Finds the stream id based on the Alarm.topic attribute. 
2. Uses the found stream id to get the last message in a stream from elasticsearch.

The HipChat messages contain the following details:
1. The Alarm content
2. The GrayLog2 short message
3. A deep link to the message on your GrayLog2 instance

Documentation: http://support.torch.sh/help/kb/plugins/hipchat-alarm-callback

Available from the [Graylog2 plugin directory](http://www.graylog2.org/plugins).
