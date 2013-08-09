package b33hive.client.ui.tabs.code;

import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhCamera;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.navigation.bhBrowserNavigator;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.structs.bhAccountInfo;
import b33hive.client.ui.cell.bhAlertManager;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhA_Grid;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhE_CellAddressParseError;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class bhU_CellApi
{
	private static final StateMachine_Camera.SnapToCoordinate.Args m_snapCoordArgs = new StateMachine_Camera.SnapToCoordinate.Args();
	private static final StateMachine_Camera.SnapToAddress.Args m_snapAddressArgs = new StateMachine_Camera.SnapToAddress.Args();
	private static final StateMachine_Camera.SetCameraTarget.Args m_snapPointArgs = new StateMachine_Camera.SetCameraTarget.Args();
	
	
	static native void registerApi(String namespace)
	/*-{
		
		function isInt(value)
		{ 
		    return !isNaN(parseInt(value,10)) && (parseFloat(value,10) == parseInt(value,10)); 
		}
		
		function isString(value)
		{
			return (typeof value) == "string";
		}
		
		function isObject(value)
		{
			return (typeof value) == "object";
		}
		
		function isNumeric(value)
		{
			return !isNaN(parseFloat(value)) && isFinite(value);
		}
		
		function isCoordinate()
		{
			if( arguments.length == 1 )
			{
				if( isObject(arguments[0]) )
				{
					return isInt(arguments[0].m) && isInt(arguments[0].n);
				}
			}
			else if( arguments.length == 2 && isInt(arguments[0]) && isInt(arguments[1]) )
			{
				return true;
			}
			
			return false;
		}
		
		function isPoint()
		{
			if( arguments.length == 1 )
			{
				if( isObject(arguments[0]) )
				{
					if( isNumeric(arguments[0].x) && isNumeric(arguments[0].y) )
					{
						if( ('z' in arguments[0]) == false )
						{
							arguments[0].z = 0.0;
							
							return true;
						}
						else
						{
							return isNumeric(arguments[0].z);
						}
					}
				}
			}
			else if( arguments.length == 2 && isNumeric(arguments[0]) && isNumeric(arguments[1]) )
			{
				return true;
			}
			else if( arguments.length == 3 && isNumeric(arguments[0]) && isNumeric(arguments[1]) && (isNumeric(arguments[2]) || arguments[2] === undefined) )
			{
				return true;
			}
			
			return false;
		}
		
		function string_contract(string)
		{
			if( arguments.length == 1 && isString(string) )
			{
				return true;
			}
			
			@b33hive.client.ui.tabs.code.bhU_CellApi::logError(Ljava/lang/String;)("Expected string value.");
			return false;
		}
		
		$wnd[namespace] = 
		{
			snap:function()
			{
				var argumentsLength = arguments.length;
				
				if( argumentsLength == 1 )
				{
					if( isString(arguments[0]) )
					{
						@b33hive.client.ui.tabs.code.bhU_CellApi::snapToAddress(Ljava/lang/String;)(arguments[0]);
					}
					else if( isInt(arguments[0]) )
					{
						@b33hive.client.ui.tabs.code.bhU_CellApi::snapToHistory(D)(arguments[0]);
					}
					else if( isCoordinate(arguments[0]) )
					{
						@b33hive.client.ui.tabs.code.bhU_CellApi::snapToCoordinate(DD)(arguments[0].m, arguments[0].n);
					}
					else if( isPoint(arguments[0]) )
					{
						@b33hive.client.ui.tabs.code.bhU_CellApi::snapToPoint(DDD)(arguments[0].x, arguments[0].y, arguments[0].z);
					}
				}
				else if( argumentsLength == 2 )
				{
					var args_modified = [arguments[0], arguments[1]];
					if( isCoordinate.apply(null, args_modified) )
					{
						@b33hive.client.ui.tabs.code.bhU_CellApi::snapToCoordinate(DD)(arguments[0], arguments[1]);
					}
					else if( isPoint.apply(null, args_modified) )
					{
						@b33hive.client.ui.tabs.code.bhU_CellApi::snapToPoint(DDD)(arguments[0], arguments[1], 0.0);
					}
				}
				else if( argumentsLength == 3 )
				{
					var args_modified = [arguments[0], arguments[1], arguments[2]];
					if( isPoint.apply(null, args_modified) )
					{
						@b33hive.client.ui.tabs.code.bhU_CellApi::snapToPoint(DDD)(arguments[0], arguments[1], arguments[2]);
					}
				}
			},			
			getCoordinate:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var m = @b33hive.client.ui.tabs.code.bhU_CellApi::getCoordinateM()();
				var n = @b33hive.client.ui.tabs.code.bhU_CellApi::getCoordinateN()();
				
				var coord = {m:m, n:n, toString:function(){return "{m:"+this.m+", n:"+this.n+"}"}};
				
				return coord;
			},
			getAddress:function()
			{
				var address = @b33hive.client.ui.tabs.code.bhU_CellApi::getAddress()();
				
				return address;
			},
			getPosition:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var x = @b33hive.client.ui.tabs.code.bhU_CellApi::getPositionX()();
				var y = @b33hive.client.ui.tabs.code.bhU_CellApi::getPositionY()();
				var z = @b33hive.client.ui.tabs.code.bhU_CellApi::getPositionZ()();
				
				var point = {x:x, y:y, z:z};
				
				return point;
			},
			getUsername:function()
			{
				var username = @b33hive.client.ui.tabs.code.bhU_CellApi::getUsername()();
				
				return username;
			},
			getGridWidth:function()
			{
				var width = @b33hive.client.ui.tabs.code.bhU_CellApi::getGridWidth()();
				
				return width;
			},
			getGridHeight:function()
			{
				var height = @b33hive.client.ui.tabs.code.bhU_CellApi::getGridHeight()();
				
				return height;
			}
		};
		
		$wnd[namespace+"_alert"] = function(message)
		{
			if( string_contract(message) )
			{
				@b33hive.client.ui.tabs.code.bhU_CellApi::alert(Ljava/lang/String;)(message);
			}
		}
		
	}-*/;
	
	private static void snapToHistory(double delta)
	{
		bh_c.navigator.getBrowserNavigator().go((int)delta);
	}
	
	private static void snapToAddress(String rawAddress)
	{
		if( rawAddress.equals("~") )
		{
			bhA_Action.perform(State_ViewingCell.Refresh.class);
			
			return;
		}
		
		bhCellAddress address = new bhCellAddress(rawAddress);
		if( address.getParseError() != bhE_CellAddressParseError.NO_ERROR )
		{
			logError("Invalid cell address format.");
			return;
		}
		
		m_snapAddressArgs.setAddress(address);
		bhA_Action.perform(StateMachine_Camera.SnapToAddress.class, m_snapAddressArgs);
	}
	
	private static void alert(String message)
	{
		bhAlertManager.getInstance().queue(message);
	}
	
	private static void snapToPoint(double x, double y, double z)
	{
		bhPoint point = new bhPoint(x, y, z);

		m_snapPointArgs.initialize(point, false);
		bhA_Action.perform(StateMachine_Camera.SetCameraTarget.class, m_snapPointArgs);
	}
	
	/*private static void snapToRelativePoint(double x, double y, double z)
	{
		bhPoint point = bh_c.camera.getPosition();
		
		snapToPoint(point.getX() + x, point.getY() + y, point.getZ() + z);
	}*/
	
	private static void snapToCoordinate(double m, double n)
	{
		bhGridCoordinate coordinate = new bhGridCoordinate((int)m, (int)n);
		
		bhA_Grid grid = bh_c.gridMngr.getGrid();
		
		if( !grid.isInBounds(coordinate) )
		{
			logError("Coordinates are out of bounds.");
			return;
		}
		
		m_snapCoordArgs.setCoordinate(coordinate);
		bhA_Action.perform(StateMachine_Camera.SnapToCoordinate.class, m_snapCoordArgs);
	}
	
	private static void snapToRelativeCoordinate(double m, double n)
	{
		bhBufferCell cell = getCurrentCell();
		snapToCoordinate(cell.getCoordinate().getM() + m, cell.getCoordinate().getN() + n);
	}
	
	private static int getCoordinateM()
	{
		bhBufferCell cell = getCurrentCell();

		return cell.getCoordinate().getM();
	}
	
	private static int getCoordinateN()
	{
		bhBufferCell cell = getCurrentCell();

		return cell.getCoordinate().getN();
	}
	
	private static bhPoint getPosition()
	{
		StateMachine_Camera machine = bhA_State.getEnteredInstance(StateMachine_Camera.class);
		
		return machine.getCamera().getPosition();
	}
	
	private static double getPositionX()
	{
		return getPosition().getX();
	}
	
	private static double getPositionY()
	{
		return getPosition().getY();
	}
	
	private static double getPositionZ()
	{
		return getPosition().getZ();
	}
	
	private static String getAddress()
	{
		return getCurrentCell().getCellAddress().getRawAddressNoLeadSlash();
	}
	
	private static String getUsername()
	{
		bhClientAccountManager accountManager = bh_c.accountMngr;
		
		bhAccountInfo accountInfo = accountManager.getAccountInfo();
		
		if( accountInfo == null )
		{
			return null;
		}
		else
		{
			return accountInfo.get(bhAccountInfo.Type.USERNAME);
		}
	}
	
	private static int getGridWidth()
	{
		return getCurrentCell().getGrid().getWidth();
	}
	
	private static int getGridHeight()
	{
		return getCurrentCell().getGrid().getHeight();
	}
	
	static State_ViewingCell getViewingState()
	{
		State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
		
		if( viewingState == null )
		{
			bhU_Debug.ASSERT(false, "Expected to be in viewing state for caja api- or uri-related action.");
		}
		
		return viewingState;
	}
	
	static bhBufferCell getCurrentCell()
	{
		return getViewingState().getCell();
	}
	
	private static native void logError(String message)
	/*-{
		console.log(message);
	}-*/;
}
