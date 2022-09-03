/*
 * Zach Crise
 * CIT344
 * 11/8/19
 * 
 * This is the stock class provided to complete the assignment with a few small modifications
 */

// This class represents the stock being bought and sold

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Stock {
	
		private Lock lock = new ReentrantLock();

       private double price;

       private int sharesAvailable;

       private String symbol; //e.g. DIS for Disney

       public Stock(String symbol)

       {

             price = getInitialPrice();

             sharesAvailable = 500;

             this.symbol = symbol;

       }

       //Methods to retrieve variable values for prices, shares, and the stock symbol
       public double getPrice() {
    	   return price;   
       }
       public int getAvailable() {
    	   return sharesAvailable; 
       }
       public String getAbbreviation() {
    	   return symbol;  
       }
      // You will need to modify this method -- I've put some basic logic in place for you

       public  double purchaseStock(int shares){
    	   		
	             if(sharesAvailable >= shares){
	            	 lock.lock();
                  		sharesAvailable = sharesAvailable - shares;
                     lock.unlock();
                        return shares * price;
	
	             }
	            
	             return -1; // could not buy
       }

       // You will need to modify this method -- I've put some basic logic in place for you

       public double sellStock(int shares){
    	   	
             if(shares > 0){
            	 lock.lock();
                    sharesAvailable = sharesAvailable + shares;
                lock.unlock();
                    return shares * price;

             }
             
             return -1; // could not sell
             
       }

       // gets the initial share cost 

       public double getInitialPrice(){

             // modify this to get the actual price for extra credit (can combine with method below if you'd like)
    	   	
             Random r = new Random();

             price = r.nextInt(100);

             return price;

       }

      // gets the current stock price

       public double updatePrice(){

             // modify this to get the actual price for extra credit

             return ++price;

       }

}