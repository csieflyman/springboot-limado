package util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * https://github.com/EsotericSoftware/kryo
 * @author csieflyman
 */
@Slf4j
public class KryoRedisSerializer implements RedisSerializer {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final int START_TYPE_ID = 9; //Class IDs -1 and -2 are reserved. Class IDs 0-8 are used by default for primitive types and String

    private final KryoPool KRYO_POOL;

    public KryoRedisSerializer(Class[] classes) {
        this(IntStream.range(START_TYPE_ID, classes.length + START_TYPE_ID).boxed().collect(Collectors.toMap(i -> classes[i - START_TYPE_ID], i -> i)));
    }

    public KryoRedisSerializer(Map<Class, Integer> mappings) {
        KryoFactory KRYO_FACTORY = () -> {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.setWarnUnregisteredClasses(true);
            if(mappings == null || mappings.isEmpty()) {
                kryo.setRegistrationRequired(false);
            }
            else {
                mappings.forEach((clazz, id) -> {
                    if(id == null)
                        kryo.register(clazz);
                    else
                        kryo.register(clazz, id);
                });
            }
            return kryo;
        };
        KRYO_POOL = new KryoPool.Builder(KRYO_FACTORY).softReferences().build();
    }

    @Override
    public byte[] serialize(Object t) throws SerializationException {
        if (t == null) {
            return EMPTY_BYTE_ARRAY;
        }

        Kryo kryo = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); Output output = new Output(baos)) {
            kryo = KRYO_POOL.borrow();
            kryo.writeClassAndObject(output, t);
            output.flush();
            return baos.toByteArray();
        } catch (Throwable e) {
            log.error("fail to serialize: " + t.getClass().getName(), e);
        }
        finally {
            if(kryo != null) {
                KRYO_POOL.release(kryo);
            }
        }
        return EMPTY_BYTE_ARRAY;
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        Kryo kryo = null;
        try (Input input = new Input(bytes)) {
            kryo = KRYO_POOL.borrow();
            return kryo.readClassAndObject(input);
        } catch (Throwable e) {
            log.error("fail to deserialize", e);
        }
        finally {
            if(kryo != null) {
                KRYO_POOL.release(kryo);
            }
        }
        return null;
    }
}