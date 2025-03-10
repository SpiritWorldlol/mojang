package net.minecraft.screen;

public class ArrayPropertyDelegate implements PropertyDelegate {
   private final int[] data;

   public ArrayPropertyDelegate(int size) {
      this.data = new int[size];
   }

   public int get(int index) {
      return this.data[index];
   }

   public void set(int index, int value) {
      this.data[index] = value;
   }

   public int size() {
      return this.data.length;
   }
}
