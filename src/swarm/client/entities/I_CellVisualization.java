package swarm.client.entities;

import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.Code;

/**
 * ...
 * @author 
 */
public interface I_CellVisualization
{
//	void onCellRecycled(int width, int height, int padding, int subCellDimension);
	
	void setCode(Code code, String namespace);
	
	void setCodeAfterFocusLost(Code code, String namespace);
	
	void showEmptyContent();
	
	void showLoading();
	
	void clearLoading();
	
	void onError(E_CodeType eType);
	
	void onFocusGained();
	
	void onFocusLost();
	
	boolean isLoaded();
	
	boolean isFullyDisplayed();
	
	void onSavedFromDeathSentence();
	
	void onRevealed();
}