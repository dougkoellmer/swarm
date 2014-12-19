package swarm.client.view.cell;

public enum E_ImageLoadState
{
	NOT_SET,
	FAILED,
	QUEUED,
	LOADING,
	RENDERING,
	SHOULD_BE_RENDERED_BY_NOW,
	DEFINITELY_SHOULD_BE_RENDERED_BY_NOW;
}
