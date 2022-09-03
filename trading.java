/*
 * Zach Crise
 * CIT344
 * 11/8/2019
 * 
 * Objective build a program that will simulate the stock market and will handle race conditions
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class trading {

	public static void main(String[] args) {
			//starting money of each trader
			double money = 10000;
			
		try {
			
			ExecutorService market = Executors.newCachedThreadPool();
			
			//sets up the stock object and gives it the following symbol.
			Stock stocks = new Stock("DIS");
			
			//iterating variables for while loops.
			int logEx = 0;
			int updateEx = 0;
			int stockRounds = 0;
			
			//trader threads with the stock object, starting money and ID
			Thread trader1 = new Thread(new trader(stocks, money, 1));
			Thread trader2 = new Thread(new trader(stocks, money, 2));
			Thread trader3 = new Thread(new trader(stocks,money, 3));
			
			//Update and log threads
			Thread update = new Thread(new priceUpdate(stocks));
			Thread log = new Thread(new transaction(stocks));
			
			//While loop to run threads 10 times each.
			while(stockRounds < 30) {
				//runs trader 1 and ups the counter
				market.execute(trader1);
				stockRounds++;
				
				//checks for every 5th round to execute log and updater threads
				if(stockRounds == 5 || stockRounds == 10 || stockRounds == 15 || stockRounds == 20 || stockRounds == 25 || stockRounds == 30) {
					//While loops helps logs record the correct price so the updater does not mess up the correct pricing logged
					while(logEx < 1) {
						market.execute(log);
						logEx++;
					}
					logEx = 0;
					
					while(updateEx < 1) {
						market.execute(update);
						updateEx++;
					}
					updateEx = 0;
				}
				
				////runs trader 2 and ups the counter;
				market.execute(trader2);
				stockRounds++;
				
				//checks for every 5th round to execute log and updater threads
				if(stockRounds == 5 || stockRounds == 10 || stockRounds == 15 || stockRounds == 20 || stockRounds == 25 || stockRounds == 30) {
					//While loops helps logs record the correct price so the updater does not mess up the correct pricing logged
					while(logEx < 1) {
						market.execute(log);
						logEx++;
					}
					logEx = 0;
					
					while(updateEx < 1) {
						market.execute(update);
						updateEx++;
					}
					updateEx = 0;
				}
				
				//runs trader 3 and ups the counter;
				market.execute(trader3);
				stockRounds++;
				
				//checks for every 5th round to execute log and updater threads
				if(stockRounds == 5 || stockRounds == 10 || stockRounds == 15 || stockRounds == 20 || stockRounds == 25 || stockRounds == 30) {
					//While loops helps logs record the correct price so the updater does not mess up the correct pricing logged
					while(logEx < 1) {
						market.execute(log);
						logEx++;
					}
					logEx = 0;
					
					while(updateEx < 1) {
						market.execute(update);
						updateEx++;
					}
					updateEx = 0;
				}
				
					
			}

		} catch(IllegalThreadStateException e) {
			System.out.println("An IllegalThreadStateException has occurred");
		}catch(IllegalMonitorStateException e) {
			System.out.println("An IllegalMonitorStateException has occurred");
		}
	}
	
	//The amount of stock to buy and or sell
	public static int getAmount() {
		return 100;
	}
	
}

//trader thread worker
class trader implements Runnable{
	private Stock stock;
	private double traderMoney;
	private int ID;
	Lock locker = lordOfTheLocks.getLock();
	private String time = "";
	private int avail = 0;
	
	trader(Stock stocks, double money, int traderNum){
		stock = stocks;
		traderMoney = money;
		ID = traderNum;
	}

	@Override
	public void run() {
	
		try {
			//gets the current available stock and either buys randomly or if the stock is at 500 it buys
			avail = stock.getAvailable();
			if((Math.random() >= .5 && avail > 0) || (avail >= 500 && avail > 0)) {
				buy();
			} else {
				sell();
			}
			
			//sleeps the the current trader
			Thread.sleep(5000);
			
		} catch (InterruptedException e) {
			System.out.println("A trader has been interrupted");
		}
		
	}
	
	//used to buy stock
	public void buy() {
		locker.lock();
		//grabs the amount to buy and prints that the stock is bought as well as how much is left
		stock.purchaseStock(trading.getAmount());
		System.out.println("Bought " + stock.getAbbreviation() +  " stock");
		System.out.println(stock.getAbbreviation() + " stock left: " + stock.getAvailable());
		//prints the current price of the stock, which trader bought it, and how much money they have left
		System.out.println("Current Price " + stock.getPrice());
		traderMoney = traderMoney - stock.getPrice();
		System.out.println("Current cash of trader " + ID + " " + traderMoney);
		//saves the time at which it was bought and sends it to a queue
		time = DateTimeFormatter.ofPattern("EE MMM dd HH:mm:ss Y").format(LocalDateTime.now()) + " Bought";
		timeTransaction.setTime(time);
		locker.unlock();
	}
	
	public void sell() {
		locker.lock();
		//grabs the amount to sell and prints that the stock is sold as well as how much is left
		stock.sellStock(trading.getAmount());
		System.out.println("Sold " + stock.getAbbreviation() +  " stock");
		System.out.println(stock.getAbbreviation() + " stock left: " + stock.getAvailable());
		//prints the current price of the stock, which trader sold it, and how much money they have left
		System.out.println("Current Price " + stock.getPrice());
		traderMoney = traderMoney + stock.getPrice();
		System.out.println("Current cash of trader " + ID + " " + traderMoney);
		//saves the time at which it was sold and sends it to a queue
		time = DateTimeFormatter.ofPattern("EE MMM dd HH:mm:ss Y").format(LocalDateTime.now()) + " Sold";
		timeTransaction.setTime(time);
		locker.unlock();
	}
}

//The class for holding the queue responsible for holding bought and sold times.
class timeTransaction{
	static ConcurrentLinkedQueue<String> timeQueue = new ConcurrentLinkedQueue<String>();
	
	//Get a time from a trader and add it to the queue
	public static void setTime(String date){
		timeQueue.add(date);
		
	}
	
	//Send the times to be logged or if there is no time available report the issue
	public static String getTime() {
		if(timeQueue.isEmpty())
			return "There appears to be a problem with the queue";
		else {
			
			return timeQueue.poll();
		}
			
	}
	
}

//Class to distribute the same lock to all threads
class lordOfTheLocks{
	static Lock locks = new ReentrantLock();
	
	//give the lock for use to the threads
	public static Lock getLock() {
		return locks;
	}
	
}

//price update thread worker
class priceUpdate implements Runnable{
	private Stock stock;
	Lock priceLock = lordOfTheLocks.getLock();
	
	priceUpdate(Stock stocks){
		stock = stocks;
	}

	@Override
	public void run() {
		//show the current price and the new price after calling the update price method
		priceLock.lock();
		System.out.println("Current Price: " + stock.getPrice());
		stock.updatePrice();
		System.out.println("New Price: " + stock.getPrice());
		priceLock.unlock();
	}
}

//log thread worker
class transaction implements Runnable{
	private Stock stock;
	int amount = trading.getAmount();
	int bought = 0;
	int sold = 0;
	String logDate = "";
	Lock logLock = lordOfTheLocks.getLock();
	transaction(Stock stocks){
		stock = stocks;
	}

	@Override
	public void run() {
		logLock.lock();
		try {
		//Filewriter to append to a log file
		FileWriter logFile = new FileWriter("log.txt", true);
		PrintWriter logger = new PrintWriter(logFile, true);
		
		//Logs the last five items
		for(int i = 0; i < 5; i++) {
			//gets the time of the transaction
			logDate = timeTransaction.getTime();
		
			//if it was a buying transaction, record it as so, format it and then append it to the log file.
			if(logDate.contains("Bought")) {
				System.out.println(logDate.substring(0, 19) + " " + stock.getAbbreviation() + " " + logDate.substring(20, 24) + " Bought " + amount + " share for " + stock.getPrice());
				logger.println(logDate.substring(0, 19) + " " + stock.getAbbreviation() + " " + logDate.substring(20, 24) + " Bought " + amount + " share for " + stock.getPrice());
			} //if it was a selling transaction, record it as so, format it and then append it to the log file.	
			else if(logDate.contains("Sold")) {
				System.out.println(logDate.substring(0, 19) + " " + stock.getAbbreviation() + " " + logDate.substring(20, 24) + " Sold " + amount + " share for " + stock.getPrice());
				logger.println(logDate.substring(0, 19) + " " + stock.getAbbreviation() + " " + logDate.substring(20, 24) + " Sold " + amount + " share for " + stock.getPrice());
			} //if an issue was reported write this to the system only
			else {
				System.out.println("Something went wrong with the logs.");
			}
		}
		logLock.unlock();
		
		
		
		logger.close();
		} catch(IOException e) {
			System.out.println("Could not find the file log.txt");
		}
	}
}