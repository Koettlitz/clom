package com.github.koettlitz.clom;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.koettlitz.clom.TypeAdapter.Default;
import com.github.koettlitz.opt.ArgumentParser;

/**
 * Annotates a field to represent a command line option.
 * The field value can then be set by {@link CLOM}.
 *
 * @see CLArgument
 *
 * @author David Koettlitz
 * <br>Erstellt am 09.11.2018
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface CLOption {
   /**
    * the key (letter) of the command line option
    * to be provided with a leading dash, e.g <code>-v</code>
    *
    * @return the key of the <code>CLOption</code>
    */
   char key();

   /**
    * the long option variant of this command line option
    * to be provided with two leading dashs, e.g. <code>--verbose</code><br>
    * For generated messages, e.g. {@link ArgumentParser#printUsage(java.io.PrintStream)}
    * the value provided by this parameter is used as the name for this command
    * line option. If no <code>longKey</code> is provided the name of the field is used
    * as the name for the <code>CLOption</code>
    *
    * @return the long option variant of this <code>CLOption</code> (without the dashes)
    */
   String longKey() default "";

   /**
    * Declares whether this command line option is just a switch/flag
    * (e.g. <code>-c</code>) or expects a value to be provided with it
    * (e.g. <code>-t 8</code>).
    *
    * @return <code>true</code> if this CLOption expects a value<br>
    * <code>false</code> if this CLOption is just a simple switch/flag
    */
   boolean expectsValue() default false;

   /**
    * Provides a description for the command line option
    * to be used for generated messages e.g.
    * {@link ArgumentParser#printUsage(java.io.PrintStream)}
    *
    * @return the description of this <code>CLOption</code>
    */
   String description() default "";

   /**
    * Provides an adapter to parse the provided string value from the
    * command line into a value of the right type.
    * A type adapter is only if {@link #expectsValue()} returns <code>true</code>.
    * For any primitive type or String no <code>TypeAdapter</code>
    * has to be provided.
    *
    * @return a type adapter to parse String values from the command line
    * into a value of the right type
    */
   Class<? extends TypeAdapter<?>> adapter() default Default.class;
}
