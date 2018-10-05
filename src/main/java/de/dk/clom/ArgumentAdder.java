package de.dk.clom;

import java.lang.reflect.Field;

import de.dk.opt.ArgumentParserBuilder;

public class ArgumentAdder implements Comparable<ArgumentAdder> {
   private final CLArgument arg;
   private final Field field;

   public ArgumentAdder(CLArgument arg, Field field) {
      this.arg = arg;
      this.field = field;
   }

   public ArgumentParserBuilder addArgumentTo(ArgumentParserBuilder builder) {
      return builder.buildArgument(CLOM.get(arg.name(), field.getName()))
                    .setMandatory(arg.mandatory())
                    .setDescription(arg.description())
                    .build();
   }

   public int index() {
      return arg.index();
   }

   public Field getField() {
      return field;
   }

   @Override
   public int compareTo(ArgumentAdder other) {
      if (other == this)
         return 0;

      if (index() == other.index()) {
         throw new IllegalStateException("Duplicate index of fields " + field.getName()
                                         + "(index=" + index() + ") and " + field.getName()
                                         + "(index=" + other.index() + ")");
      }

      return arg.index() - other.arg.index();
   }

}
