package org.graylog2.hipchatalarmcallback.callback;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.graylog2.plugin.alarms.Alarm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class GraylogMongoClient {
	
	private static Logger LOG = Logger.getLogger(GraylogMongoClient.class.getName());
	
	public String getStreamMessageId(Alarm alarm) {
		
		String streamId = null;
		try {
			Mongo mongo = new Mongo();
			DBCollection col = mongo.getDB("graylog2").getCollection("streams");
			String title = getStreamName(alarm);
			BasicDBObject query = new BasicDBObject("title", title);
			LOG.debug("query: "+query);
			DBObject message = col.findOne(query);
			if(message!=null) {
				ObjectId id = (ObjectId)message.get("_id");
				streamId = id.toString();
			} else {
				LOG.debug("query did not return any documents.");
			}
			
		} catch (UnknownHostException e) {
			LOG.error("unable to query mongodb", e);
			streamId = null;
		} catch (MongoException e) {
			LOG.error("unable to query mongodb", e);
			streamId = null;
		}
		
		return streamId;
	}
	
    private static String getStreamName(Alarm alarm) {

    	LOG.debug("alarm.topic: "+alarm.getTopic());
    	
    	String streamName = alarm.getTopic();
    	
    	String pattern = "\\[(.*?)\\]";
    	String[] matches = streamName.split(pattern); 
    	streamName = streamName.replace(matches[0], "");
    	streamName = streamName.substring(1, streamName.length()-1);
    	
    	return streamName;
    }
    
    
}
