package swarm.server.code;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public class bhNodeWrapper extends org.apache.xerces.dom.ElementNSImpl
{
	private final String m_overrideName;
	private final Node m_inner;
	
	public bhNodeWrapper(Node inner, String overrideName)
	{
		m_inner = inner;
		m_overrideName = overrideName;
	}
	
	@Override
	public Node appendChild(Node arg0) throws DOMException {
		return m_inner.appendChild(arg0);
	}

	@Override
	public Node cloneNode(boolean arg0) {
		return m_inner.cloneNode(arg0);
	}

	@Override
	public short compareDocumentPosition(Node arg0) throws DOMException {
		return m_inner.compareDocumentPosition(arg0);
	}

	@Override
	public NamedNodeMap getAttributes() {
		return m_inner.getAttributes();
	}

	@Override
	public String getBaseURI() {
		return m_inner.getBaseURI();
	}

	@Override
	public NodeList getChildNodes() {
		return m_inner.getChildNodes();
	}

	@Override
	public Object getFeature(String arg0, String arg1) {
		return m_inner.getFeature(arg0, arg1);
	}

	@Override
	public Node getFirstChild() {
		return m_inner.getFirstChild();
	}

	@Override
	public Node getLastChild() {
		return m_inner.getLastChild();
	}

	@Override
	public String getLocalName() {
		return m_overrideName;
	}

	@Override
	public String getNamespaceURI() {
		return m_inner.getNamespaceURI();
	}

	@Override
	public Node getNextSibling() {
		return m_inner.getNextSibling();
	}

	@Override
	public String getNodeName() {
		return m_overrideName;
	}

	@Override
	public short getNodeType() {
		return m_inner.getNodeType();
	}

	@Override
	public String getNodeValue() throws DOMException {
		return m_inner.getNodeValue();
	}

	@Override
	public Document getOwnerDocument() {
		return m_inner.getOwnerDocument();
	}

	@Override
	public Node getParentNode() {
		return m_inner.getParentNode();
	}

	@Override
	public String getPrefix() {
		return m_inner.getPrefix();
	}

	@Override
	public Node getPreviousSibling() {
		return m_inner.getPreviousSibling();
	}

	@Override
	public String getTextContent() throws DOMException {
		return m_inner.getTextContent();
	}

	@Override
	public Object getUserData(String arg0) {
		return m_inner.getUserData(arg0);
	}

	@Override
	public boolean hasAttributes() {
		return m_inner.hasAttributes();
	}

	@Override
	public boolean hasChildNodes() {
		return m_inner.hasChildNodes();
	}

	@Override
	public Node insertBefore(Node arg0, Node arg1) throws DOMException {
		return m_inner.insertBefore(arg0, arg1);
	}

	@Override
	public boolean isDefaultNamespace(String arg0) {
		return m_inner.isDefaultNamespace(arg0);
	}

	@Override
	public boolean isEqualNode(Node arg0) {
		return m_inner.isEqualNode(arg0);
	}

	@Override
	public boolean isSameNode(Node arg0) {
		return m_inner.isSameNode(arg0);
	}

	@Override
	public boolean isSupported(String arg0, String arg1) {
		return m_inner.isSupported(arg0, arg1);
	}

	@Override
	public String lookupNamespaceURI(String arg0) {
		return m_inner.lookupNamespaceURI(arg0);
	}

	@Override
	public String lookupPrefix(String arg0) {
		return m_inner.lookupPrefix(arg0);
	}

	@Override
	public void normalize() {
		m_inner.normalize();
	}

	@Override
	public Node removeChild(Node arg0) throws DOMException {
		return m_inner.removeChild(arg0);
	}

	@Override
	public Node replaceChild(Node arg0, Node arg1) throws DOMException {
		return m_inner.replaceChild(arg0, arg1);
	}

	@Override
	public void setNodeValue(String arg0) throws DOMException {
		m_inner.setNodeValue(arg0);
	}

	@Override
	public void setPrefix(String arg0) throws DOMException {
		m_inner.setPrefix(arg0);
	}

	@Override
	public void setTextContent(String arg0) throws DOMException {
		m_inner.setTextContent(arg0);
	}

	@Override
	public Object setUserData(String arg0, Object arg1, UserDataHandler arg2) {
		return m_inner.setUserData(arg0, arg1, arg2);
	}
}
