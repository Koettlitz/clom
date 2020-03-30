package com.github.koettlitz.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author David Koettlitz
 * <br>Erstellt am 10.11.2016
 */
public final class ReflectionUtils {
    private static final List<Primitive<?>> PRIMITIVES;

    static {
        PRIMITIVES = Arrays.asList(new Primitive<>(Byte.TYPE,
                        Byte.class,
                        Byte::parseByte,
                        (byte) 0),
                new Primitive<>(Short.TYPE,
                        Short.class,
                        Short::parseShort,
                        (short) 0),
                new Primitive<>(Integer.TYPE,
                        Integer.class,
                        Integer::parseInt,
                        0),
                new Primitive<>(Long.TYPE,
                        Long.class,
                        Long::parseLong,
                        0l),
                new Primitive<>(Float.TYPE,
                        Float.class,
                        Float::parseFloat,
                        0f),
                new Primitive<>(Double.TYPE,
                        Double.class,
                        Double::parseDouble,
                        0d),
                new Primitive<>(Character.TYPE,
                        Character.class,
                        s -> s.charAt(0),
                        ' '),
                new Primitive<>(Boolean.TYPE,
                        Boolean.class,
                        Boolean::parseBoolean,
                        false),
                new Primitive<>(String.class,
                        String.class,
                        s -> s,
                        ""));
    }

    private ReflectionUtils() {}

    /**
     * Converts the <code>object</code> into a String
     * by including the simple class name
     * and all of its fields including the inherited fields
     * with their values.<br> Note: Deep reflection is used here.
     *
     * @param object The object to be converted into a String
     *
     * @return The object as a String
     *
     * <code>object</code> contains a private field
     */
    public static String toString(Object object) {
        return toString(object, new StringBuilder(), 0).toString();
    }

    private static StringBuilder toString(Object object,
                                          StringBuilder builder,
                                          int tabCount) {
        if (object == null)
            return builder.append("null");

        Class<?> type = object.getClass();
        if (ReflectionUtils.isPrimitive(type)) {
            if (type.equals(String.class)) {
                return builder.append('\"')
                        .append(object.toString())
                        .append('\"');
            } else {
                return builder.append(object.toString());
            }
        } else if (type.isArray()) {
            return arrayToString(object, builder);
        } else if (type.isEnum()) {
            return builder.append(object.toString());
        }

        StringBuilder tabBuilder = new StringBuilder(tabCount * 2);
        for (int i = 0; i < tabCount; i++)
            tabBuilder.append("  ");

        builder.append(type.getSimpleName());
        Collection<Field> fields = ReflectionUtils.getAllFieldsOf(type);
        if (fields.isEmpty())
            return builder;

        builder.append(" {\n");
        Iterator<Field> iter = fields.iterator();
        while (iter.hasNext()) {
            Field f = iter.next();
            builder.append(tabBuilder.toString())
                    .append("  ")
                    .append(f.getName())
                    .append('=');
            try {
                f.setAccessible(true);
                toString(f.get(object), builder, tabCount + 1);
            } catch (SecurityException | IllegalAccessException e) {
                builder.append("<not accessible>");
            }

            if (iter.hasNext()) {
                builder.append(",\n");
            }
        }
        return builder.append('\n')
                .append(tabBuilder.toString())
                .append('}');
    }

    private static StringBuilder arrayToString(Object array,
                                               StringBuilder builder) {
        Class<?> type = array.getClass()
                .getComponentType();

        builder.append('[');
        int length = Array.getLength(array);

        Consumer<Object> appender;
        if (ReflectionUtils.isPrimitive(type)) {
            if (type.equals(String.class)) {
                appender = s -> builder.append('\"')
                        .append(s.toString())
                        .append('\"');
            } else {
                appender = p -> builder.append(p.toString());
            }
        } else {
            appender = o -> builder.append(toString(o));
        }
        for (int i = 0; i < length; i++) {
            try {
                appender.accept(Array.get(array, i));
            } catch (NullPointerException e) {
                // Nothing to do here
            }
            if (i < length - 1)
                builder.append(", ");
        }
        return builder.append(']');
    }

