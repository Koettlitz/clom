package de.dk.clom;

public interface TypeAdapter<T> {
   T parse(String argValue) throws IllegalArgumentException;

   public static final class Default implements TypeAdapter<Object> {
      @Override
      public Object parse(String t) {
         return t;
      }
   }
}
