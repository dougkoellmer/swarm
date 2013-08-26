package swarm.server.data.blob;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import swarm.shared.utils.smU_TypeConversion;

public final class smU_Serialization
{
	private smU_Serialization()
	{
		
	}
	
	public static void writeNullableString(String string, ObjectOutput out) throws IOException
	{
		boolean isNull = string == null;
		out.writeBoolean(isNull);
		
		if( !isNull )
		{
			out.writeUTF(string);
		}
	}
	
	public static String readNullableString(ObjectInput in) throws IOException
	{
		boolean isNull = in.readBoolean();

		if( isNull )
		{
			return null;
		}
		else
		{
			return in.readUTF();
		}
	}
	
	public static void writeNullableObject(Serializable object, ObjectOutput out) throws IOException
	{
		boolean isNull = object == null;
		out.writeBoolean(isNull);
		
		if( !isNull )
		{
			if( object instanceof Externalizable )
			{
				((Externalizable)object).writeExternal(out);
			}
			else
			{
				out.writeObject(object);
			}
		}
	}
	
	public static <T> T readNullableObject(Class<? extends Serializable> T, ObjectInput in) throws IOException
	{
		boolean isNull = in.readBoolean();
		
		if( !isNull )
		{
			try
			{
				Serializable instance;

				if( Externalizable.class.isAssignableFrom(T) )
				{
					instance = T.newInstance();
					((Externalizable) instance).readExternal(in);
				}
				else
				{
					instance = (Serializable) in.readObject();
				}
				
				
				return (T) instance;
			}
			catch(Exception e)
			{
				throw new IOException(e);
			}
		}
		
		return null;
	}
	
	public static void writeNullableEnum(Enum enumValue, ObjectOutput out) throws IOException
	{
		boolean isNull = enumValue == null;
		out.writeBoolean(isNull);
		
		if( !isNull )
		{
			String enumName = smU_TypeConversion.convertEnumToString(enumValue);
			out.writeUTF(enumName);
		}
	}
	
	public static <T extends Enum> T readNullableEnum(T[] enumValues, ObjectInput in) throws IOException
	{
		boolean isNull = in.readBoolean();

		if( isNull )
		{
			return null;
		}
		else
		{
			String enumName = in.readUTF();
			
			T enumValue = smU_TypeConversion.convertStringToEnum(enumName, enumValues);
			
			if( enumValue == null )
			{
				throw new IOException("Couldn't resolve enum " + enumName + ".");
			}
			else
			{
				return enumValue;
			}
		}
	}
	
	public static <T extends Serializable>void writeJavaArry(T[] array, ObjectOutput out) throws IOException
	{
		int listSize = array.length;
		out.writeInt(listSize);
		for( int i = 0; i < listSize; i++ )
		{
			smU_Serialization.writeNullableObject(array[i], out);
		}
	}

	public static <T extends Serializable> T[] readJavaArray(Class<T> T, ObjectInput in) throws IOException
	{
		int listSize = in.readInt();
		T[] array = (T[]) Array.newInstance(T, listSize);
		
		for( int i = 0; i < listSize; i++ )
		{
			T ithObject = smU_Serialization.readNullableObject(T, in);
			array[i] = ithObject;
		}
		
		return array;
	}
	
	public static void writeArrayList(ArrayList<? extends Serializable> list, ObjectOutput out) throws IOException
	{
		int listSize = list.size();
		out.writeInt(listSize);
		for( int i = 0; i < listSize; i++ )
		{
			smU_Serialization.writeNullableObject(list.get(i), out);
		}
	}
	
	public static <T extends Serializable> void readArrayList(ArrayList<T> outList, Class<T> T, ObjectInput in) throws IOException
	{
		outList.clear();
		
		int listSize = in.readInt();
		for( int i = 0; i < listSize; i++ )
		{
			T ithObject = smU_Serialization.readNullableObject(T, in);
			outList.add(ithObject);
		}
	}
}
