package com.b33hive.shared;

public final class bhU_BitTricks
{
	public static boolean isPowerOfTwo(int bits)
	{
		return ((bits & (bits - 1)) == 0);
	}
	
	public static int calcOrdinalBit(int ordinal)
	{
		return 0x1 << ordinal;
	}
	
	public static int calcBitPosition(int bit)
	{
		int r = 0; // r will be lg(v)

		while ((bit >>= 1) != 0)
		{
			r++;
		}
		
		return r;
	}
	
	//TODO: Can probably optimize these two methods with bitwise tricks.
	public static int calcMaskAfterBit(int bitPosition)
	{
		byte mask = 0x0;
		for( int i = bitPosition+1; i < 32; i++ )
		{
			mask |= (0x1 << i);
		}
		
		return mask;
	}
	public static int calcMaskBeforeBit(int bitPosition)
	{
		byte mask = 0x0;
		for( int i = bitPosition-1; i >= 0; i-- )
		{
			mask |= (0x1 << i);
		}
		
		return mask;
	}
	
	public static int calcUpperPowerOfTwo(int value)
	{
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		value++;
		
		return value;
	}
	
	public static int calcLowerPowerOfTwo(int value)
	{
		value = value | (value >> 1); 
		value = value | (value >> 2); 
		value = value | (value >> 4); 
		value = value | (value >> 8); 
		value = value | (value >> 16);
		
		return value - (value >> 1); 
	}
}
