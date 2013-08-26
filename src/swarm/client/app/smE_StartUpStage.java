package swarm.client.app;

public enum smE_StartUpStage
{
	CHECK_BROWSER_SUPPORT,
	CONFIGURE_LOGGING,
	LOAD_SUPPORT_LIBRARIES,
	START_APP_MANAGERS,
	START_VIEW_MANAGERS,
	REGISTER_STATES,
	ESTABLISH_TIMING,
	
	GUNSHOT_SOUND;
	
	public smE_StartUpStage getNext()
	{
		if( this.ordinal()+1 < smE_StartUpStage.values().length )
		{
			return smE_StartUpStage.values()[this.ordinal()+1];
		}
		
		return null;
	}
}
