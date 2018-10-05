package de.dk.clom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.dk.opt.ArgumentParserBuilder;
import de.dk.opt.ex.ArgumentParseException;

public class CLOMTest {
   private static final String ARG0 = "foo";
   private static final int ARG1 = 8;
   private static final char FOO_KEY = 'f';
   private static final char BAR_KEY = 'b';
   private static final long BAR_VALUE = 1024l;

   private static ArgumentModel parse(String... args) {
      try {
         return CLOM.parse(ArgumentModel.class, ArgumentParserBuilder.begin(), args);
      } catch (IllegalArgumentException | ArgumentParseException | InvalidTargetTypeException e) {
         fail(e);
         return null;
      }
   }

   @Test
   public void argumentIsPresent() {
      ArgumentModel result = parse(ARG0);
      assertEquals(ARG0, result.getArg0());
   }

   @Test
   public void nonGivenArgsAreAbsent() {
      ArgumentModel result = parse(ARG0);
      assertEquals(-1, result.getArg1());
      assertEquals(-1, result.getArg2());
   }

   @Test
   public void optionIsPresent() {
      ArgumentModel result = parse(ARG0, "-" + FOO_KEY);
      assertTrue(result.isFlag(), "Flag was given, but is not present in object.");
   }

   @Test
   public void nonGivenOptionsAreAbsent() {
      ArgumentModel result = parse(ARG0);
      assertEquals(false, result.isFlag());
      assertEquals(-1, result.getBar());
   }

   @Test
   public void optionValueIsPresent() {
      ArgumentModel result = parse(ARG0, "-" + BAR_KEY, "" + BAR_VALUE);
      assertEquals(BAR_VALUE, result.getBar());
   }

   @Test
   public void argsAndOptionsArePresent() {
      ArgumentModel result = parse(ARG0, "" + ARG1, "-" + FOO_KEY, "-" + BAR_KEY, "" + BAR_VALUE);
      assertEquals(ARG0, result.getArg0());
      assertEquals(ARG1, result.getArg1());
      assertEquals(true, result.isFlag());
      assertEquals(BAR_VALUE, result.getBar());
   }
}
