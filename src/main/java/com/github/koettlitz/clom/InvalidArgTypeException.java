package com.github.koettlitz.clom;

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
      return msgCanOnlyBeOneOfEither("a CLArgument", "a CLOption", context);
   }

   static String msgVarArgsAndArgs(Context<?> context) {
      return msgCanOnlyBeOneOfEither("a CLArgument", "VarArgs", context);
   }

   static String msgVarArgsAndOpt(Context<?> context) {
      return msgCanOnlyBeOneOfEither("a CLOption", "VarArgs", context);
   }

   private static String msgCanOnlyBeOneOfEither(String annotationA, String annotationB, Context<?> context) {
      return String.format("A field can only represent either %s or %s. The field %s of class %s was annotated both.",
                           annotationA,
                           annotationB,
                           context.currentField.getName(),
                           context.targetType.getName());
   }
}
