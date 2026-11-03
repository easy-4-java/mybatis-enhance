package org.apache.ibatis.enhance.typehandler;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JacksonTypeHandlerTest {

    public static class Person {
        public String name;
        public int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void configuredMapperShouldRoundTripAndOmitNullProperties() {
        MyJacksonTypeHandler handler = new MyJacksonTypeHandler(Map.class);
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("name", "Ada");
        input.put("unused", null);

        String json = handler.convert(input);
        Assert.assertEquals("{\"name\":\"Ada\"}", json);
        Assert.assertEquals("Ada", ((Map<?, ?>) handler.parse(json)).get("name"));
    }

    @Test
    public void configuredMapperShouldReportMalformedJson() {
        MyJacksonTypeHandler handler = new MyJacksonTypeHandler(Map.class);
        RuntimeException error = Assert.assertThrows(RuntimeException.class, () -> handler.parse("{"));
        Assert.assertTrue(error.getMessage().contains("Jackson deserialization failed"));
        Assert.assertNotNull(error.getCause());
    }

    @Test
    public void listHandlerShouldRoundTripTypedElementsAndPreserveEmptyBehavior() {
        ListTypeHandler<Person> handler = personListHandler();
        List<Person> input = Arrays.asList(new Person("Ada", 37), new Person("Grace", 85));

        String json = handler.convert(input);
        List<Person> parsed = handler.parse(json);
        Assert.assertEquals(2, parsed.size());
        Assert.assertEquals("Ada", parsed.get(0).name);
        Assert.assertEquals(37, parsed.get(0).age);
        Assert.assertEquals("Grace", parsed.get(1).name);
        Assert.assertNull(handler.convert(new ArrayList<Person>()));
    }

    @Test
    public void listHandlerShouldReportMalformedJson() {
        ListTypeHandler<Person> handler = personListHandler();
        RuntimeException error = Assert.assertThrows(RuntimeException.class, () -> handler.parse("["));
        Assert.assertTrue(error.getMessage().contains("Failed to deserialize list column"));
        Assert.assertNotNull(error.getCause());
    }

    @Test
    public void pojoHandlerShouldRoundTripSingleObjectAndArray() {
        JsonStringTypeHandler<Person> single = new JsonStringTypeHandler<Person>() { };
        Person parsed = single.parse(single.convert(new Person("Ada", 37)));
        Assert.assertEquals("Ada", parsed.name);
        Assert.assertEquals(37, parsed.age);

        JsonStringTypeHandler<Person[]> array = new JsonStringTypeHandler<Person[]>() { };
        Person[] people = array.parse(array.convert(new Person[] {new Person("Grace", 85)}));
        Assert.assertEquals(1, people.length);
        Assert.assertEquals("Grace", people[0].name);
    }

    @Test
    public void pojoHandlerShouldReportMalformedJson() {
        JsonStringTypeHandler<Person> handler = new JsonStringTypeHandler<Person>() { };
        RuntimeException error = Assert.assertThrows(RuntimeException.class, () -> handler.parse("{"));
        Assert.assertNotNull(error.getCause());
    }

    @Test
    public void mapperShouldWrapSerializationFailure() {
        MyJacksonTypeHandler handler = new MyJacksonTypeHandler(Map.class);
        Map<String, Object> cyclic = new LinkedHashMap<>();
        cyclic.put("self", cyclic);
        RuntimeException error = Assert.assertThrows(RuntimeException.class, () -> handler.convert(cyclic));
        Assert.assertTrue(error.getMessage().contains("Jackson serialization failed"));
        Assert.assertNotNull(error.getCause());
    }

    private static ListTypeHandler<Person> personListHandler() {
        return new ListTypeHandler<Person>() {
            @Override
            protected com.fasterxml.jackson.core.type.TypeReference<List<Person>> elementType() {
                return new com.fasterxml.jackson.core.type.TypeReference<List<Person>>() { };
            }
        };
    }
}
