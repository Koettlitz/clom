package com.github.koettlitz.clom;

import com.github.koettlitz.opt.ArgumentModel;
import com.github.koettlitz.opt.ArgumentParserBuilder;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.util.TreeSet;

public class Context<T> {
   final Class<T> targetType;
   final ArgumentParserBuilder builder;
   Field currentField;
   ArgumentModel argModel;
   Object target;
   TreeSet<ArgumentAdder> argAdders = new TreeSet<>();

   Context(Class<T> targetType, ArgumentParserBuilder builder) {
      this.targetType = requireNonNull(targetType);
      this.builder = requireNonNull(builder);
   }

   public Class<T> getTargetType() {
      return targetType;
   }
}
