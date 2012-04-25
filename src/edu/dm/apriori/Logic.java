package edu.dm.apriori;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dm.object.AssociationRule;
import edu.dm.object.Item;
import edu.dm.object.ItemSet;
import edu.dm.object.Transaction;

public class Logic 
{

	private Logger logger = Logger.getLogger(Logic.class);

	List<Transaction> TransactionList;

	List<Item> ItemList;     // sorted item according to mis values
	HashMap<String, Item> ItemHash = new HashMap<String, Item>();

	float N, SDC;

	List<ItemSet> F[];
	
	List<AssociationRule> ruleList = new ArrayList<AssociationRule>();

	public Logic(List<Item> itemList, float sdc, List<Transaction> transaction)
	{
		this.SDC = sdc;
		this.ItemList = itemList;
		this.TransactionList = transaction;
		Collections.sort(ItemList);

		this.startApriori();
	}
	
	public void startApriori(){
		
		System.out.println("Starting Apriori...\n");
		
		// give order number to the list
		for( int i = 0 ; i < ItemList.size() ; i++)
		{
			ItemList.get(i).setOrder(i);
		}
		
		N = this.TransactionList.size();
		F =  (List<ItemSet>[] )new ArrayList[(int)N];

		//populate the hash map
		for(Item item : this.ItemList)
		{
			ItemHash.put(item.getItemValue(), item);
		}

		List<Item> L = initPass();
		logger.debug("L (" + L.size() + ") = " + L + "\n");
		generateF1(L);
		logger.debug("F1 (" + F[0].size() + ") = " + F[0] + "\n");

		for ( int i = 1; !F[i-1].isEmpty(); i++ )
//		for ( int i = 1; i < 3 ; i++ )
		{
			if( i == 1)
			{
				generateLevelTwo(L);
			}
			else
			{
				generateOtherLevels(F[i-1],i);
			}

			pruneItemSetList(F[i]);
		}
		
		this.generateRules();
		
		this.printResult();
	}
	
