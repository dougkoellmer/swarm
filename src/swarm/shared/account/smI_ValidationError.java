package swarm.shared.account;

public interface smI_ValidationError
{
	boolean isError();
	
	boolean isEmptyError();
	
	String calcErrorText(smI_CredentialType credential);
	
	boolean isServerGeneratedError();
	
	boolean isRetryable();
}
