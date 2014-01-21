package swarm.server.transaction;

import swarm.shared.transaction.smE_HttpMethod;
import swarm.shared.transaction.smE_RequestPathBlock;
import swarm.shared.transaction.smI_RequestPath;

public enum smE_AdminRequestPath implements smI_RequestPath
{
	createGrid,
	setEditingPermission,
	deactivateUserCells,
	refreshHomeCells,
	clearCell,
	recompileCells,
	deleteHomeCells;
	
	private final smE_HttpMethod m_method;
	
	private smE_AdminRequestPath()
	{
		m_method = smE_HttpMethod.POST;
	}
	
	@Override
	public smE_HttpMethod getDefaultMethod()
	{
		return m_method;
	}

	@Override
	public int getId()
	{
		return smE_RequestPathBlock.LIB_ADMIN.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
