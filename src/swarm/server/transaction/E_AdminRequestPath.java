package swarm.server.transaction;

import swarm.shared.transaction.E_HttpMethod;
import swarm.shared.transaction.E_RequestPathBlock;
import swarm.shared.transaction.I_RequestPath;

public enum E_AdminRequestPath implements I_RequestPath
{
	createGrid,
	setEditingPermission,
	deactivateUserCells,
	refreshHomeCells,
	clearCell,
	recompileCells,
	deleteHomeCells;
	
	private final E_HttpMethod m_method;
	
	private E_AdminRequestPath()
	{
		m_method = E_HttpMethod.POST;
	}
	
	@Override
	public E_HttpMethod getDefaultMethod()
	{
		return m_method;
	}

	@Override
	public int getId()
	{
		return E_RequestPathBlock.LIB_ADMIN.getPathId(this);
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
