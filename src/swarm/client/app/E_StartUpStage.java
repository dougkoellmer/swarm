package swarm.client.app;

public enum E_StartUpStage
{
	CHECK_BROWSER_SUPPORT,
	CONFIGURE_LOGGING,
	LOAD_SUPPORT_LIBRARIES,
	START_APP_MANAGERS,
	START_VIEW_MANAGERS,
	REGISTER_STATES,
	START_UPDATE_LOOP,
	
	GUNSHOT_SOUND;
	
	public E_StartUpStage getNext()
	{
		if( this.ordinal()+1 < E_StartUpStage.values().length )
		{
			return E_StartUpStage.values()[this.ordinal()+1];
		}
		
		return null;
	}
}
