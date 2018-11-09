package de.dk.clom;

/**
 * An adapter that is used to parse a command line token string
 * into a value of another type.
 *
 * @author David Koettlitz
 * <br>Erstellt am 09.11.2018
 */
public interface TypeAdapter<T> {
   /**
    * Parses the given command line token, which represents the
    * value of the corresponding command line argument/option
    * into a value of the right type.
    *
    * @param argValue the command line token that represents
    * the value of the corresponding command line argument/option
    *
    * @return The equivalent value to the given <code>argValue</code>
    * of another type.
    *
    * @throws IllegalArgumentException if the given <code>argValue</code>
    * was invalid and could not be parsed into the right format.
    */
   T parse(String argValue) throws IllegalArgumentException;

   static final class Default implements TypeAdapter<Object> {
      @Override
      public Object parse(String t) {
         String msg = "The Default class \"implementation\" of " +
                      TypeAdapter.class.getName() +
                      " is just a holder type to indicate the absence of an explicitly provided TypeAdapter.";

         throw new UnsupportedOperationException(msg);
      }
   }
}
