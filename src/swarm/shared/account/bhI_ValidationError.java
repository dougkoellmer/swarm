package swarm.shared.account;

public interface bhI_ValidationError
{
	boolean isError();
	
	boolean isEmptyError();
	
	String calcErrorText(bhI_CredentialType credential);
	
	boolean isServerGeneratedError();
	
	boolean isRetryable();
}
