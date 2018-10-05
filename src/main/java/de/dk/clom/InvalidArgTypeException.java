package de.dk.clom;

public class InvalidArgTypeException extends InvalidTargetTypeException {
   private static final long serialVersionUID = -7093001048010749126L;

   public InvalidArgTypeException(String msg) {
      super(msg);
   }

   public InvalidArgTypeException(String msg, Exception cause) {
      super(msg, cause);
   }

   public static String msgArgAndOpt(Context<?> context) {
      return "A field can only represent either a CLArgument or a CLOption. " +
             "The field " + context.currentField.getName() + " of class " +
             context.targetType.getName() + " was annotated both.";
   }
}
