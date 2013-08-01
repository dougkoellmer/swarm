package com.b33hive.shared.structs;

public class bhTuple <T, U>
{
	private final T m_first;
	private final U m_second;
	
	public bhTuple(T first, U second)
	{
		m_first = first;
		m_second = second;
	}
	
	public T getFirst()
	{
		return m_first;
	}
	
	public U getSecond()
	{
		return m_second;
	}
}
