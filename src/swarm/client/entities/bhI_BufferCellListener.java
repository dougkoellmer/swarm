package swarm.client.entities;

import swarm.shared.entities.bhE_CodeSafetyLevel;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.structs.bhCode;

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