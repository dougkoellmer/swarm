package swarm.client.view.alignment;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Element;


public class smU_Alignment
{
	public static void performAlignment(Element master, Element slave, smAlignmentDefinition alignment)
	{
		for(int i = 0; i < 2; i++ )
		{
			smE_AlignmentType slaveType = smE_AlignmentType.values()[i];
			smE_AlignmentType masterType = smE_AlignmentType.values()[i+2];
			
			smE_AlignmentPosition slaveAnchor = alignment.getPosition(slaveType);
			
			smAlignmentRect masterOverrideRect = alignment.getMasterRect();
			
			Object rectSource = masterOverrideRect != null ? masterOverrideRect : master;
			
			double masterAnchorPosition = getPosition(rectSource, i, alignment, masterType);
			
			double slaveDimension = getDimension(slave, i);
			double slaveAnchorPosition = 0;
			Double padding = alignment.getPadding(slaveType);
			padding = padding != null ? padding : 0;
			
			switch( slaveAnchor )
			{
				case LEFT_OR_TOP:		slaveAnchorPosition = masterAnchorPosition + padding;  break;
				case CENTER:			slaveAnchorPosition = masterAnchorPosition - slaveDimension/2 + padding;  break;
				case RIGHT_OR_BOTTOM:	slaveAnchorPosition = masterAnchorPosition - slaveDimension - padding;  break;
				case DEFINED:			slaveAnchorPosition = alignment.getDefinedPosition(slaveType) + padding;  break;
			}
			
			setPositionComponent(slave, i, slaveAnchorPosition);
		}
	}
	
	public static double getPosition(Object element, int componentIndex, smAlignmentDefinition alignment, smE_AlignmentType type)
	{
		smE_AlignmentPosition anchorPosition = alignment.getPosition(type);
		Double padding = alignment.getPadding(type);
		padding = padding != null ? padding : 0;
		
		switch(anchorPosition)
		{
			case LEFT_OR_TOP:		return getPosition(element, componentIndex) + padding;
			case CENTER:			return getPosition(element, componentIndex) + getDimension(element, componentIndex)/2 + padding;
			case RIGHT_OR_BOTTOM:	return getPosition(element, componentIndex) + getDimension(element, componentIndex) - padding;
			case DEFINED:			return alignment.getDefinedPosition(type) + padding;
		}
		
		return 0;
	}
	
	public static double getPosition(Object element, int componentIndex)
	{
		if( element instanceof smAlignmentRect )
		{
			return ((smAlignmentRect)element).getPositionComponent(componentIndex);
		}
		else
		{
			switch(componentIndex)
			{
				case 0:  return ((Element)element).getAbsoluteLeft();
				case 1:  return ((Element)element).getAbsoluteTop();
			}
		}
		
		return 0;
	}
	
	public static double getDimension(Object element, int componentIndex)
	{
		if( element instanceof smAlignmentRect )
		{
			return ((smAlignmentRect)element).getDimensionComponent(componentIndex);
		}
		else
		{
			switch(componentIndex)
			{
				case 0:  return ((Element)element).getClientWidth();
				case 1:  return ((Element)element).getClientHeight();
			}
		}
		
		return 0;
	}
	
	public static void setPositionComponent(Element element, int componentIndex, double position)
	{
		position = Math.round(position);
		
		switch(componentIndex)
		{
			case 0:  element.getStyle().setLeft(position, Unit.PX);		break;
			case 1:  element.getStyle().setTop(position, Unit.PX);		break;
		}
	}
	
	public static smAlignmentDefinition createHorRightVerCenter(double padding)
	{
		smAlignmentDefinition alignment = new smAlignmentDefinition();
		
		alignment.setPosition(smE_AlignmentType.MASTER_ANCHOR_VERTICAL, smE_AlignmentPosition.CENTER);
		alignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL, smE_AlignmentPosition.CENTER);
		alignment.setPosition(smE_AlignmentType.MASTER_ANCHOR_HORIZONTAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
		alignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, smE_AlignmentPosition.LEFT_OR_TOP);
		alignment.setPadding(smE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, padding);
		
		return alignment;
	}
}
