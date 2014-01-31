package swarm.server.code;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.commons.codec.binary.Base64;

import swarm.shared.lang.Boolean;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.I_SignUpCredentialValidator;
import swarm.shared.code.U_UriPolicy;
import swarm.shared.code.smUriData;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.E_NetworkPrivilege;

import com.google.api.server.spi.config.ApiNamespace;
import com.google.caja.lexer.ExternalReference;
import com.google.caja.plugin.LoaderType;
import com.google.caja.plugin.UriEffect;
import com.google.caja.plugin.UriFetcher;
import com.google.caja.plugin.UriPolicy;

public class SpecialUriPolicy implements UriPolicy
{
	private static final String ATTRIBUTE_HINT = "XML_ATTR";
	
	private static final String[] ALLOWED_IMAGE_TYPES =
	{
		"image/png",
		"image/gif",
		"image/jpeg"
	};
	
	private final E_NetworkPrivilege m_networkPrivilege;
	private Boolean m_foundB33hivePath = new Boolean();
	private final String m_apiNamespace;
	
	public SpecialUriPolicy(E_NetworkPrivilege networkPrivilege, String apiNamespace)
	{
		m_foundB33hivePath.value = false;
		m_networkPrivilege = networkPrivilege;
		m_apiNamespace = apiNamespace;
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
		
		smUriData uriData = new smUriData();
		uriData.attribute = attribute;
		uriData.authority = u.getUri().getAuthority();
		uriData.fullUri = u.getUri().toString();
		//uriData.isOpaque = u.getUri().isOpaque();
		uriData.path = u.getUri().getPath();
		uriData.scheme = u.getUri().getScheme();
		uriData.client = false;
		//uriData.schemeSpecificPart = u.getUri().getSchemeSpecificPart();
		
		Boolean bool_out = m_foundB33hivePath.value == true ? null : m_foundB33hivePath;
		String toReturn = U_UriPolicy.rewriteUri(m_networkPrivilege, uriData, m_apiNamespace, bool_out);
		
		return toReturn;
	}
}