	public void printResult(){
		// print the results
		try
		{
			File file = new File("result1.txt");
			if(file.exists())
			{
				file.delete();
			}

			file.createNewFile();

			FileWriter fw = new FileWriter(file);

			for( int i = 0 ; !F[i].isEmpty() ; i++)
			{
					fw.write("No. of length "+ (i+1) +" frequent itemsets: " + F[i].size());
					fw.write("\n");
					System.out.println("\nNo. of length "+ (i+1) +" frequent itemsets: " + F[i].size());

					for ( int j = 0 ; j < F[i].size() ; j++)
					{
						fw.write("{" + F[i].get(j).getItemSet() + "}: support-count=" +F[i].get(j).getCount());
						fw.write("\n");
						System.out.println("{" + F[i].get(j).getItemSet() + "}: support-count=" +F[i].get(j).getCount());
					}
					fw.write("\n");
			}
			
			System.out.println("\nNo. of rules generated: " + this.ruleList.size());
			fw.write("No. of rules generated: " + this.ruleList.size() + "\n");
			
			for(AssociationRule rule : this.ruleList){
				fw.write(rule.toString() + "\n");
				System.out.println(rule.toString());
			}
			
			fw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void generateRules(){
		
		System.out.println("\nGenerating association rules");
		
		for(int i = 1; i >= 1 && !(this.F[i] == null); i++){
			
			for(ItemSet itemset : this.F[i]){

				String[] items = itemset.getItemSet().split(",");

				for(int first = 0; first < items.length; first++){

					int rest = 0;
					AssociationRule rule = new AssociationRule();

					while(rest < items.length){
						if(rest == first)
							rest++;
						if(rest == items.length)
							break;

						//only first item is the consequence
						rule.setConsequence(items[first]);

						//rest forms the premise
						StringBuffer premise = new StringBuffer(items[rest]);
						for(int j = rest+1; j < items.length ; j++){
							if(j == first)
								continue;
							premise.append("," + items[j]);
						}

						rule.setPremise(premise.toString());

						float premiseCount = 0;

//						if(!rule.getPremise().equals(rule.getConsequence())){
						//get count from previous F set
						for(ItemSet previousItemSet : this.F[i-1]){
							if(previousItemSet.equals(new ItemSet(premise.toString(), 0))){
								premiseCount = previousItemSet.getCount();
								break;
							}
						}
						//add the rule only if confidence>SDC and if rule is not already added.
						if(premiseCount > 0 && !this.ruleList.contains(rule)
								&& itemset.getCount() / premiseCount >= this.SDC){
							
							this.ruleList.add(rule);
						}
						break;
//						}
					}
				}
			}
		}
	}
	
	// returns  list of j.count/n >= MIS (i)
	private List<Item> initPass()
	{
		System.out.println("Initial Pass");
		
		List<Item> L = new ArrayList<Item>();

		Item i = null;

		for(Item item : ItemList)
		{
			if(i == null)
			{
				if( item.getCount()/N >= item.getMis())
				{
					i = item;
					L.add(item);
				}
			}
			else
			{
				if( item.getCount()/N >= i.getMis() )
				{
					L.add(item);
				}
			}
		}
		return L;
	}

	private void pruneItemSetList(List<ItemSet> Flist)
	{
		System.out.println("\nPruning itemset list");
		
		Iterator<ItemSet> x= Flist.iterator();

		while(x.hasNext())
		{
			ItemSet temp = x.next();

			if(temp.getItemSet().equals("15"))
			{
				int sd = 0;
			}

			if(temp.getCount()/N < ItemHash.get(temp.getFirstItem()).getMis())
			{
				x.remove();
			}
		}
	}

	private void generateF1(List<Item> L)
	{
		System.out.println("F1 Generation");
		
		List<ItemSet> f1 = new ArrayList<ItemSet>();

		for(Item item : L)
		{
			ItemSet itemSet = new ItemSet(item.getItemValue(), item.getCount());
			f1.add(itemSet);
		}

		pruneItemSetList(f1);
		F[0] = f1;
	}

	private void generateLevelTwo(List<Item> oneLevel)
	{
		System.out.println("F2 Generation");
		
		List<ItemSet> two = new ArrayList<ItemSet>();
		
		for(int i = 0 ; i < oneLevel.size() ; i++)
//		for(int i = 0 ; i < 100 ; i++)
		{
			Item item = oneLevel.get(i);
			
			System.out.println("Adding item " + (i+1) + "... " + item.getItemValue());
			
			if(item.getCount() / N >= item.getMis())
			{
				for(int j = i+1 ; j < oneLevel.size() ; j++)
				{
					Item item2 = oneLevel.get(j);

					if( ( item2.getCount() / this.N >= item.getMis() )
							&& this.roundOff( (Math.abs( item2.getCount() / this.N - 
									item.getCount() / this.N ) ), 2)  <= this.SDC)
					{
						two.add(this.createNewItemSet(item.getItemValue() + "," + item2.getItemValue(), 0));
					}
				}
			}
		}
		F[1] = two;
	}

	private void generateOtherLevels(List<ItemSet> level, int levelNumber)
	{
		System.out.println("\nF" + (levelNumber+1) + " Generation");
		
		List<ItemSet> newLevel = new ArrayList<ItemSet>();

		for ( int i = 0 ; i < level.size() ; i++ )
		{
			ItemSet one = level.get(i);
			
			System.out.println("Adding item " + i + "... " + one);

			for ( int j = i+1 ; j < level.size() ; j++ )
			{
				ItemSet two = level.get(j);

				String first =  one.getItemSet().substring( 0, one.getItemSet().lastIndexOf(",") );

				String second = two.getItemSet().substring( 0, two.getItemSet().lastIndexOf(",") );

				if(first.compareTo(second) == 0)
				{
					String firstBreak[] = one.getItemSet().split(",");
					String secondBreak[] = two.getItemSet().split(",");

					if( roundOff( Math.abs(( ItemHash.get( firstBreak[firstBreak.length-1] ).getCount()/N ) - 
							ItemHash.get( secondBreak[secondBreak.length-1] ).getCount()/N ), 2 ) <= SDC )
					{

						if ( ItemHash.get(firstBreak[firstBreak.length-1]).getOrder() <  ItemHash.get(secondBreak[secondBreak.length-1]).getOrder() )
						{
							String newItemSet = one.getItemSet() + "," + secondBreak[secondBreak.length-1];

							if (checkNewItemSetValid(newItemSet, level))
							{
								newLevel.add(createNewItemSet(newItemSet, levelNumber));
							}
						}
						else
						{
							logger.error("this should not happen");
						}
					}
				}
			}
		}
		F[levelNumber] = newLevel;
	}

	public float roundOff(float value, int precision) {
		float p = (float)Math.pow(10,precision);
		float temp = Math.round(value * p);
		
		return (float)temp/p;
	}

	
	public ItemSet createNewItemSet(String newItemSet, int levelNumber)
	{
		ItemSet itemSet = null;
		int count = 0;

		for(Transaction transction : TransactionList)
		{
			if (transction.containsItemSet(newItemSet))
				count++;

			if(levelNumber > 0){
				ItemSet c1 = new ItemSet(newItemSet.substring(newItemSet.indexOf(",")), 0);
				for(ItemSet item : F[levelNumber-1]){
					if(item.getItemSet().equals(c1.getItemSet()))  //item.equals(c1);
						item.incrementCount();
				}
			}
		}
		itemSet = new ItemSet(newItemSet, count);

		return itemSet;
	}
		
	// check whether the new itemset is valid () line 8-11
	private boolean checkNewItemSetValid(String newItemSet,List<ItemSet> level)
	{
		boolean status = true;

		String item[] = newItemSet.split(",");

		for(int i = 0 ; i < item.length ; i++ )
		{
			StringBuffer s = new StringBuffer();

			for(int j = 0 ; j < item.length ; j++)
			{
				if(j != i)
				{
					if(s.length() == 0 )
					{
						s.append(item[j]);
					}
					else
						s.append("," + item[j]);
				}
			}
			if (  (i != 0) ||  ( ItemHash.get(item[0]).getMis() == ItemHash.get(item[1]).getMis()) )
			{
				ItemSet temp = new ItemSet(s.toString(), 0f);

				if( !level.contains(temp) )
				{
					status = false;
					break;
				}
			}
		}
		return status;
	}
}
