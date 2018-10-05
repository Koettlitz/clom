package de.dk.clom;

public class ArgumentModel {
   @CLArgument(index=0)
   private String arg0;

   @CLArgument(index=1, mandatory=false)
   private int arg1 = -1;

   @CLArgument(index=2, mandatory=false)
   private float arg2 = -1;

   @CLOption(key='f', longKey="flag")
   private boolean flag;

   @CLOption(key='b', expectsValue=true)
   private long bar = -1;

   public String getArg0() {
      return arg0;
   }

   public void setArg0(String arg0) {
      this.arg0 = arg0;
   }

   public int getArg1() {
      return arg1;
   }

   public void setArg1(int arg1) {
      this.arg1 = arg1;
   }

   public float getArg2() {
      return arg2;
   }

   public void setArg2(float arg2) {
      this.arg2 = arg2;
   }

   public boolean isFlag() {
      return flag;
   }

   public void setFlag(boolean flag) {
      this.flag = flag;
   }

   public long getBar() {
      return this.bar;
   }

   public void setBar(long bar) {
      this.bar = bar;
   }


}
