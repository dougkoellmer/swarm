package b33hive.server.transaction;

import b33hive.shared.transaction.bhE_HttpMethod;
import b33hive.shared.transaction.bhI_RequestPath;

public enum bhE_AdminRequestPath implements bhI_RequestPath
{
	createGrid,
	setEditingPermission,
	deactivateUserCells,
	refreshHomeCells,
	clearCell,
	recompileCells;
	
	private final bhE_HttpMethod m_method;
	
	private bhE_AdminRequestPath()
	{
		m_method = bhE_HttpMethod.POST;
	}
	
	@Override
	public bhE_HttpMethod getDefaultMethod()
	{
		return m_method;
	}

	@Override
	public int getId()
	{
		return this.ordinal() + 1000000;
	}

	@Override
	public String getName()
	{
		return this.name();
	}
}
