package swarm.client.entities;

import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCode;

/**
 * ...
 * @author 
 */
public interface smI_BufferCellListener
{
	void onCellRecycled(int cellSize);
	
	void setCode(smCode code, String namespace);
	
	void showEmptyContent();
	
	void showLoading();
	
	void clearLoading();
	
	void onError(smE_CodeType eType);
	
	void onFocusGained();
	
	void onFocusLost();
}