package de.dk.clom;

import static de.dk.clom.InvalidArgTypeException.msgArgAndOpt;

import java.lang.reflect.Field;
import java.util.Optional;

import de.dk.clom.TypeAdapter.Default;
import de.dk.opt.ArgumentParser;
import de.dk.opt.ArgumentParserBuilder;
import de.dk.opt.ex.ArgumentParseException;
import de.dk.util.ReflectionUtils;
import de.dk.util.function.UnsafeConsumer;

public class CLOM<T> {
   private static boolean printUsageOnHelp = true;

   private final ArgumentParser parser;
   private final Context<T> context;

   public static boolean isPrintUsageOnHelp() {
      return printUsageOnHelp;
   }

   public static void setPrintUsageOnHelp(boolean printUsageOnHelp) {
      CLOM.printUsageOnHelp = printUsageOnHelp;
   }

   public CLOM(Class<T> targetType) {
      this.context = new Context<>(targetType, new ArgumentParserBuilder());
      this.parser = buildParser(context);
   }

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
      ArgumentParser parser = buildParser(context);
      if (printUsageOnHelp && parser.isHelp(args)) {
         parser.printUsage(System.out);
         return null;
      }

      return parse(parser, context, args);
   }

   private static <T> T parse(ArgumentParser parser,
                              Context<T> context,
                              String... args) throws ArgumentParseException,
                                                     IllegalArgumentException {

      context.argModel = parser.parseArguments(args);

      T object;
      try {
         object = context.targetType
                         .newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
         String msg = "Could not instantiate target object of type " + context.targetType;
         throw new InvalidTargetTypeException(msg, e);
      }

      for (Field field : context.targetType.getDeclaredFields()) {
         context.currentField = field;
         processField(context,
                      arg -> setArgValue(context, object, arg),
                      opt -> setOptValue(context, object, opt));
      }

      return object;
   }

   private static ArgumentParser buildParser(Context<?> context) {
      ArgumentParserBuilder builder = context.builder;
      Field[] fields = context.targetType
                              .getDeclaredFields();

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

      return builder.buildAndGet();
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

   private static void setArgValue(Context<?> context,
                                   Object target,
                                   CLArgument arg) throws InvalidTargetTypeException {
      String value = context.argModel
                            .getArgumentValue(get(arg.name(), context.currentField.getName()));
      if (value == null)
         return;

      if (arg.adapter() != Default.class) {
         parseValue(context, arg.adapter(), target, value);
      } else {
         if (context.currentField.getType().equals(Boolean.TYPE)) {
            System.out.println("It is strange that this application wants you "
                               + "to provide a boolean value as a text argument"
                               + " instead of just an option, but hey! "
                               + "Like my father used to say: \""
                               + "Die Freiheit des Programmierers ist grenzenlos!\"");
         }

         try {
            ReflectionUtils.setPrimitiveValue(target, context.currentField, value);
         } catch (IllegalStateException e) {
            throw new InvalidArgTypeException(e.getMessage(), e);
         }
      }
   }

   private static void setOptValue(Context<?> context,
                                   Object target,
                                   CLOption opt) throws InvalidTargetTypeException {
      if (opt.expectsValue()) {
         Optional<String> value = context.argModel
                                         .getOptionalValue(opt.key());
         if (value.isPresent()) {
            if (opt.adapter() != Default.class) {
               parseValue(context, opt.adapter(), target, value.get());
            } else {
               try {
                  ReflectionUtils.setPrimitiveValue(target, context.currentField, value.get());
               } catch (IllegalStateException e) {
                  throw new InvalidArgTypeException(e.getMessage(), e);
               }
            }
         }
      } else {
         Class<?> type = context.currentField.getType();

         if (!type.equals(Boolean.TYPE) && !type.equals(Boolean.class)) {
            String msg = "Option field " + context.currentField.getName()
                         + " has to be of type boolean or must expect a value.";

            throw new InvalidArgTypeException(msg);
         }

         setFieldValue(context.currentField, target, context.argModel.isOptionPresent(opt.key()));
      }
   }

   private static void parseValue(Context<?> context,
                                  Class<? extends TypeAdapter<?>> adapterType,
                                  Object target,
                                  String value) throws InvalidTargetTypeException {

      TypeAdapter<?> adapter;
      try {
         adapter = adapterType.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
         String msg = "Could not instantiate value adapter of type "
                      + adapterType.getName() + " for field "
                      + context.currentField;

         throw new InvalidTargetTypeException(msg, e);
      }

      Object parsedValue;
      parsedValue = adapter.parse(value);
      setFieldValue(context.currentField, target, parsedValue);
   }

   private static void setFieldValue(Field field,
                                     Object target,
                                     Object value) throws InvalidArgTypeException {
      try {
         field.setAccessible(true);
         field.set(target, value);
      } catch (IllegalArgumentException |
               IllegalAccessException |
               SecurityException e) {

         throw new InvalidArgTypeException("Could not set value " + value + " to field " + field.getName(), e);
      }
   }

   public T parse(String... args) throws ArgumentParseException,
                                         IllegalArgumentException {
      return parse(parser, context, args);
   }

   public ArgumentParser getParser() {
      return parser;
   }
}
