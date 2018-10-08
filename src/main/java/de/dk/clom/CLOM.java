package de.dk.clom;

import static de.dk.clom.InvalidArgTypeException.msgArgAndOpt;

import java.lang.reflect.Field;
import java.util.Optional;

import de.dk.opt.ArgumentParser;
import de.dk.opt.ArgumentParserBuilder;
import de.dk.opt.ex.ArgumentParseException;
import de.dk.util.ReflectionUtils;
import de.dk.util.function.UnsafeConsumer;

public class CLOM {

   public static <T> T parse(Class<T> targetType,
                             String... args) throws ArgumentParseException,
                                                    InvalidTargetTypeException,
                                                    IllegalArgumentException {

      return parse(targetType, new ArgumentParserBuilder(), args);
   }

   public static <T> T parse(Class<T> targetType,
                             ArgumentParserBuilder builder,
                             String... args) throws ArgumentParseException,
                                                    InvalidTargetTypeException,
                                                    IllegalArgumentException {

      Context<T> context = new Context<>(targetType, builder);
      Field[] fields = targetType.getDeclaredFields();
      for (Field field : fields) {
         context.currentField = field;
         try {
            processField(context,
                         arg -> context.argAdders.add(new ArgumentAdder(arg, field)),
                         opt -> addOption(builder, field, opt));

         } catch (IllegalStateException e) {
            // Exception comes from the compare method of the argadder
            throw new InvalidTargetTypeException(e.getMessage(), e);
         }
      }

      addArgs(context);

      ArgumentParser parser = builder.buildAndGet();
      if (parser.isHelp(args)) {
         parser.printUsage(System.out);
         return null;
      }

      context.argModel = parser.parseArguments(args);

      T object;
      try {
         object = targetType.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
         throw new InvalidTargetTypeException("Could not instantiate target object of type " + targetType, e);
      }
      for (Field field : fields) {
         context.currentField = field;
         processField(context,
                      arg -> setArgValue(context, object, arg),
                      opt -> setOptValue(context, object, opt));
      }

      return object;
   }

   private static void addArgs(Context<?> context) throws InvalidTargetTypeException {
      int i = 0;
      for (ArgumentAdder argAdder : context.argAdders) {
         if (argAdder.index() != i++) {
            String msg = "Missing index " + i + ". Next index was "
                         + argAdder.index() + " at field " + argAdder.getField().getName();

            throw new InvalidTargetTypeException(msg);
         }

         argAdder.addArgumentTo(context.builder);
      }
   }

   private static void processField(Context<?> context,
                                    UnsafeConsumer<CLArgument, InvalidTargetTypeException> argProcessor,
                                    UnsafeConsumer<CLOption, InvalidTargetTypeException> optProcessor) throws InvalidTargetTypeException {

      CLArgument arg = context.currentField
                              .getDeclaredAnnotation(CLArgument.class);
      if (arg != null)
         argProcessor.accept(arg);

      CLOption opt = context.currentField
                            .getDeclaredAnnotation(CLOption.class);
      if (opt != null) {
         if (arg != null)
            throw new InvalidArgTypeException(msgArgAndOpt(context));

         optProcessor.accept(opt);
      }
   }

   static String get(String string, String ifEmpty) {
      return string.isEmpty() ? ifEmpty : string;
   }

   private static void addOption(ArgumentParserBuilder builder, Field field, CLOption opt) {
      builder.buildOption(opt.key())
             .setLongKey(opt.longKey())
             .setDescription(opt.description())
             .setExpectsValue(opt.expectsValue())
             .build();
   }

   private static void setArgValue(Context<?> context, Object target, CLArgument arg) throws InvalidArgTypeException {
      String value = context.argModel
                            .getArgumentValue(get(arg.name(), context.currentField.getName()));
      if (value == null)
         return;

      try {
         ReflectionUtils.setPrimitiveValue(target, context.currentField, value);
      } catch (IllegalStateException e) {
         throw new InvalidArgTypeException(e.getMessage(), e);
      }
   }

   private static void setOptValue(Context<?> context, Object target, CLOption opt) throws InvalidArgTypeException {
      if (opt.expectsValue()) {
         Optional<String> value = context.argModel
                                         .getOptionalValue(opt.key());
         if (value.isPresent()) {
            try {
               ReflectionUtils.setPrimitiveValue(target, context.currentField, value.get());
            } catch (IllegalStateException e) {
               throw new InvalidArgTypeException(e.getMessage(), e);
            }
         }
      } else {
         Class<?> type = context.currentField.getType();

         if (!type.equals(Boolean.TYPE) && !type.equals(Boolean.class)) {
            String msg = "Option field " + context.currentField.getName()
                         + " has to be of type boolean or must expect a value.";

            throw new InvalidArgTypeException(msg);
         }

         context.currentField.setAccessible(true);
         try {
            context.currentField.set(target, context.argModel.isOptionPresent(opt.key()));
         } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InvalidArgTypeException("Could not set flag " + context.currentField.getName(), e);
         }
      }
   }
}
