/**
 * Copyright 2014 Markus Wuersch <markus@wuersch.net>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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

/**
 * A simple MongoDB client to get necessary information about a triggered Alarm. 
 * @author Markus Wuersch <markus@wuersch.net>
 */
public class GraylogMongoClient {
	
	private static Logger LOG = Logger.getLogger(GraylogMongoClient.class.getName());
	
	/**
	 * Gets the ID of the Graylog stream. 
	 * The stream is identified by the alarm's topic. 
	 * @param alarm Object of the alarm that was triggered.
	 * @return The stream id. 
	 */
	public String getStreamId(Alarm alarm) {
		
		String streamId = null;
		try {
			Mongo mongo = new Mongo();
			// the streams collection
			DBCollection col = mongo.getDB("graylog2").getCollection("streams");
			// get the name of the current stream
			String title = getStreamName(alarm);
			// find the stream by name (title = stream name)
			BasicDBObject query = new BasicDBObject("title", title);
			LOG.debug("query: "+query);
			DBObject message = col.findOne(query);
			if(message!=null) {
				// get the stream id
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
	
	/**
	 * Gets the stream name for the current alarm. 
	 * @param alarm Object of the alarm that was triggered.
	 * @return Name of the Stream.
	 */
    private static String getStreamName(Alarm alarm) {

    	LOG.debug("alarm.topic: "+alarm.getTopic());
    	
    	// the stream name is part of the topic
    	String streamName = alarm.getTopic();
    	
    	// pattern to find the stream name 
    	String pattern = "\\[(.*?)\\]";
    	String[] matches = streamName.split(pattern); 
    	streamName = streamName.replace(matches[0], "");
    	streamName = streamName.substring(1, streamName.length()-1);
    	
    	return streamName;
    }
    
    
}
