package de.dk.clom;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.LinkedList;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface CLVarArgs {
    Class<? extends Collection> collectionType() default LinkedList.class;

    /**
     * Provides an adapter to parse the provided argument String
     * into the value of the type of the collection elements.<br>
     *
     * @return the adapter to parse the provided argument String
     * to a value of the right type
     */
    Class<? extends TypeAdapter<?>> adapter();
}
