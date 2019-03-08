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

package at.grahsl.kafka.connect.mongodb.converter;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@RunWith(JUnitPlatform.class)
class SinkConverterTest {
    private static BsonDocument expectedBsonDoc;
    private static Map<Object, Schema> combinations;
    private final SinkConverter sinkConverter = new SinkConverter();

    @BeforeAll
    static void initializeTestData() {
        String jsonString1 = "{\"myField\":\"some text\"}";
        Schema objSchema1 = SchemaBuilder.struct().field("myField", Schema.STRING_SCHEMA);
        Struct objStruct1 = new Struct(objSchema1).put("myField", "some text");

        Map<String, Object> objMap1 = new LinkedHashMap<>();
        objMap1.put("myField", "some text");

        expectedBsonDoc = new BsonDocument("myField", new BsonString("some text"));

        combinations = new HashMap<>();
        combinations.put(jsonString1, null);
        combinations.put(objStruct1, objSchema1);
        combinations.put(objMap1, null);
    }

    @TestFactory
    @DisplayName("test different combinations for sink record conversions")
    List<DynamicTest> testDifferentOptionsForSinkRecordConversion() {
        List<DynamicTest> tests = new ArrayList<>();

        for (Map.Entry<Object, Schema> entry : combinations.entrySet()) {
            tests.add(dynamicTest("key only SinkRecord conversion for type " + entry.getKey().getClass().getName()
                    + " with data -> " + entry.getKey(), () -> {
                SinkDocument converted = sinkConverter.convert(
                        new SinkRecord("topic", 1, entry.getValue(), entry.getKey(), null, null, 0L));
                assertAll("checks on conversion results",
                        () -> assertNotNull(converted),
                        () -> assertEquals(expectedBsonDoc, converted.getKeyDoc().get()),
                        () -> assertEquals(Optional.empty(), converted.getValueDoc())
                );
            }));

            tests.add(dynamicTest("value only SinkRecord conversion for type " + entry.getKey().getClass().getName()
                    + " with data -> " + entry.getKey(), () -> {
                SinkDocument converted = sinkConverter.convert(
                        new SinkRecord("topic", 1, null, null, entry.getValue(), entry.getKey(), 0L));
                assertAll("checks on conversion results",
                        () -> assertNotNull(converted),
                        () -> assertEquals(Optional.empty(), converted.getKeyDoc()),
                        () -> assertEquals(expectedBsonDoc, converted.getValueDoc().get())
                );
            }));

            tests.add(dynamicTest("key + value SinkRecord conversion for type " + entry.getKey().getClass().getName()
                    + " with data -> " + entry.getKey(), () -> {
                SinkDocument converted = sinkConverter.convert(
                        new SinkRecord("topic", 1, entry.getValue(), entry.getKey(), entry.getValue(), entry.getKey(), 0L));
                assertAll("checks on conversion results",
                        () -> assertNotNull(converted),
                        () -> assertEquals(expectedBsonDoc, converted.getKeyDoc().get()),
                        () -> assertEquals(expectedBsonDoc, converted.getValueDoc().get())
                );
            }));
        }
        return tests;
    }

    @Test
    @DisplayName("test empty sink record conversion")
    void testEmptySinkRecordConversion() {
        SinkDocument converted = sinkConverter.convert(
                new SinkRecord("topic", 1, null, null, null, null, 0L));

        assertAll("checks on conversion result",
                () -> assertNotNull(converted),
                () -> assertEquals(Optional.empty(), converted.getKeyDoc()),
                () -> assertEquals(Optional.empty(), converted.getValueDoc())
        );
    }

    @Test
    @DisplayName("test invalid sink record conversion")
    void testInvalidSinkRecordConversion() {

        assertAll("checks on conversion result",
                () -> assertThrows(DataException.class, () -> sinkConverter.convert(
                        new SinkRecord("topic", 1, null, new Object(), null, null, 0L)
                )),
                () -> assertThrows(DataException.class, () -> sinkConverter.convert(
                        new SinkRecord("topic", 1, null, null, null, new Object(), 0L)
                )),
                () -> assertThrows(DataException.class, () -> sinkConverter.convert(
                        new SinkRecord("topic", 1, null, new Object(), null, new Object(), 0L)
                ))
        );
    }
}
