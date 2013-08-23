package b33hive.client.app;

public enum bhE_StartUpStage
{
	CHECK_BROWSER_SUPPORT,
	CONFIGURE_LOGGING,
	LOAD_SUPPORT_LIBRARIES,
	START_APP_MANAGERS,
	START_VIEW_MANAGERS,
	REGISTER_STATES,
	ESTABLISH_TIMING,
	
	GUNSHOT_SOUND;
	
	public bhE_StartUpStage getNext()
	{
		if( this.ordinal()+1 < bhE_StartUpStage.values().length )
		{
			return bhE_StartUpStage.values()[this.ordinal()+1];
		}
		
		return null;
	}
}
