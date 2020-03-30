package com.github.koettlitz.clom;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.koettlitz.clom.TypeAdapter.Default;
import com.github.koettlitz.opt.ArgumentParser;

/**
 * Annotation for declaring a field to represent a
 * plain command line argument.
 * The value of the command line argument can be set to the
 * annotated field by {@link CLOM}
 *
 * @see CLOption
 *
 * @author David Koettlitz
 * <br>Erstellt am 09.11.2018
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface CLArgument {
   /**
    * Provide the index of the argument.
    * All fields annotated with <code>CLArgument</code>
    * must have different indices and the indices must be
    * consecutive starting at 0 to provide a valid order.
    *
    * @return The index of the argument
    */
   int index();

   /**
    * The name of the argument to be used
    * when using generated messages, e.g.
    * {@link ArgumentParser#printUsage(java.io.PrintStream)}.
    * If no name is provided, the name of the attribute is used
    * as the name for the command line argument
    *
    * @return the name of the command line argument
    */
   String name() default "";

   /**
    * Declares whether this command line argument is
    * mandatory or optional.
    * By default command line arguments are mandatory.
    *
    * @return <code>true</code> if this <code>CLArgument</code>
    * is mandatory<br>
    * <code>false</code> if this <code>CLArgument</code>
    * is optional
    */
   boolean mandatory() default true;

   /**
    * Provides the descripton of the command line argument
    * as it is used by generated messages like e.g.
    * {@link ArgumentParser#printUsage(java.io.PrintStream)}.
    *
    * @return the description for the user of this <code>CLArgument</code>
    */
   String description() default "";

   /**
    * Provides an adapter to parse the provided argument String
    * into the value of the type of the field that represents this
    * command line argument.<br>
    * For any primitive type or String no <code>TypeAdapter</code>
    * has to be provided.
    *
    * @return the adapter to parse the provided argument String
    * to a value of the right type
    */
   Class<? extends TypeAdapter<?>> adapter() default Default.class;
}
