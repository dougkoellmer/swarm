package swarm.server.code;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.commons.codec.binary.Base64;

import swarm.shared.lang.bhBoolean;
import swarm.shared.account.bhE_SignUpValidationError;
import swarm.shared.account.bhI_SignUpCredentialValidator;
import swarm.shared.code.bhU_UriPolicy;
import swarm.shared.code.bhUriData;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhE_CellAddressParseError;
import swarm.shared.structs.bhE_NetworkPrivilege;
import com.google.caja.lexer.ExternalReference;
import com.google.caja.plugin.LoaderType;
import com.google.caja.plugin.UriEffect;
import com.google.caja.plugin.UriFetcher;
import com.google.caja.plugin.UriPolicy;

public class bhUriPolicy implements UriPolicy
{
	private static final String ATTRIBUTE_HINT = "XML_ATTR";
	
	private static final String[] ALLOWED_IMAGE_TYPES =
	{
		"image/png",
		"image/gif",
		"image/jpeg"
	};
	
	private final bhE_NetworkPrivilege m_networkPrivilege;
	private bhBoolean m_foundB33hivePath = new bhBoolean();
	
	public bhUriPolicy(bhE_NetworkPrivilege networkPrivilege)
	{
		m_foundB33hivePath.value = false;
		m_networkPrivilege = networkPrivilege;
	}
	
	public boolean foundB33hivePath()
	{
		return m_foundB33hivePath.value;
	}
	
	@Override
	public String rewriteUri(ExternalReference u, UriEffect effect, LoaderType loader, Map<String, ?> hints)
	{
		Object attributeObject = (String) hints.get(ATTRIBUTE_HINT);
		
		if( attributeObject == null || !(attributeObject instanceof String) )
		{
			return null;
		}
		
		String attribute = (String) attributeObject;
		
		bhUriData uriData = new bhUriData();
		uriData.attribute = attribute;
		uriData.authority = u.getUri().getAuthority();
		uriData.fullUri = u.getUri().toString();
		//uriData.isOpaque = u.getUri().isOpaque();
		uriData.path = u.getUri().getPath();
		uriData.scheme = u.getUri().getScheme();
		uriData.client = false;
		//uriData.schemeSpecificPart = u.getUri().getSchemeSpecificPart();
		
		bhBoolean bool_out = m_foundB33hivePath.value == true ? null : m_foundB33hivePath;
		String toReturn = bhU_UriPolicy.rewriteUri(m_networkPrivilege, uriData, bool_out);
		
		return toReturn;
	}
}
