package com.github.koettlitz.clom;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class ModelWithAdapters {
   public static final String INVALID_DATE_ERROR_MSG = "Invalid date.";

   @CLArgument(index=0, adapter=DateParser.class)
   private LocalDate date;

   @CLOption(key='w', longKey="words", expectsValue=true, adapter=WordParser.class)
   private List<String> words;

   public LocalDate getDate() {
      return date;
   }

   public List<String> getWords() {
      return words;
   }

   public static class DateParser implements TypeAdapter<LocalDate> {
      private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

      @Override
      public LocalDate parse(String argValue) throws IllegalArgumentException {
         try {
            return LocalDate.parse(argValue, DATE_FORMAT);
         } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(INVALID_DATE_ERROR_MSG, e);
         }
      }
   }

   public static class WordParser implements TypeAdapter<List<String>> {
      @Override
      public List<String> parse(String argValue) throws IllegalArgumentException {
         return Arrays.asList(argValue.split(" "));
      }
   }
}
