package ir.ac.ut.common;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Hamed
 */
public class Pair<firstThing, secondThing>{
   private firstThing first;//first member of pair
   private secondThing second;//second member of pair

   public Pair(firstThing first, secondThing second){
     this.first = first;
     this.second = second;
   }

   public void setFirst(firstThing first){
    this.first = first;
   }

   public void setSecond(secondThing second) {
     this.second = second;
   }

   public firstThing getFirst() {
     return this.first;
   }

   public secondThing getSecond() {
     return this.second;
   }
}
