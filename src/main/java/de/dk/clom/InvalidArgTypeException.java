package de.dk.clom;

/**
 * Indicates that the target type to be parsed from command line arguments
 * is invalidly annotated, e.g. a {@link CLOption} where the
 * {@link CLOption#expectsValue()} parameter returns false but the field is
 * not of type <code>boolean</code>.
 *
 * @author David Koettlitz
 * <br>Erstellt am 09.11.2018
 */
public class InvalidArgTypeException extends InvalidTargetTypeException {
   private static final long serialVersionUID = -7093001048010749126L;

   public InvalidArgTypeException(String msg) {
      super(msg);
   }

   public InvalidArgTypeException(String msg, Exception cause) {
      super(msg, cause);
   }

   static String msgArgAndOpt(Context<?> context) {
      return "A field can only represent either a CLArgument or a CLOption. " +
             "The field " + context.currentField.getName() + " of class " +
             context.targetType.getName() + " was annotated both.";
   }
}