    /**
     * Finds out recursively whether the class of the <code>sub</code> parameter
     * is a subclass of the <code>sup</code> parameter class.
     *
     * @param sub the subclass
     * @param sup the superclass or superinterface
     *
     * @return <code>true</code> if <code>sub</code> is the subclass of <code>sup</code>.
     */
    public static boolean isSubclass(Class<?> sub, Class<?> sup) {
        if (sub.equals(sup))
            return true;

        for (Class<?> i : sub.getInterfaces()) {
            if (isSubclass(i, sup))
                return true;
        }

        if (sub.getSuperclass() == null)
            return false;

        return isSubclass(sub.getSuperclass(), sup);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeGetter(Object entity, Field field) throws NoSuchMethodException,
            SecurityException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            ClassCastException {
        String prefix = "get";
        Primitive<?> primitive = Primitive.of(field.getType());
        if (primitive != null)
            prefix = primitive.equals(Primitive.of(Boolean.TYPE)) ? "is" : "get";

        String name = prefix + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Method getter = getMethod(entity.getClass(), name);
        return (T) getter.invoke(entity);
    }

    public static void invokeSetter(Object target, Field field, Object value) throws NoSuchMethodException,
            SecurityException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException {
        String name = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Method setter = getMethod(target.getClass(), name, field.getType());
        setter.invoke(target, value);
    }

    public static void invokePrimitiveSetter(Object target,
                                             Field field,
                                             Object value,
                                             Class<?> primitiveClass) throws ReflectiveOperationException {
        String name = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Method setter = getMethod(target.getClass(), name, field.getType());
        setter.invoke(target, value);
    }

    public static <E extends Enum<E>> Enum<E> parseEnumValue(Class<Enum<E>> type, String value) throws IllegalArgumentException {
        if (!type.isEnum())
            throw new IllegalArgumentException("Type " + type.getName() + " is not an enum class.");

        for (Enum<E> e : type.getEnumConstants()) {
            if (e.toString().equals(value))
                return e;
        }
        throw new IllegalArgumentException("No enum value found for value " + value + " in enum " + type.getName());
    }

    public static Enum<?> parseUnknownEnumValue(Class<?> type, String value) throws IllegalArgumentException {
        if (!type.isEnum())
            throw new IllegalArgumentException("Type " + type.getName() + " is not an enum class.");

        for (Object e : type.getEnumConstants()) {
            if (e.toString().equals(value))
                return (Enum<?>) e;
        }
        throw new IllegalArgumentException("No enum value found for value " + value + " in enum " + type.getName());
    }

    /**
     * Get all of the fields including fields of superclasses of a type.
     *
     * @param type The type to get the fields of.
     *
     * @return A collection containing all of the fields of <code>type</code>
     */
    public static Collection<Field> getAllFieldsOf(Class<?> type) {
        Collection<Field> result = new LinkedList<>();
        result.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null)
            result.addAll(getAllFieldsOf(type.getSuperclass()));

        return result;
    }

    public static Field getField(Class<?> type, String fieldName) throws NoSuchFieldException, SecurityException {
        return getMetaObject(type, t -> t.getDeclaredField(fieldName));
    }

    public static Method getMethod(Class<?> type, String methodName, Class<?>... params) throws NoSuchMethodException {
        return getMetaObject(type, (t) -> t.getDeclaredMethod(methodName, params));
    }

    private static <T, E extends Exception> T getMetaObject(Class<?> type,
                                                            UnsafeFunction<Class<?>, ? extends T, E> metaObjectRetriever)
            throws E {
        try {
            return metaObjectRetriever.apply(type);
        } catch (Exception e) {
            if (type.getSuperclass() == null)
                throw e;

            return getMetaObject(type.getSuperclass(), metaObjectRetriever);
        }
    }

