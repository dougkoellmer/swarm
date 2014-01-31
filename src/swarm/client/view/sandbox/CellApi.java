package swarm.client.view.sandbox;

import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.Camera;
import swarm.client.entities.A_ClientUser;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.UserManager;
import swarm.client.navigation.BrowserNavigator;
import swarm.client.states.camera.Action_Camera_SnapToPoint;
import swarm.client.states.camera.Action_Camera_SnapToAddress;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.structs.AccountInfo;
import swarm.client.view.ViewContext;
import swarm.client.view.cell.AlertManager;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class CellApi
{
	private static final Action_Camera_SnapToCoordinate.Args m_snapCoordArgs = new Action_Camera_SnapToCoordinate.Args();
	private static final Action_Camera_SnapToAddress.Args m_snapAddressArgs = new Action_Camera_SnapToAddress.Args();
	private static final Action_Camera_SnapToPoint.Args m_snapPointArgs = new Action_Camera_SnapToPoint.Args();
	
	private final ViewContext m_viewContext;
	
	public CellApi(ViewContext viewContext)
	{
		m_viewContext = viewContext;
	}
	
	void registerApi(String apiNamespace)
	{
		registerApi_private(this, apiNamespace);
	}
	
	native void registerApi_private(CellApi thisArg, String namespace)
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
			
			thisArg.@swarm.client.view.sandbox.CellApi::logError(Ljava/lang/String;)("Expected string value.");
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
						thisArg.@swarm.client.view.sandbox.CellApi::snapToAddress(Ljava/lang/String;)(arguments[0]);
					}
					else if( isInt(arguments[0]) )
					{
						thisArg.@swarm.client.view.sandbox.CellApi::snapToHistory(D)(arguments[0]);
					}
					else if( isCoordinate(arguments[0]) )
					{
						thisArg.@swarm.client.view.sandbox.CellApi::snapToCoordinate(DD)(arguments[0].m, arguments[0].n);
					}
					else if( isPoint(arguments[0]) )
					{
						thisArg.@swarm.client.view.sandbox.CellApi::snapToPoint(DDD)(arguments[0].x, arguments[0].y, arguments[0].z);
					}
				}
				else if( argumentsLength == 2 )
				{
					var args_modified = [arguments[0], arguments[1]];
					if( isCoordinate.apply(null, args_modified) )
					{
						thisArg.@swarm.client.view.sandbox.CellApi::snapToCoordinate(DD)(arguments[0], arguments[1]);
					}
					else if( isPoint.apply(null, args_modified) )
					{
						thisArg.@swarm.client.view.sandbox.CellApi::snapToPoint(DDD)(arguments[0], arguments[1], 0.0);
					}
				}
				else if( argumentsLength == 3 )
				{
					var args_modified = [arguments[0], arguments[1], arguments[2]];
					if( isPoint.apply(null, args_modified) )
					{
						thisArg.@swarm.client.view.sandbox.CellApi::snapToPoint(DDD)(arguments[0], arguments[1], arguments[2]);
					}
				}
			},			
			getCoordinate:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var m = thisArg.@swarm.client.view.sandbox.CellApi::getCoordinateM()();
				var n = thisArg.@swarm.client.view.sandbox.CellApi::getCoordinateN()();
				
				var coord = {m:m, n:n, toString:function(){return "{m:"+this.m+", n:"+this.n+"}"}};
				
				return coord;
			},
			getAddress:function()
			{
				var address = thisArg.@swarm.client.view.sandbox.CellApi::getAddress()();
				
				return address;
			},
			getPosition:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var x = thisArg.@swarm.client.view.sandbox.CellApi::getPositionX()();
				var y = thisArg.@swarm.client.view.sandbox.CellApi::getPositionY()();
				var z = thisArg.@swarm.client.view.sandbox.CellApi::getPositionZ()();
				
				var point = {x:x, y:y, z:z};
				
				return point;
			},
			getUsername:function()
			{
				return thisArg.@swarm.client.view.sandbox.CellApi::getUsername()();
			},
			getGridWidth:function()
			{
				return thisArg.@swarm.client.view.sandbox.CellApi::getGridWidth()();
			},
			getGridHeight:function()
			{
				return thisArg.@swarm.client.view.sandbox.CellApi::getGridHeight()();
			},
			getCellWidth:function()
			{
				return thisArg.@swarm.client.view.sandbox.CellApi::getCellWidth()();
			},
			getCellHeight:function()
			{
				return thisArg.@swarm.client.view.sandbox.CellApi::getCellHeight()();
			},
			getCellPadding:function()
			{
				return thisArg.@swarm.client.view.sandbox.CellApi::getCellPadding()();
			}
		};
		
		$wnd[namespace+"_alert"] = function(message)
		{
			if( string_contract(message) )
			{
				thisArg.@swarm.client.view.sandbox.CellApi::alert(Ljava/lang/String;)(message);
			}
		}
	}-*/;
	
	private void snapToHistory(double delta)
	{
		m_viewContext.browserNavigator.go((int)delta);
	}
	
	private void snapToAddress(String rawAddress)
	{
		if( rawAddress.equals("~") )
		{
			m_viewContext.stateContext.performAction(Action_ViewingCell_Refresh.class);
			
			return;
		}
		
		CellAddress address = new CellAddress(rawAddress);
		if( address.getParseError() != E_CellAddressParseError.NO_ERROR )
		{
			logError("Invalid cell address format.");
			return;
		}
		
		m_snapAddressArgs.init(address);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToAddress.class, m_snapAddressArgs);
	}
	
	private void alert(String message)
	{
		m_viewContext.alertMngr.queue(message);
	}
	
	private void snapToPoint(double x, double y, double z)
	{
		Point point = new Point(x, y, z);

		m_snapPointArgs.init(point, false, true);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_snapPointArgs);
	}
	
	/*private static void snapToRelativePoint(double x, double y, double z)
	{
		smPoint point = sm_c.cameraMngr.getCamera().getPosition();
		
		snapToPoint(point.getX() + x, point.getY() + y, point.getZ() + z);
	}*/
	
	private void snapToCoordinate(double m, double n)
	{
		GridCoordinate coordinate = new GridCoordinate((int)m, (int)n);
		
		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
		
		if( !grid.isInBounds(coordinate) )
		{
			logError("Coordinates are out of bounds.");
			return;
		}
		
		m_snapCoordArgs.init(coordinate);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToCoordinate.class, m_snapCoordArgs);
	}
	
	private void snapToRelativeCoordinate(double m, double n)
	{
		BufferCell cell = getCurrentCell();
		snapToCoordinate(cell.getCoordinate().getM() + m, cell.getCoordinate().getN() + n);
	}
	
	private int getCoordinateM()
	{
		BufferCell cell = getCurrentCell();

		return cell.getCoordinate().getM();
	}
	
	private int getCoordinateN()
	{
		BufferCell cell = getCurrentCell();

		return cell.getCoordinate().getN();
	}
	
	private Point getPosition()
	{
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		return camera.getPosition();
	}
	
	private double getPositionX()
	{
		return getPosition().getX();
	}
	
	private double getPositionY()
	{
		return getPosition().getY();
	}
	
	private double getPositionZ()
	{
		return getPosition().getZ();
	}
	
	private String getAddress()
	{
		return getCurrentCell().getCellAddress().getRawAddress();
	}
	
	private String getUsername()
	{
		ClientAccountManager accountManager = m_viewContext.appContext.accountMngr;
		
		AccountInfo accountInfo = accountManager.getAccountInfo();
		
		if( accountInfo == null )
		{
			return null;
		}
		else
		{
			return accountInfo.get(AccountInfo.Type.USERNAME);
		}
	}
	
	private int getGridWidth()
	{
		return getCurrentCell().getGrid().getWidth();
	}
	
	private int getGridHeight()
	{
		return getCurrentCell().getGrid().getHeight();
	}
	
	private int getCellWidth()
	{
		return getCurrentCell().getGrid().getCellWidth();
	}
	
	private int getCellHeight()
	{
		return getCurrentCell().getGrid().getCellHeight();
	}
	
	private int getCellPadding()
	{
		return getCurrentCell().getGrid().getCellPadding();
	}
	
	State_ViewingCell getViewingState()
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		
		if( viewingState == null )
		{
			U_Debug.ASSERT(false, "Expected to be in viewing state for caja api- or uri-related action.");
		}
		
		return viewingState;
	}
	
	BufferCell getCurrentCell()
	{
		return getViewingState().getCell();
	}
	
	private native void logError(String message)
	/*-{
		console.log(message);
	}-*/;
}
