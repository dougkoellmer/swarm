package com.b33hive.client.entities;

import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.entities.bhA_Grid;
import com.b33hive.shared.structs.bhGridCoordinate;

/**
 * ...
 * @author 
 */
public class bhClientGrid extends bhA_Grid
{
	private static final bhClientGrid s_instance = new bhClientGrid();
	
	private bhClientGrid() 
	{
		
	}
	
	public static bhClientGrid getInstance()
	{
		return s_instance;
	}
}