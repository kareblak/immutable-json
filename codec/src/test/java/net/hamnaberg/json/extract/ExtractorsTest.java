package net.hamnaberg.json.extract;

import javaslang.collection.List;
import javaslang.control.Option;
import net.hamnaberg.json.codec.DecodeResult;
import org.junit.Test;


import static net.hamnaberg.json.Json.*;
import static net.hamnaberg.json.extract.TypedField.*;
import static org.junit.Assert.*;

public class ExtractorsTest {

    JObject json = jObject(
            tuple("name", jString("Erlend")),
            tuple("age", jNumber(35)),
            tuple("address", jObject(
                tuple("street", jString("Ensjøveien 30 A")),
                tuple("city", jString("Oslo")),
                tuple("country", jString("Norway"))
            )),
            tuple("interests", jArray(jString("Programming"), jString("Books"), jString("podcasts")))
    );

    JObject json2 = jObject(
            tuple("name", jString("Erlend")),
            tuple("age", jNumber(35))
    );

    @Test
    public void extractPerson() {
        Extractor<Address> addressExtractor = Extractors.extract3(
                TString("street"),
                TString("city"),
                TString("country").map(Country::new),
                Address::new
        );

        TypedField<List<String>> interests = TJArray("interests").mapToOptionalList(JValue::asString);
        Extractor<Person> extractor = Extractors.extract4(
                TString("name"),
                TInt("age"),
                TJObject("address").extractTo(addressExtractor),
                interests,
                Person::new
        );
        DecodeResult<Person> personOpt = extractor.apply(json);
        assertTrue(personOpt.isOk());
        personOpt.forEach(person -> {
            assertEquals("Erlend", person.name);
            assertEquals(35, person.age);
            assertEquals("Ensjøveien 30 A", person.address.street);
            assertEquals(List.of("Programming", "Books", "podcasts"), person.interests);
        });
    }

    @Test
    public void extractPerson2() {
        Extractor<Address> addressExtractor = Extractors.extract3(
                TString("street"),
                TString("city"),
                TString("country").map(Country::new),
                Address::new
        );

        Extractor<Person2> extractor = Extractors.extract3(
                TString("name"),
                TInt("age"),
                TOptional("address", addressExtractor.decoder()),
                Person2::new
        );
        DecodeResult<Person2> personOpt = extractor.apply(json);
        DecodeResult<Person2> person2Opt = extractor.apply(json2);
        assertTrue(personOpt.isOk());
        assertTrue(person2Opt.isOk());
        personOpt.forEach(person -> {
            assertEquals("Erlend", person.name);
            assertEquals(35, person.age);
            assertTrue(person.address.isDefined());
            assertEquals("Ensjøveien 30 A", person.address.get().street);
        });
        person2Opt.forEach(person -> {
            assertEquals("Erlend", person.name);
            assertEquals(35, person.age);
            assertTrue(person.address.isEmpty());
        });
    }


    public static class Person {
        public final String name;
        public final int age;
        public final Address address;
        public final List<String> interests;

        public Person(String name, int age, Address address, List<String> interests) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.interests = interests;
        }


        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", address=" + address +
                    ", interests=" + interests +
                    '}';
        }
    }

    public static class Person2 {
        public final String name;
        public final int age;
        public final Option<Address> address;

        public Person2(String name, int age, Option<Address> address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }


        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", address=" + address +
                    '}';
        }
    }

    public static class Country {
        public final String name;

        public Country(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public static class Address {
        public final String street;
        public final String city;
        public final Country country;

        public Address(String street, String city, Country country) {
            this.street = street;
            this.city = city;
            this.country = country;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "street='" + street + '\'' +
                    ", city='" + city + '\'' +
                    ", country=" + country +
                    '}';
        }
    }
}