    /**
     * Parses the given String <code>value</code> to the <code>field</code>s
     * primitive type and assigns the value to the <code>field</code>.
     *
     * @param target the owner object of the field to assign the <code>value</code> to
     * @param field the field of <code>target</code> to assign the <code>value</code> to
     * @param value the String value representing a primitive to be parsed
     *              and assigned to the <code>field</code>
     *
     * @throws SecurityException if the field is not modifiable via reflection
     * @throws IllegalArgumentException if the value cannot be parsed to the fields type
     * or the fields type is a primitive, primitive wrapper nor String
     */
    public static void setPrimitiveValue(Object target,
                                         Field field,
                                         String value) throws SecurityException,
            IllegalArgumentException {
        if (!field.isAccessible())
            field.setAccessible(true);

        try {
            field.set(target, Primitive.of(field.getType()).parse(value));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not modify field " + field, e);
        }
    }

    /**
     * Checks whether the given type is a primitive type (or a String).
     *
     * @param type The type to be checked.
     *
     * @return <code>true</code> if the type is a primitve or a String.
     * <code>false</code> if it's a Type of Object except String.
     */
    public static boolean isPrimitive(Class<?> type) {
        return PRIMITIVES.stream()
                .anyMatch(p -> p.type.equals(type) || p.boxType.equals(type));
    }

    /**
     * Represents a class of a primitive type (and String) and provides some methods
     * for handling reflective operations with primitive types and values.
     *
     * @author David Koettlitz
     * <br>Erstellt am 16.12.2017
     */
    public static class Primitive<T> {
        private final Class<T> type;
        private final Class<T> boxType;
        private final Function<String, T> converter;
        private final T defaultValue;

        private Primitive(Class<T> type, Class<T> boxType, Function<String, T> converter, T defaultValue) {
            this.type = type;
            this.boxType = boxType;
            this.converter = converter;
            this.defaultValue = defaultValue;
        }

        /**
         * Provides an instance of a Primitive, that is matching the given primitive type
         * (or <code>String</code>).
         * An object wrapper class of the primitives will also go for a result.
         *
         * @param type The primitive type, primitive wrapper class of a primitive type or <code>String</code>
         * @param <P> The primitive type, primitive wrapper class of a primitive type or <code>String</code>
         *
         * @return The primitive instance matching the given primitive type,
         * primitive wrapper class or String class.
         *
         * @throws IllegalArgumentException if the given <code>type</code> is neither of
         * a primitive type primitive wrapper class or <code>String</code>
         *
         */
        public static <P> Primitive<P> of(Class<P> type) throws IllegalArgumentException {
            for (Primitive<?> p : PRIMITIVES) {
                if (p.type.equals(type) || p.boxType.equals(type)) {
                    @SuppressWarnings("unchecked")
                    Primitive<P> returnVal = (Primitive<P>) p;
                    return returnVal;
                }
            }
            throw new IllegalArgumentException(type + " is not a primitive, primitive wrapper, nor String.");
        }

        /**
         * Applies the given value to the field of the target object by invoking the fields setter method.
         *
         * @param target The target object whose field value should be set
         * @param field The field whose value should be set
         * @param value The value that should be set to the field
         *
         * @throws ReflectiveOperationException If no public setter for the field is declared
         * or the setter throws an exception
         * @throws NullPointerException If <code>target</code> or <code>field</code> is <code>null</code>
         */
        public void applyValue(Object target, Field field, String value) throws ReflectiveOperationException,
                NullPointerException{
            invokePrimitiveSetter(target, field, converter.apply(value), type);
        }

        public T parse(String string) throws IllegalArgumentException {
            try {
                return converter.apply(string);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Could not parse \"" + string + "\" to " + type.getName(), e);
            }
        }

        public Class<T> getType() {
            return type;
        }

        public T getDefaultValue() {
            return defaultValue;
        }
    }

}