
package br.com.conductor.heimdall.core.appender;

/*-
 * =========================LICENSE_START==================================
 * heimdall-core
 * ========================================================================
 * Copyright (C) 2018 Conductor Tecnologia SA
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==========================LICENSE_END===================================
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <h1>MongoDB Appender</h1><br/>
 * 
 * This class provides a appender service to a MongoDB database.
 * 
 * @author Marcos Filho
 * @see AppenderBase
 *
 */
@Slf4j
@NoArgsConstructor
public class MongoDBAppender extends AppenderBase<LoggingEvent> {

     private MongoClient mongoClient;

     private MongoCollection<Document> collection;
     
     @Setter @Getter
     private String url;
     @Setter @Getter
     private Long port;
     @Setter @Getter
     private String dataBase;
     @Setter @Getter
     private String collectionName;
     
     
     public MongoDBAppender(String url, Long port, String dataBase, String collectionName) {
          this.url = url;
          this.port = port;
          this.dataBase = dataBase;
          this.collectionName = collectionName;
     }

     @Override
     public void start() {
          log.info("Initializing Mongodb Appender");
          MongoClientOptions options = new MongoClientOptions.Builder().socketKeepAlive(true).build();
          ServerAddress address = new ServerAddress(this.url, this.port.intValue());
          this.mongoClient = new MongoClient(address, options);
          MongoDatabase database = this.mongoClient.getDatabase(this.dataBase);
          this.collection = database.getCollection(this.collectionName);
          log.info("Starting connection with url: {} - port: {}", this.url, this.port);
          log.info("Database used: {} - Collection: {}", this.dataBase, this.collectionName);
          super.start();
     }
     
     @Override
     public void stop() {
          log.info("Closing mongodb appender");
          this.mongoClient.close();
          super.stop();
     }

     @Override
     protected void append(LoggingEvent e) {
          Map<String, Object> objLog = new HashMap<>();
          objLog.put("ts", new Date(e.getTimeStamp()));
          objLog.put("msg", e.getFormattedMessage());
          objLog.put("level", e.getLevel().toString());
          objLog.put("logger", e.getLoggerName());
          objLog.put("thread", e.getThreadName());

          if (e.hasCallerData()) {
               StackTraceElement st = e.getCallerData()[0];
               String callerData = String.format("%s.%s:%d", st.getClassName(), st.getMethodName(), st.getLineNumber());
               objLog.put("caller", callerData);
          }
          Map<String, String> mdc = e.getMDCPropertyMap();
          if (mdc != null && !mdc.isEmpty()) {
               objLog.put("mdc", new BasicDBObject(mdc));
          }
//          BasicDBObject content = new BasicDBObject(objLog);
          collection.insertOne(new Document(objLog));
     }

}
