package swarm.shared.account;

public interface I_ValidationError
{
	boolean isError();
	
	boolean isEmptyError();
	
	String calcErrorText(I_CredentialType credential);
	
	boolean isServerGeneratedError();
	
	boolean isRetryable();
}
