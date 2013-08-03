package b33hive.client.entities;

import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCode;

/**
 * ...
 * @author 
 */
public interface bhI_BufferCellListener
{
	void onCellRecycled(int cellSize);
	
	void setCode(bhCode code, String namespace);
	
	void showEmptyContent();
	
	void showLoading();
	
	void clearLoading();
	
	void onError(bhE_CodeType eType);
	
	void onFocusGained();
	
	void onFocusLost();
}