package de.dk.clom;

import static de.dk.clom.InvalidArgTypeException.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;

import de.dk.clom.TypeAdapter.Default;
import de.dk.opt.ArgumentParser;
import de.dk.opt.ArgumentParserBuilder;
import de.dk.opt.ex.ArgumentParseException;
import de.dk.util.ReflectionUtils;
import de.dk.util.UnsafeConsumer;

/**
 * This class parses a custom object from provided command line arguments.
 * By providing the type of the target object and the command line arguments
 * the command line object mapper creates the object and maps the values
 * from the command line to the corresponding fields of the object.
 * This can either be done by directly calling the {@link #parse(Class, String...)}
 * method directly or by creating a new <code>CLOM</code> instance and
 * calling the {@link #parse(String...)} method on it.<br>
 * If you parse the arguments over an instance of <code>CLOM</code>
 * you can modify the {@link ArgumentParser}, that the <code>CLOM</code>
 * instance uses by calling the {@link #getParser()} method before actually
 * parsing the arguments. Additionally it allows you to print a generated
 * help message by using the parsers {@link ArgumentParser#printUsage(java.io.PrintStream)}
 * method. Some code example right here:<br>
 * <pre>
 * CLOM&lt;MyArgModel&gt; clom = new CLOM<>(MyArgModel.class);
 * ArgumentParser parser = clom.getParser();
 * if (parser.isHelp(args)) {
 *    parser.printUsage(System.out);
 *    return;
 * }
 *
 * result = clom.parse(args);
 * </pre>
 *
 * This behaviour (printing a generated help message if
 * arguments like <code>--help</code> are given) is enabled by default when
 * calling the static {@link #parse(Class, String...)} method directly.
 * In that case <code>null</code> is returned by that method. This behaviour
 * can be disabled by {@link #setPrintUsageOnHelp(boolean)}.
 *
 * @param <T> The type of the custom target object
 *
 * @see CLArgument
 * @see CLOption
 *
 * @author David Koettlitz
 * <br>Erstellt am 09.11.2018
 */
public class CLOM<T> {
   private static boolean printUsageOnHelp = true;

   private final ArgumentParser parser;
   private final Context<T> context;

   /**
    * Flag that controls the behaviour of this class to react to
    * arguments, that indicate the request for help like
    * e.g. <code>--help</code>. This does not affect any
    * <code>CLOM</code> instances.
    *
    * @return <code>true</code> if the static <code>parse</code>
    * methods prints a generated help message when help requesting
    * arguments are given and returns <code>null</code><br>
    * <code>false</code> if help requesting args are treated as normal
    * arguments and are tried to be mapped to any field of the
    * target object.
    */
   public static boolean isPrintUsageOnHelp() {
      return printUsageOnHelp;
   }

   /**
    * Set the flag that controls the behaviour of this class to react to
    * arguments, that indicate the request for help like
    * e.g. <code>--help</code>. This does not affect any
    * <code>CLOM</code> instances.
    *
    * @param printUsageOnHelp <code>true</code> if the static <code>parse</code>
    * methods should print a generated help message when help requesting
    * arguments are given and then return <code>null</code><br>
    * <code>false</code> if help requesting args should be treated as normal
    * arguments and be tried to be mapped to any field of the
    * target object.
    */
   public static void setPrintUsageOnHelp(boolean printUsageOnHelp) {
      CLOM.printUsageOnHelp = printUsageOnHelp;
   }

   /**
    * Creates a new command line object mapper, which parses instances of
    * the given target type from the command line.
    *
    * @param targetType The type of the target object to be parsed from
    * the command line
    *
    * @throws InvalidTargetTypeException if <code>targetType</code> is invalidly
    * annotated
    */
   public CLOM(Class<T> targetType) throws InvalidTargetTypeException {
      this.context = new Context<>(targetType, new ArgumentParserBuilder());
      this.parser = buildParser(context);
   }

