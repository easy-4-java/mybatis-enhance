package org.apache.ibatis.enhance.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class MyJacksonTypeHandlerTest {

    public static class Payload {
        public String name;
        public String optional;
        public LocalDate date;
    }

    @Test
    public void roundTripsJavaTimeAndOmitsNullProperties() {
        MyJacksonTypeHandler handler = new MyJacksonTypeHandler(Payload.class);
        Payload input = new Payload();
        input.name = "sample";
        input.date = LocalDate.of(2026, 11, 3);

        String json = handler.convert(input);
        Assert.assertTrue(json.contains("\"name\":\"sample\""));
        Assert.assertFalse(json.contains("optional"));

        Payload output = (Payload) handler.parse(json);
        Assert.assertEquals(input.name, output.name);
        Assert.assertEquals(input.date, output.date);
        Assert.assertNull(output.optional);
    }

    @Test
    public void rejectsMalformedJsonWithJacksonCause() {
        MyJacksonTypeHandler handler = new MyJacksonTypeHandler(Payload.class);

        RuntimeException failure = Assert.assertThrows(RuntimeException.class,
                () -> handler.parse("{broken"));
        Assert.assertTrue(failure.getCause() instanceof JsonProcessingException);
    }
}
