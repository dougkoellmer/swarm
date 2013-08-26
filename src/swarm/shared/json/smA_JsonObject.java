package swarm.shared.json;

public abstract class smA_JsonObject implements smI_JsonObject
{
	public boolean isEqualTo(smI_JsonObject otherObject)
	{
		//--- DRK > TODO: Hopefully optimize this equality check somehow in the future.
		return otherObject.writeString().equals(this.writeString());
	}
}
