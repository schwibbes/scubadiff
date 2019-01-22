package com.github.schwibbes.scubadiff;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runners.MethodSorters;

public class ObjectDiffTest {

    private ObjectDiff underTest = new ObjectDiff();

    @Test
    public void null_input_SHOULD_not_throw_NPE() throws Exception {
	underTest.diff(new Object(), null);
	underTest.diff(null, null);
	underTest.diff(null, new Object());
    }

    @Test
    public void different_class_as_input_SHOULD_throw_IAE() throws Exception {
	assertThrows(IllegalArgumentException.class, () -> underTest.diff(new Object(), new String()));
	assertThrows(IllegalArgumentException.class, () -> underTest.diff(new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void collection_input_SHOULD_not_throw_NPE() throws Exception {
	underTest.diff(new ArrayList<>(), null);
	underTest.diff(null, null);
	underTest.diff(null, new ArrayList<>());
	underTest.diff(Arrays.asList("a", "b"), Arrays.asList());
    }

    @Test
    public void enum_input_SHOULD_print_as_values() throws Exception {
	underTest.diff(MethodSorters.JVM, MethodSorters.NAME_ASCENDING);
    }

    @Test
    public void boolean_input_SHOULD_print_as_values() throws Exception {
	underTest.diff(true, false);
    }
    
    @Test
    public void exception_input_SHOULD_print_as_values() throws Exception {
	underTest.diff(new NullPointerException(), new NullPointerException("foo"));
    }

    @Test
    public void integer_input_SHOULD_print_as_values() throws Exception {
	underTest.diff(2, 3);
    }

    @Test
    public void string_input_SHOULD_print_as_values() throws Exception {
	underTest.diff("abc", "def");
    }

    @Test
    public void uuid_input_SHOULD_print_as_values() throws Exception {
	underTest.diff(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void nested_objects() throws Exception {
	Map<String, List<String>> a = new HashMap<>();
	a.put("a", Arrays.asList("x", "y", "z"));

	Map<String, List<Integer>> b = new HashMap<>();
	b.put("b", Arrays.asList(1, 2, 3));

	underTest.diff(a, b);
    }

    @Test
    public void simpleDiff() throws Exception {
	Foo a = new Foo();
	a.fooString = "a";
	a.fooList = Stream.of(1, 2).map(x -> "listItem" + x).collect(toList());
	a.fooMap = Stream.of(1, 2).collect(toMap(x -> "k" + x, x -> "v" + x));
	a.fooSet = Stream.of(1, 2).map(x -> "setItem" + x).collect(toSet());
	a.bar = new Bar("bar1");

	Foo b = new Foo();
	b.fooString = "b";
	b.fooList = Stream.of(2, 3).map(x -> "listItem" + x).collect(toList());
	b.fooMap = Stream.of(2, 3).collect(toMap(x -> "k" + x, x -> "v" + x));
	b.fooSet = Stream.of(2, 3).map(x -> "setItem" + x).collect(toSet());
	b.bar = new Bar("bar2");

	underTest.diff(a, b);
    }

    @Test
    public void subClassDiff() throws Exception {
	Foo a = new Foo2("1");
	a.fooString = "a";
	a.fooList = Stream.of(1, 2).map(x -> "listItem" + x).collect(toList());
	a.fooMap = Stream.of(1, 2).collect(toMap(x -> "k" + x, x -> "v" + x));
	a.fooSet = Stream.of(1, 2).map(x -> "setItem" + x).collect(toSet());
	a.bar = new Bar("bar1");

	Foo b = new Foo2("override");
	b.fooString = "b";
	b.fooList = Stream.of(2, 3).map(x -> "listItem" + x).collect(toList());
	b.fooMap = Stream.of(2, 3).collect(toMap(x -> "k" + x, x -> "v" + x));
	b.fooSet = Stream.of(2, 3).map(x -> "setItem" + x).collect(toSet());
	b.bar = new Bar("bar2");

	underTest.diff(a, b);
    }

    @SuppressWarnings("unused")
    private class Foo {
	String fooString;
	List<String> fooList;
	Map<String, String> fooMap;
	Set<String> fooSet;
	Bar bar;
    }

    @SuppressWarnings("unused")
    private class Foo2 extends Foo {
	private String fooString;

	public Foo2(String fooString) {
	    super();
	    this.fooString = fooString;
	}
    }

    @SuppressWarnings("unused")
    private class Bar {
	private final String bar;

	public Bar(String bar) {
	    super();
	    this.bar = bar;
	}

    }

}
