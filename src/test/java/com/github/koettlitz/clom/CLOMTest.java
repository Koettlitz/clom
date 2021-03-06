package com.github.koettlitz.clom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.github.koettlitz.opt.ArgumentParserBuilder;
import com.github.koettlitz.opt.ex.ArgumentParseException;
import com.github.koettlitz.opt.ex.MissingArgumentException;
import com.github.koettlitz.opt.ex.MissingOptionValueException;

public class CLOMTest {
   private static final String ARG0 = "foo";
   private static final byte ARG1 = 0xF;
   private static final short ARG2 = 800;
   private static final int ARG3 = 80000;
   private static final long ARG4 = 8000000;
   private static final float ARG5 = 800.800f;
   private static final double ARG6 = 8000.8000;
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
      ArgumentModel result = parse(ARG0,
                                   "" + ARG1,
                                   "" + ARG2,
                                   "" + ARG3,
                                   "" + ARG4,
                                   "" + ARG5,
                                   "" + ARG6,
                                   "-" + FOO_KEY,
                                   "-" + BAR_KEY,
                                   "" + BAR_VALUE);

      assertEquals(ARG0, result.getArg0());
      assertEquals(ARG1, result.getArg1());
      assertEquals(ARG2, result.getArg2());
      assertEquals(ARG3, result.getArg3());
      assertEquals(ARG4, result.getArg4());
      assertEquals(ARG5, result.getArg5());
      assertEquals(ARG6, result.getArg6());
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

   @Test
   public void non_boolean_option_not_expecting_a_value_throws_exception() {
      assertThrows(InvalidArgTypeException.class, () -> CLOM.parse(InvalidOptionModel.class, "-o"));
   }

   @Test
   public void value_parsers_parse_values() {
      ModelWithAdapters result;
      try {
         result = CLOM.parse(ModelWithAdapters.class,
                                               "20181024",
                                               "-w",
                                               "first second third");
      } catch (IllegalArgumentException | ArgumentParseException | InvalidTargetTypeException e) {
         fail("Failed to parse", e);
         return;
      }

      assertEquals(LocalDate.of(2018, 10, 24), result.getDate());
      assertEquals(Arrays.asList("first", "second", "third"), result.getWords());
   }

   @Test
   public void value_parser_throws_exception_at_wrong_date_format() {
      Executable parseCall = () -> CLOM.parse(ModelWithAdapters.class,
                                              "20181324",
                                              "-w",
                                              "first second third");

      IllegalArgumentException e = assertThrows(IllegalArgumentException.class, parseCall);
      assertEquals(ModelWithAdapters.INVALID_DATE_ERROR_MSG, e.getMessage());
   }
}
