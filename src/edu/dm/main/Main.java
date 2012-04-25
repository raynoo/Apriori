package edu.dm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;

import edu.dm.apriori.Logic;
import edu.dm.db.DBConnection;
import edu.dm.object.Item;
import edu.dm.object.ItemSet;
import edu.dm.object.Transaction;

public class Main
{
	private static List<Transaction> transaction = new ArrayList<Transaction>();
	private static List<Item> itemList = new ArrayList<Item>();
	private static float SDC;
	private static float MIS_DEFAULT;
	
	private static HashMap<Integer, ItemSet> userID_Count_Map = new HashMap<Integer, ItemSet>();

	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		
		getTransaction();
//		main.transaction = getNTransactions(main.transaction, 1000);
		readMIS();
		populateItemList();
		
		new Logic(itemList, SDC, transaction);
	}
	
	private static List<Transaction> getNTransactions(List<Transaction> transaction , int n){
		
		List<Transaction> nTransactions = new ArrayList<Transaction>();
		
		Collections.shuffle(transaction);
		
		for(int i = 0; i < n ; i++){
			nTransactions.add(transaction.get(i));
		}
		
		return nTransactions;
	}

	private static void getTransaction()
	{
		//read all unique ids from db/file. store it in hashmap (id -> itemset)
		for(Integer userid : readUsersFromFile()){
			userID_Count_Map.put(userid, new ItemSet(String.valueOf(userid), 0));
		}
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(new File("data_files/transactions_2000/transactions_all_2.txt")));
			String line;
			System.out.println("Reading transactions\n");
			
			while((line = br.readLine()) != null)
			{
				String[] inputchars = line.split(",");
				Set<String> arr = new HashSet<String>();
				String input;
				
				for(int i = 0; i < inputchars.length; i++){
					if((input = inputchars[i].trim()) != null){
						arr.add(input);
						
						//update count in map
						if(userID_Count_Map.containsKey(Integer.valueOf(input))){
							userID_Count_Map.get(Integer.valueOf(input)).incrementCount();
						}
					}
				}
				
				Transaction trans = new Transaction(arr);
				transaction.add(trans);
			}

		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private static void readMIS()
	{
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(new File("data_files/default-values.txt")));
			String line;
			
			while((line = br.readLine()) != null)
			{
				if(!line.equals(" ") && !line.equals(""))
				{
					String[] input = line.split(" ");

					if(input[0].equals("SDC")){
						SDC = Float.valueOf(input[2]);
						continue;
					}
					
					if(input[0].equals("MIS")){
						MIS_DEFAULT = Float.valueOf(input[2]);
						break;
					}
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void createNewItemSet(String newItem, float mis, List<Transaction> transaction)
	{
		Item item = null;
		int count = 0 ;

		for(Transaction trans : transaction)
		{
			if (trans.containsItemSet(newItem))
				count++;
		}

		item = new Item(newItem, count, mis);

		itemList.add(item);
		
	}

	private static List<Integer> readUsersFromFile() {
		
		List<Integer> userList = new ArrayList<Integer>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("data_files/unique_users_good.txt")));
			String line;
			
			while((line = br.readLine()) != null){
				userList.add(Integer.parseInt(line));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Retrieved " + userList.size() + " users\n");
		
		return userList;
	}
	
	private static List<Integer> readUsersFromDB() {
		
		DBConnection db = new DBConnection();
		List<Integer> userList = new ArrayList<Integer>();
		
//		String distinctUsersQuery = "select distinct reviewer_id from review where rating = \"Good\"" +
//				" and reviewer_id is not null ";
		
		String distinctUsersQuery = "select distinct review.reviewer_id " +
			"from review, reviewer " +
			"where review.reviewer_id = reviewer.id and reviewer.review_count > 5 " +
			"and review.rating = \"bad\" " +
			"and review.reviewer_id is not null";
		
		ResultSet rs = db.executeQuery(distinctUsersQuery);
		
		try {
			File file = new File("data_files/unique_users_good_1star.txt");
			
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			
			while(rs.next()){
				userList.add(rs.getInt("reviewer_id"));
				fw.write(rs.getInt("reviewer_id") + "\n");
			}
			
			fw.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Retrieved " + userList.size() + " users.");
		
		return userList;
	}
	
	private static void populateItemList() {
		
		Item item = null;
		
		System.out.println("Populating item list\n");
		
		for(Integer user_id: userID_Count_Map.keySet()){
			/*for(Transaction trans : transaction){
				//update count in map
				
				if(trans.containsItemSet(user_id.toString())){
//				if(userID_Count_Map.containsKey(user_id)){
					
					userID_Count_Map.get(user_id).incrementCount();
				}
			}*/
			
			item = new Item(String.valueOf(user_id), userID_Count_Map.get(user_id).getCount(), MIS_DEFAULT);
			itemList.add(item);
		}
	}

}