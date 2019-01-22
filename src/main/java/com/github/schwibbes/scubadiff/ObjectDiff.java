package com.github.schwibbes.scubadiff;

import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class ObjectDiff {

    Set<Class<?>> checkUsingEquals = Collections.unmodifiableSet(
	    Stream.of(String.class, Number.class, Boolean.class, UUID.class, Enum.class).collect(toSet()));

    public <T> void diff(T a, T b) {
	diff(null, -1, a, b);
    }

    private <T> void diff(Field field, int depth, T a, T b) {

	if (a == null || b == null) {
	    if (!Objects.equals(a, b))
		collect(depth, "A: " + a + ", B: " + b);
	} else if (a.getClass() != b.getClass()) {
	    throw new IllegalArgumentException(
		    "classes of input args must be equal, but got " + a.getClass() + " and " + b.getClass());
	} else if (a instanceof Collection) {
	    Set<?> onlyInA = setDiff(a, b);
	    Set<?> onlyInB = setDiff(b, a);
	    if (!(onlyInA.isEmpty() && onlyInB.isEmpty()))
		collect(depth, prefix(field) + "A:" + onlyInA + ", B: " + onlyInB);
	} else if (a instanceof Map) {
	    diff(field, depth, ((Map<?, ?>) a).entrySet(), ((Map<?, ?>) b).entrySet());
	} else if (shouldCheckUsingEquals(a, b)) {
	    if (!Objects.equals(a, b))
		collect(depth, convertToStringDiff(field, a, b));
	} else {
	    compareFields(a, b, ++depth);
	}
    }

    private <T> boolean shouldCheckUsingEquals(T a, T b) {
	return a instanceof Enum || Arrays.stream(a.getClass().getDeclaredMethods())
		.anyMatch(method -> method.getName().equals("equals"));
	// return checkUsingEquals.stream().anyMatch(clazz ->
	// clazz.isAssignableFrom(a.getClass()));
    }

    private <T> String convertToStringDiff(Field field, T a, T b) {
	return prefix(field) + str(a) + " <> " + str(b);
    }

    private String prefix(Field field) {
	return field == null ? "" : field.getDeclaringClass().getSimpleName() + "." + field.getName() + ": ";
    }

    private void collect(int depth, Object obj) {
	System.out.println(prefix(depth) + obj);
    }

    private String prefix(int depth) {
	return ">".repeat(Math.max(0, depth));
    }

    private <T> void compareFields(T a, T b, int depth) {
	compareFields(a, b, a.getClass(), b.getClass(), depth);
    }

    private <T> void compareFields(T a, T b, Class<?> classA, Class<?> classB, int depth) {
	for (Field fa : classA.getDeclaredFields()) {
	    if (skipField(fa))
		continue;

	    for (Field fb : classB.getDeclaredFields()) {
		if (fa.equals(fb)) {
		    fieldDiff(a, b, fa, fb, depth);
		}
	    }
	}

	Class<?> superA = classA.getSuperclass();
	Class<?> superB = classB.getSuperclass();

	if (superA.equals(superB) && !superA.equals(Object.class) && !superB.equals(Object.class)) {
	    compareFields(a, b, superA, superB, ++depth);
	}
    }

    private <T> void fieldDiff(T a, T b, Field fa, Field fb, int depth) {
	fa.setAccessible(true);
	fb.setAccessible(true);
	try {
	    diff(fa, depth, fa.get(a), fb.get(b));
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	}
    }

    private boolean skipField(Field field) {
	return field.getName().equals("this$0") || Modifier.isTransient(field.getModifiers());
    }

    private <T> Set<?> setDiff(T a, T b) {
	Set<?> onlyInA = new HashSet<>((Collection<?>) a);
	onlyInA.removeAll((Collection<?>) b);
	return onlyInA;
    }

    private String str(Object a) {
	return "\"" + a + "\"";
    }

}
