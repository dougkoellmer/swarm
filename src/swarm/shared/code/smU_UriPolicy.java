package swarm.shared.code;

import swarm.shared.lang.smBoolean;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smE_CellAddressParseError;
import swarm.shared.structs.smE_NetworkPrivilege;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public final class smU_UriPolicy
{
	public static final String ATTRIBUTE_HINT = "XML_ATTR";
	
	private static final String[] ALLOWED_IMAGE_TYPES =
	{
		"image/png",
		"image/gif",
		"image/jpeg"
	};
	
	/**
	 * From http://tools.ietf.org/html/rfc2397
	 *   dataurl    := "data:" [ mediatype ] [ ";base64" ] "," data
	 *   mediatype  := [ type "/" subtype ] *( ";" parameter )
	 *   data       := *urlchar
	 *   parameter  := attribute "=" value
	 */
	
	private enum DATA_URI { ALL, TYPE, BASE64, DATA; }
	
	private static final String DATA_URI_DEFAULT_CHARSET = "US-ASCII";
	
	private static boolean isImageDataUri(String schemeSpecificPart)
	{
		RegExp regExp = RegExp.compile("([^,]*?)(;base64)?,(.*)", "mi");
		MatchResult result = regExp.exec(schemeSpecificPart);
		
		if (result != null )
		{
			String mimeType = result.getGroup(DATA_URI.TYPE.ordinal());
			for( int i = 0; i < ALLOWED_IMAGE_TYPES.length; i++ )
			{
				if( ALLOWED_IMAGE_TYPES[i].equals(mimeType) )
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static boolean isDataUri(String scheme, boolean isOpaque)
	{
		return scheme != null && scheme.equals("data") && isOpaque;
	}
	
	public static String rewriteUri(smE_NetworkPrivilege privilege, smUriData uriData, smBoolean foundB33hivePath_out_nullable)
	{
		if( foundB33hivePath_out_nullable != null )
		{
			foundB33hivePath_out_nullable.value = false;
		}
		
		if( uriData.attribute.equals("img::src") )
		{
			/*if( isDataUri(uriData.scheme, uriData.isOpaque) )
			{
				if( isImageDataUri(uriData.schemeSpecificPart) )
				{
					String uri = uriData.scheme + ":" + uriData.schemeSpecificPart;
					return uri;
				}
			}*/

			return uriData.fullUri;
		}
		
		else if( uriData.attribute.equals("a::href") )
		{
			String b33hivePath = getB33hivePath(uriData);
			
			if( b33hivePath != null )
			{
				if( foundB33hivePath_out_nullable != null )
				{
					foundB33hivePath_out_nullable.value = true;
				}
				
				if( uriData.client )
				{
					return bhU_Code.transformPathToJavascript(b33hivePath);
				}
				else
				{
					return b33hivePath;
				}
			}
			else
			{
				if( "mailto".equals(uriData.scheme) || "http".equals(uriData.scheme) || "https".equals(uriData.scheme) )
				{
					return uriData.fullUri;
				}
			}
		}
		
		if( privilege == smE_NetworkPrivilege.ALL )
		{
			return uriData.fullUri;
		}

		return null;
	}
	
	private static String getB33hivePath(smUriData uriData)
	{
		if( uriData.scheme == null && uriData.authority == null )
		{
			smCellAddress address = new smCellAddress(uriData.path);
			
			if( address.getParseError() == smE_CellAddressParseError.NO_ERROR )
			{
				return address.getRawAddress();
			}
		}
		
		return null;
	}
}
