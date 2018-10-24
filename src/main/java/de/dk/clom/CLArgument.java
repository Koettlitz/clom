package de.dk.clom;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import de.dk.clom.TypeAdapter.Default;

@Retention(RUNTIME)
@Target(FIELD)
public @interface CLArgument {
   int index();
   String name() default "";
   boolean mandatory() default true;
   String description() default "";
   Class<? extends TypeAdapter<?>> adapter() default Default.class;
}
