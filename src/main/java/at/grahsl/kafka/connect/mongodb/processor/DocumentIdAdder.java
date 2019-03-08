/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Original Work: Apache License, Version 2.0, Copyright 2017 Hans-Peter Grahsl.
 */

package at.grahsl.kafka.connect.mongodb.processor;

import at.grahsl.kafka.connect.mongodb.MongoDbSinkConnectorConfig;
import at.grahsl.kafka.connect.mongodb.converter.SinkDocument;
import at.grahsl.kafka.connect.mongodb.processor.id.strategy.IdStrategy;
import org.apache.kafka.connect.sink.SinkRecord;

import static at.grahsl.kafka.connect.mongodb.MongoDbSinkConnectorConfig.MONGODB_ID_FIELD;

public class DocumentIdAdder extends PostProcessor {

    private final IdStrategy idStrategy;

    public DocumentIdAdder(final MongoDbSinkConnectorConfig config, final String collection) {
        this(config, config.getIdStrategy(collection), collection);
    }

    public DocumentIdAdder(final MongoDbSinkConnectorConfig config, final IdStrategy idStrategy, final String collection) {
        super(config, collection);
        this.idStrategy = idStrategy;
    }

    @Override
    public void process(final SinkDocument doc, final SinkRecord orig) {
        doc.getValueDoc().ifPresent(vd -> vd.append(MONGODB_ID_FIELD, idStrategy.generateId(doc, orig)));
        getNext().ifPresent(pp -> pp.process(doc, orig));
    }

}
