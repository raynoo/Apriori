package edu.dm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class DBConnection {
	
	public static void main(String[] args) {
		DBConnection db = new DBConnection();
		db.getUsers();
	}
	
	public HashMap<Integer, ArrayList<String>> getUsers(){

		ArrayList<String> reviewerList = null;
		HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();

		String allStoresQuery = "select id from store";
		String topReviewerQuery = "select reviewer_name from data where store_name = \'";

		reviewerList = new ArrayList<String>();
		ResultSet storesRS = this.executeQuery(allStoresQuery);
		ResultSet reviewers = null;

		try {
			while(storesRS.next()){
				int storeid = storesRS.getInt("store_id");
				reviewers = this.executeQuery(topReviewerQuery+ storeid + "\' and review_rating = \'good\' order by review_top_reviewer");
				
				String username = "";
				
				while(reviewers.next()){
					username = reviewers.getString("");
					reviewerList.add(username);
				}
				map.put(storeid, reviewerList);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public ResultSet executeQuery(String query) {
		
		Statement statement = null;
		Connection conn = this.getConnection();
		ResultSet rs = null;
		
		try {
			statement = conn.createStatement();
//			System.out.println("Executing query: \"" + query + "\"");
			rs = statement.executeQuery(query);
			
		} catch(SQLException e){
			e.printStackTrace();
		}
		return rs;
	}
	
	private Connection getConnection() {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", "root");
		connectionProps.put("password", "root");

		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/product_review", connectionProps);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Connected to database");
		return conn;
	}
}
