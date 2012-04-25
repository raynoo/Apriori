package edu.dm.object;

import java.util.Set;

public class Transaction
{
	Set<String> trans;
	
	public Transaction(Set<String> trans) 
	{
		this.trans = trans;
	}
	
	public boolean containsItemSet(String itemSet)
	{
		boolean status = true;
		
		String items[] = itemSet.split(",");
		
		for(int i = 0 ; i < items.length ; i++)
		{
			if( !trans.contains(items[i]))
			{
				status = false;
				break;
			}
		}
		return status;
	}
}
