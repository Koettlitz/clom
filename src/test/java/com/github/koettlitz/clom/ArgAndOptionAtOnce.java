package com.github.koettlitz.clom;

public class ArgAndOptionAtOnce {
   @CLArgument(index=0)
   @CLOption(key='f', expectsValue=true)
   private double invalidAttribute;
}