   /**
    * Parses the given <code>args</code> into an instance of
    * <code>targetType</code>. If the first argument indicates
    * a request for help like e.g. <code>--help</code> AND
    * {@link #setPrintUsageOnHelp(boolean)} is set to <code>true</code>
    * (which is the default state) a generated help message will be printed
    * to standardout and <code>null</code> will be returned.
    *
    * @param targetType The type of the object to be parsed from the
    * command line. The fields of <code>targetType</code> should be annotated.
    * @param args the command line arguments
    *
    * @return An instance of <code>targetType</code> that contains the
    * values provided by the <code>args</code>
    *
    * @throws ArgumentParseException if the given <code>args</code> do not
    * match the format of <code>targetType</code>
    * @throws InvalidTargetTypeException if the given <code>tagetType</code> is
    * invalidly annotated
    * @throws IllegalArgumentException if <code>args</code>
    */
   public static <T> T parse(Class<T> targetType,
                             String... args) throws ArgumentParseException,
                                                    InvalidTargetTypeException,
                                                    IllegalArgumentException {

      return parse(targetType, new ArgumentParserBuilder(), args);
   }

   /**
    * Parses the given <code>args</code> into an instance of
    * <code>targetType</code>. If the first argument indicates
    * a request for help like e.g. <code>--help</code> AND
    * {@link #setPrintUsageOnHelp(boolean)} is set to <code>true</code>
    * (which is the default state) a generated help message will be printed
    * to standardout and <code>null</code> will be returned.
    *
    * @param targetType The type of the object to be parsed from the
    * command line. The fields of <code>targetType</code> should be annotated.
    * @param builder the builder to build the {@link ArgumentParser} with,
    * that is used to parse the arguments
    * @param args the command line arguments
    *
    * @return An instance of <code>targetType</code> that contains the
    * values provided by the <code>args</code>
    *
    * @throws ArgumentParseException if the given <code>args</code> do not
    * match the format of <code>targetType</code>
    * @throws InvalidTargetTypeException if the given <code>tagetType</code> is
    * invalidly annotated
    * @throws IllegalArgumentException if <code>args</code>
    */
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
                      varArgs -> setVarArgsValue(context, object, varArgs),
                      opt -> setOptValue(context, object, opt));
      }

      return object;
   }

   private static ArgumentParser buildParser(Context<?> context) throws InvalidTargetTypeException {
      ArgumentParserBuilder builder = context.builder;
      Field[] fields = context.targetType
                              .getDeclaredFields();

      for (Field field : fields) {
         context.currentField = field;
         try {
            processField(context,
                         arg -> context.argAdders.add(new ArgumentAdder(arg, field)),
                         varArgs -> setVarArgs(builder, context),
                         opt -> addOption(builder, field, opt));

         } catch (IllegalStateException e) {
            // Exception comes from the compare method of the argadder
            throw new InvalidTargetTypeException(e.getMessage(), e);
         }
      }

      addArgs(context);

      return builder.buildAndGet();
   }

   private static void setVarArgs(ArgumentParserBuilder builder, Context<?> context) {
      if (!Collection.class.isAssignableFrom(context.currentField.getType())) {
         String msg = String.format("VarArgs field %s of type %s has to be a collection.",
                                    context.currentField.getName(),
                                    context.targetType.getName());

         throw new InvalidTargetTypeException(msg);
      }

      try {
         builder.setVarArgs(true);
      } catch (IllegalStateException e) {
         String msg = String.format("Error with VarArgs annotation in type %s at field %s: \"%s\"",
                                    context.targetType.getName(),
                                    context.currentField.getName(),
                                    e.getMessage());

         throw new InvalidTargetTypeException(msg, e);
      }
   }

   private static void addOption(ArgumentParserBuilder builder, Field field, CLOption opt) {
      builder.buildOption(opt.key())
              .setLongKey(opt.longKey())
              .setDescription(opt.description())
              .setExpectsValue(opt.expectsValue())
              .build();
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
                                    UnsafeConsumer<CLVarArgs, InvalidTargetTypeException> varArgProcessor,
                                    UnsafeConsumer<CLOption, InvalidTargetTypeException> optProcessor) throws InvalidTargetTypeException {

      CLVarArgs varArgs = context.currentField.getDeclaredAnnotation(CLVarArgs.class);
      if (varArgs != null)
         varArgProcessor.accept(varArgs);

      CLArgument arg = context.currentField.getDeclaredAnnotation(CLArgument.class);
      if (arg != null) {
         if (varArgs != null)
            throw new InvalidArgTypeException(msgVarArgsAndArgs(context));

         argProcessor.accept(arg);
      }

      CLOption opt = context.currentField.getDeclaredAnnotation(CLOption.class);
      if (opt != null) {
         if (arg != null)
            throw new InvalidArgTypeException(msgArgAndOpt(context));
         if (varArgs != null)
            throw new InvalidArgTypeException(msgVarArgsAndOpt(context));

         optProcessor.accept(opt);
      }
   }

   static String get(String string, String ifEmpty) {
      return string.isEmpty() ? ifEmpty : string;
   }

   private static void setArgValue(Context<?> context,
                                   Object target,
                                   CLArgument arg) throws InvalidTargetTypeException {
      String argName = get(arg.name(), context.currentField.getName());
      String value = context.argModel
                            .getArgumentValue(argName);
      if (value == null)
         return;

      if (arg.adapter() != Default.class) {
         Object parsedValue = parseValue(context, arg.adapter(), target, value);
         setFieldValue(context.currentField, target, parsedValue);
      } else {
         if (context.currentField.getType().equals(Boolean.TYPE)) {
            System.out.println("It is strange that this application wants you "
                               + "to provide a boolean value as a text argument "
                               + "instead of just an option, but hey! "
                               + "Like my father used to say: \""
                               + "Die Freiheit des Programmierers ist grenzenlos!\"");
         }

         setCurrentFieldsValue(context, argName, target, value);
      }
   }

   private static void setVarArgsValue(Context<?> context, Object target, CLVarArgs varArgs) {
      Collection<Object> collection;
      try {
         collection = varArgs.collectionType().newInstance();
      } catch (InstantiationException | IllegalAccessException  e) {
         String msg = String.format("Could not instantiate varArgs collection of type %s at field %s of type %s.",
                                    varArgs.collectionType().getName(),
                                    context.currentField.getName(),
                                    context.targetType.getName());

         throw new InvalidTargetTypeException(msg);
      }

      for (String value : context.argModel.getPlainArguments()) {
         Object parsedValue = parseValue(context, varArgs.adapter(), target, value);
         collection.add(parsedValue);
      }

      setFieldValue(context.currentField, target, collection);
   }

   private static void setOptValue(Context<?> context,
                                   Object target,
                                   CLOption opt) throws InvalidTargetTypeException {
      if (opt.expectsValue()) {
         Optional<String> value = context.argModel
                                         .getOptionalValue(opt.key());
         if (value.isPresent()) {
            if (opt.adapter() != Default.class) {
               Object parsedValue = parseValue(context, opt.adapter(), target, value.get());
               setFieldValue(context.currentField, target, parsedValue);
            } else {
               String optName = opt.longKey() == null ? "-" + opt.key() : opt.longKey();
               setCurrentFieldsValue(context, optName, target, value.get());
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

   private static Object parseValue(Context<?> context,
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

      return adapter.parse(value);
   }

   private static void setCurrentFieldsValue(Context<?> context, String argName, Object target, String value) {
      if (!ReflectionUtils.isPrimitive(context.currentField.getType())) {
         String msg = "Could not set value " +
                      value +
                      " of the command line argument/option " +
                      argName + " of field " + context.currentField.getName() +
                      ", because the field was not of a primitive type nor String and " +
                      "no TypeAdapter was provided.";
         throw new InvalidTargetTypeException(msg);
      }

      try {
         ReflectionUtils.setPrimitiveValue(target, context.currentField, value);
      } catch (IllegalStateException e) {
         throw new InvalidArgTypeException(e.getMessage(), e);
      }
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

   /**
    * Parses the given <code>args</code> into an instance of the
    * given <code>targetType</code>.
    *
    * @param args The command line arguments
    *
    * @return An instance of <code>targetType</code> that contains the
    * values provided by the <code>args</code>
    *
    * @throws ArgumentParseException if the given <code>args</code> do not
    * match the format of <code>targetType</code>
    * @throws InvalidTargetTypeException if the given <code>tagetType</code> is
    * invalidly annotated
    * @throws IllegalArgumentException if <code>args</code>
    */
   public T parse(String... args) throws ArgumentParseException,
                                         InvalidTargetTypeException,
                                         IllegalArgumentException {
      return parse(parser, context, args);
   }

   /**
    * Get the parser, that is used to parse the command line arguments.
    * Can be modified before parsing.
    *
    * @return the parser to parse the command line args
    */
   public ArgumentParser getParser() {
      return parser;
   }
}
