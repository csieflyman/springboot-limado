package com.csieflyman.limado.util.converter.json;

import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.util.converter.ConversionException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.hibernate.collection.internal.PersistentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author csieflyman
 */
class PartySerializer extends JsonSerializer<Party> {

    private static final Logger logger = LoggerFactory.getLogger(PartySerializer.class);

    private static final ThreadLocal parentDepthLocal = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return 0;
        }
    };
    private static final ThreadLocal childrenDepthLocal = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return 0;
        }
    };

    private static final int MAX_DEPTH = 1;

    @Override
    public void serialize(Party party, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int parentDepth = (int) parentDepthLocal.get();
        int childrenDepth = (int) childrenDepthLocal.get();
        gen.writeStartObject();

        if (party == null) {
            provider.getDefaultNullValueSerializer().serialize(null, gen, provider);
        } else {
            Class cls = party.getClass();
            while (!cls.equals(Object.class)) {
                Field[] fields = cls.getDeclaredFields();
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);
                        String name = field.getName();
                        Object value = field.get(party);

                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }

                        gen.writeFieldName(name);

                        if (name.equals(Party.RELATION_PARENT)) {
                            // we want serialize empty array instead of null
                            if (childrenDepth > 0 || value == null || (value.getClass() == PersistentSet.class && !((PersistentSet) value).wasInitialized())) {
                                gen.writeStartArray();
                                gen.writeEndArray();
                            } else {
                                if (parentDepth < MAX_DEPTH) {
                                    parentDepth++;
                                    parentDepthLocal.set(parentDepth);
                                    JsonSerializer serializer = provider.findValueSerializer(value.getClass());
                                    serializer.serialize(value, gen, provider);
                                    parentDepth--;
                                    parentDepthLocal.set(parentDepth);
                                } else {
                                    gen.writeStartArray();
                                    gen.writeEndArray();
                                }
                            }
                        } else if (name.equals(Party.RELATION_CHILDREN)) {
                            if (parentDepth > 0 || value == null || (value.getClass() == PersistentSet.class && !((PersistentSet) value).wasInitialized())) {
                                gen.writeStartArray();
                                gen.writeEndArray();
                            } else {
                                if (childrenDepth < MAX_DEPTH) {
                                    childrenDepth++;
                                    childrenDepthLocal.set(childrenDepth);
                                    JsonSerializer serializer = provider.findValueSerializer(value.getClass());
                                    serializer.serialize(value, gen, provider);
                                    childrenDepth--;
                                    childrenDepthLocal.set(childrenDepth);
                                } else {
                                    gen.writeStartArray();
                                    gen.writeEndArray();
                                }
                            }
                        } else {
                            if (value == null) {
                                provider.getDefaultNullValueSerializer().serialize(null, gen, provider);
                            } else {
                                provider.defaultSerializeValue(value, gen);
                            }
                        }
                    } catch (Throwable e) {
                        parentDepthLocal.remove();
                        childrenDepthLocal.remove();
                        logger.error("serialize" + party + " failure", e);
                        throw new ConversionException("serialize" + party + " failure", e);
                    }
                }
                cls = cls.getSuperclass();
            }
        }

        gen.writeEndObject();
        if (parentDepth == 0) {
            parentDepthLocal.remove();
        }
        if (childrenDepth == 0) {
            childrenDepthLocal.remove();
        }
    }

    @Override
    public void serializeWithType(Party party, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        serialize(party, gen, serializers);
    }
}