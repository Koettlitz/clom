package de.dk.clom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.dk.opt.ArgumentParserBuilder;
import de.dk.opt.ex.ArgumentParseException;
import de.dk.opt.ex.MissingArgumentException;
import de.dk.opt.ex.MissingOptionValueException;

public class CLOMTest {
   private static final String ARG0 = "foo";
   private static final int ARG1 = 8;
   private static final char FOO_KEY = 'f';
   private static final char BAR_KEY = 'b';
   private static final long BAR_VALUE = 1024l;

   private static ArgumentModel parse(String... args) {
      try {
         return CLOM.parse(ArgumentModel.class, args);
      } catch (IllegalArgumentException | ArgumentParseException | InvalidTargetTypeException e) {
         fail(e);
         return null;
      }
   }

   @Test
   public void argument_is_present() {
      ArgumentModel result = parse(ARG0);
      assertEquals(ARG0, result.getArg0());
   }

   @Test
   public void non_given_args_are_absent() {
      ArgumentModel result = parse(ARG0);
      assertEquals(-1, result.getArg1());
      assertEquals(-1, result.getArg2());
   }

   @Test
   public void option_is_present() {
      ArgumentModel result = parse(ARG0, "-" + FOO_KEY);
      assertTrue(result.isFlag(), "Flag was given, but is not present in object.");
   }

   @Test
   public void non_given_options_are_absent() {
      ArgumentModel result = parse(ARG0);
      assertEquals(false, result.isFlag());
      assertEquals(-1, result.getBar());
   }

   @Test
   public void option_value_is_present() {
      ArgumentModel result = parse(ARG0, "-" + BAR_KEY, "" + BAR_VALUE);
      assertEquals(BAR_VALUE, result.getBar());
   }

   @Test
   public void args_and_options_are_present() {
      ArgumentModel result = parse(ARG0, "" + ARG1, "-" + FOO_KEY, "-" + BAR_KEY, "" + BAR_VALUE);
      assertEquals(ARG0, result.getArg0());
      assertEquals(ARG1, result.getArg1());
      assertEquals(true, result.isFlag());
      assertEquals(BAR_VALUE, result.getBar());
   }

   @Test
   public void missing_mandatory_arg_throws_exception() {
      assertThrows(MissingArgumentException.class,
                   () -> CLOM.parse(ArgumentModel.class, new ArgumentParserBuilder()));
   }

   @Test
   public void missing_option_value_throws_exception() {
      assertThrows(MissingOptionValueException.class, () -> CLOM.parse(ArgumentModel.class, ARG0, "-" + BAR_KEY));
   }

   @Test
   public void switch_option_not_boolean_throws_exception() {
      assertThrows(InvalidArgTypeException.class, () -> CLOM.parse(SwitchOptionNotBoolean.class,
                                                                   "-" + FOO_KEY));
   }

   @Test
   public void attribute_is_opt_and_arg_throws_exception() {
      assertThrows(InvalidArgTypeException.class, () -> CLOM.parse(ArgAndOptionAtOnce.class,
                                                                   "-" + FOO_KEY));
   }
}
