package b33hive.shared.json;

public abstract class bhA_JsonObject implements bhI_JsonObject
{
	public boolean isEqualTo(bhI_JsonObject otherObject)
	{
		//--- DRK > TODO: Hopefully optimize this equality check somehow in the future.
		return otherObject.writeString().equals(this.writeString());
	}
}
