1. Create a class, that represents your argument structure.
2. Annotate the fields of that class with ```@CLArgument```/```@CLVarArgs``` and ```@CLOption```
3. Parse the arguments with the command line to object mapper (```CLOM``` class), e.g.:
    ```java
    public static void main(String... args) {
        MyArgModel parsedArgs = CLOM.parse(MyArgModel.class, args);
    }
    ```
4. Error handling