package com.github.koettlitz.clom;

public class ArgumentModel {
   @CLArgument(index=0)
   private String arg0;

   @CLArgument(index=1, mandatory=false)
   private byte arg1 = -1;

   @CLArgument(index=2, mandatory=false)
   private short arg2 = -1;

   @CLArgument(index=3, mandatory=false)
   private int arg3 = -1;

   @CLArgument(index=4, mandatory=false)
   private long arg4 = -1;

   @CLArgument(index=5, mandatory=false)
   private float arg5 = -1;

   @CLArgument(index=6, mandatory=false)
   private double arg6 = -1;

   @CLOption(key='f', longKey="flag")
   private boolean flag;

   @CLOption(key='b', expectsValue=true)
   private long bar = -1;

   public String getArg0() {
      return arg0;
   }

   public int getArg1() {
      return arg1;
   }

   public float getArg2() {
      return arg2;
   }

   public int getArg3() {
      return arg3;
   }

   public long getArg4() {
      return arg4;
   }

   public float getArg5() {
      return arg5;
   }

   public double getArg6() {
      return arg6;
   }

   public boolean isFlag() {
      return flag;
   }

   public long getBar() {
      return bar;
   }

}
