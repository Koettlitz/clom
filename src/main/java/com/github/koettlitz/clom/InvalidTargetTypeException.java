package com.github.koettlitz.clom;

public class InvalidTargetTypeException extends RuntimeException {
   private static final long serialVersionUID = 7479136619335273589L;

   public InvalidTargetTypeException() {

   }

   public InvalidTargetTypeException(String msg) {
      super(msg);
   }

   public InvalidTargetTypeException(String msg, Exception cause) {
      super(msg, cause);
   }
}
