package swarm.shared.json;

public abstract class A_JsonObject implements I_JsonObject
{
	public boolean isEqualTo(I_JsonObject otherObject)
	{
		//--- DRK > TODO: Hopefully optimize this equality check somehow in the future.
		return otherObject.writeString().equals(this.writeString());
	}
}
