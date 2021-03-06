package swarm.server.code;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import swarm.server.structs.ServerCode;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.I_SignUpCredentialValidator;
import swarm.shared.app.S_CommonApp;
import swarm.shared.code.A_CodeCompiler;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.CompilerMessage;
import swarm.shared.code.E_CompilationStatus;
import swarm.shared.code.E_CompilerMessageLevel;
import swarm.shared.code.FileRange;
import swarm.shared.code.U_Code;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.Code;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.E_NetworkPrivilege;
import swarm.shared.thirdparty.S_Caja;

import com.google.caja.SomethingWidgyHappenedError;
import com.google.caja.config.ConfigUtil;
import com.google.caja.lang.html.HtmlSchema;
import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.ExternalReference;
import com.google.caja.lexer.FetchedData;
import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.ParseException;
import com.google.caja.lexer.TokenConsumer;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParseTreeNodeVisitor;
import com.google.caja.parser.ParserContext;
import com.google.caja.parser.html.Dom;
import com.google.caja.parser.html.ElKey;
import com.google.caja.parser.html.Nodes;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.CajoledModule;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.ReturnStmt;
import com.google.caja.parser.js.Statement;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.plugin.DataUriFetcher;
import com.google.caja.plugin.LoaderType;
import com.google.caja.plugin.PipelineMaker;
import com.google.caja.plugin.PluginCompiler;
import com.google.caja.plugin.PluginMeta;
import com.google.caja.plugin.UriEffect;
import com.google.caja.plugin.UriFetcher;
import com.google.caja.plugin.UriPolicy;
import com.google.caja.plugin.UriFetcher.UriFetchException;
import com.google.caja.reporting.BuildInfo;
import com.google.caja.reporting.MarkupRenderMode;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.RenderContext;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.util.ContentType;
import com.google.caja.util.Pair;

public class ServerCodeCompiler extends A_CodeCompiler
{
	private static final class BlankMessagePart implements MessagePart
	{
		@Override
		public void format(MessageContext context, Appendable out) throws IOException
		{
			out.append("");
		}
	}
	
	private static BlankMessagePart s_blankMessagePart = new BlankMessagePart();
	
	private static final Logger s_logger = Logger.getLogger(ServerCodeCompiler.class.getName());
	
	private Pair<HtmlSchema, List<Message>> m_customHtmlSchema;
	
	private final boolean m_formatSourceHtml;
	
	public ServerCodeCompiler(boolean formatSourceHtml)
	{
		super();
		
		m_formatSourceHtml = formatSourceHtml;
	}

	private synchronized void createHtmlSchema()
	{
		//MessageQueue dummyQueue = new SimpleMessageQueue();
		if( m_customHtmlSchema == null )
		{
			SimpleMessageQueue cacheMq = new SimpleMessageQueue();
			URI elSrc = URI.create(
					"resource:///swarm/server/code/"
							+ "htmlall-elements.json");
			URI attrSrc = URI.create(
					"resource:///swarm/server/code/"
							+ "htmlall-attributes.json");
			try
			{
				m_customHtmlSchema = Pair.pair(
						new HtmlSchema(
								ConfigUtil.loadWhiteListFromJson(
										elSrc, ConfigUtil.RESOURCE_RESOLVER, cacheMq),
										ConfigUtil.loadWhiteListFromJson(
												attrSrc, ConfigUtil.RESOURCE_RESOLVER, cacheMq)),
												cacheMq.getMessages());
				// If the default schema is borked, there's not much we can do.
			} catch (IOException ex) {
				//dummyQueue.getMessages().addAll(cacheMq.getMessages());
				throw new SomethingWidgyHappenedError("Default schema is borked", ex);
			} catch (ParseException ex) {
				cacheMq.getMessages().add(ex.getCajaMessage());
				//dummyQueue.getMessages().addAll(cacheMq.getMessages());
				throw new SomethingWidgyHappenedError("Default schema is borked", ex);
			}
		}
	}
	
	private HtmlSchema getHtmlSchema()
	{
		if (m_customHtmlSchema == null)
		{
			this.createHtmlSchema();
		}
		
		//mq.getMessages().addAll(m_customHtmlSchema.b);
		
		return m_customHtmlSchema.a;
	}
	
	protected CompilerResult createResult()
	{
		return new CompilerResult();
	}
	
	@Override
	public CompilerResult compile(Code sourceCode, CodePrivileges privileges, String cellNamespace, String apiNamespace)
	{
		cellNamespace += S_Caja.CAJA_NAMESPACE_SUFFIX;
		
		CompilerResult result = (CompilerResult) super.compile(sourceCode, privileges, cellNamespace, apiNamespace);
		
		if( result.getStatus() != E_CompilationStatus.NO_ERROR )
		{
			return result;
		}
		
		DataUriFetcher fetcher = new DataUriFetcher()
		{
			@Override
			 public FetchedData fetch(ExternalReference ref, String mimeType) throws UriFetchException
			 {
				return super.fetch(ref, mimeType);
			 }
		};
		
		SpecialUriPolicy uriPolicy = new SpecialUriPolicy(privileges.getNetworkPrivilege(), apiNamespace);
		
		PluginMeta meta = new PluginMeta(fetcher, uriPolicy);
		meta.setIdClass(cellNamespace);
		//meta.setPrecajoleMinify(true); // not sure what it does, but sounds like extra CPU to minify, which we don't care about
		MessageQueue messageQueue = new SimpleMessageQueue();

		Dom htmlDom = null;
		try
		{
			ParserContext parserContext = new ParserContext(messageQueue);
			
			htmlDom = (Dom) parserContext
			.withInput(sourceCode.getRawCode())
			.withInput(ContentType.HTML)
			.build();
		}
		catch (Throwable e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			return result.onFailure(E_CompilationStatus.COMPILER_EXCEPTION);
		}
		
		if( m_formatSourceHtml )
		{
			String sourceCodeFormatted = Nodes.renderUnsafe(htmlDom.getValue(), MarkupRenderMode.HTML);
			result.setSource(new ServerCode(sourceCodeFormatted, E_CodeType.SOURCE));
		}
		else
		{
			result.setSource(sourceCode);
		}
		
		SpecialHtmlSchema schema = new SpecialHtmlSchema(this.getHtmlSchema());
		HtmlPreProcessor preProcessor = new HtmlPreProcessor(htmlDom, schema);
		
		boolean hasSplashOnlyContent = preProcessor.hasSplashTag();
		Dom noScriptHtmlDom = htmlDom;
		if( hasSplashOnlyContent )
		{
			htmlDom = htmlDom.clone();
		}
	
		PluginCompiler compiler = compile(htmlDom, result, meta, schema, messageQueue);
		
		if( compiler == null )  return result;
		
		boolean hasJavaScript = hasJavaScript(compiler, preProcessor);
		boolean foundB33hivePath = uriPolicy.foundB33hivePath();
		
		Node staticHtmlDocument = compiler.getStaticHtml();
		if( !hasJavaScript && staticHtmlDocument != null && foundB33hivePath )
		{
			transformDom(staticHtmlDocument, apiNamespace);
		}
		
		String staticHtml = staticHtmlDocument != null ? Nodes.render(staticHtmlDocument) : "";
		
		if( hasSplashOnlyContent )
		{
			preProcessor.injectSplashTag();
			schema.setToNoScriptMode();
			PluginCompiler splashCompiler = compile(noScriptHtmlDom, result, meta, schema, messageQueue);
			
			if( splashCompiler == null )  return result;
			
			//--- DRK > Create splash code.
			Node splashHtmlDocument = splashCompiler.getStaticHtml();
			String splashHtml = splashHtmlDocument != null ? Nodes.render(splashHtmlDocument) : "";
			ServerCode splashCode = new ServerCode(splashHtml, E_CodeType.SPLASH);
			splashCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_STATIC_SANDBOX);
			
			ServerCode compiledCode;
			
			if( !hasJavaScript)
			{
				compiledCode = new ServerCode(staticHtml, E_CodeType.COMPILED);
				compiledCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_STATIC_SANDBOX);
			}
			else
			{
				//TODO: Should probably parse the raw source html here instead of regexing it to remove splash tags.
				//		The problem is, this requires *yet another* parse of the string into a DOM, then *yet another*
				//		traversal to remove all splash tags. We could clone the original DOM made from the source, but
				//		then we'd always be incurring that overhead when for the majority of cases we probably don't need it,
				//		if we consider the user having splash code to be a <50% occurrence.
				String virtualDynamicCode = sourceCode.getRawCode();
				virtualDynamicCode = virtualDynamicCode.replaceAll("(?s)\\<splash>.*\\</splash>", "");
				
				compiledCode = new ServerCode(virtualDynamicCode, E_CodeType.COMPILED);
				compiledCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX);
			}
			
			return result.onSuccess(splashCode, compiledCode);
		}
		else
		{
			if( !hasJavaScript)
			{
				ServerCode splashCode = new ServerCode(staticHtml, E_CodeType.SPLASH, E_CodeType.COMPILED);
				splashCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_STATIC_SANDBOX);
				
				return result.onSuccess(splashCode);
			}
			else
			{
				ServerCode splashCode = new ServerCode(staticHtml, E_CodeType.SPLASH);
				splashCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_STATIC_SANDBOX);
				
				ServerCode compiledCode = new ServerCode(sourceCode.getRawCode(), E_CodeType.COMPILED);
				compiledCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX);
				
				//TODO: May minify compiled code in future, maybe as an optional step.
				
				return result.onSuccess(splashCode, compiledCode);
			}
		}
	}
	
	private static boolean hasJavaScript(PluginCompiler compiler, HtmlPreProcessor preProcessor)
	{
		ParseTreeNode jsModule = compiler.getJavascript();
		boolean isEmptyJsModule = isEmptyModule(jsModule);
		return !isEmptyJsModule || preProcessor.foundJavaScript();
	}
	
	private static PluginCompiler compile(Dom htmlDom, CompilerResult result_out, PluginMeta meta, HtmlSchema schema, MessageQueue messageQueue)
	{
		PluginCompiler compiler = new PluginCompiler(BuildInfo.getInstance(), meta, messageQueue);
		compiler.setHtmlSchema(schema);
		
		try
		{
			compiler.addInput(htmlDom, new URI(""));
		}
		catch (URISyntaxException e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			result_out.onFailure(E_CompilationStatus.COMPILER_EXCEPTION);
			
			return null;
		}
		
		boolean successfulCompile = compiler.run();
		
		boolean foundErrorsInMessageQueue = false;
		MessageLevel lowestReportingLevel = MessageLevel.FATAL_ERROR;
		MessageLevel highestAllowableLevel = MessageLevel.WARNING;
		
		List<Message> messages = messageQueue.getMessages();
		for( int i = 0; i < messages.size(); i++ )
		{
			Message message = messages.get(i);
			
			if( message.getMessageLevel().ordinal() >= lowestReportingLevel.ordinal() )
			{
				CompilerMessage compilerError = createMessage(message);
				
				result_out.addMessage(compilerError);
				
				if(  message.getMessageLevel().ordinal() > highestAllowableLevel.ordinal() )
				{
					foundErrorsInMessageQueue = true;
				}
			}
			
			//s_logger.severe(message.getMessageLevel() + ": " + message.toString());
		}
		
		if( !foundErrorsInMessageQueue )
		{
			if( successfulCompile )
			{
				return compiler;
			}
			else
			{
				s_logger.severe("Compiler::run() failed, but no error messages were found.");
				
				result_out.onFailure(E_CompilationStatus.COMPILER_EXCEPTION);
			}
		}
		else
		{
			// result has compiler errors inside already.
		}
		
		return null;
	}
	
	private static CompilerMessage createMessage(Message message)
	{
		FileRange range = null;
		String formattedMessage = null;
		E_CompilerMessageLevel level = null;
		
		if( message.getMessageLevel().ordinal() <= MessageLevel.LINT.ordinal() )
		{
			level = E_CompilerMessageLevel.LINT;
		}
		else if( message.getMessageLevel().ordinal() <= MessageLevel.WARNING.ordinal() )
		{
			level = E_CompilerMessageLevel.WARNING;
		}
		else
		{
			level = E_CompilerMessageLevel.ERROR;
		}
		
		List<MessagePart> parts = message.getMessageParts();
		MessagePart[] modParts = new MessagePart[parts.size()];
		
		for( int i = 0; i < parts.size(); i++ )
		{
			MessagePart part = parts.get(i);
			if( part instanceof FilePosition )
			{
				FilePosition filePosition = (FilePosition) part;
				range = new FileRange
				(
					filePosition.startLineNo()-1,
					filePosition.startCharInLine(),
					filePosition.endLineNo()-1,
					filePosition.endCharInLine()
				);
				
				modParts[i] = s_blankMessagePart;
			}
			else
			{
				modParts[i] = part;
			}
		}
		
		Message customMessage = new Message(message.getMessageType(), modParts);
		
		formattedMessage = customMessage.toString();
		formattedMessage = formattedMessage.replaceFirst(":", "");
		
		formattedMessage = StringEscapeUtils.escapeHtml4(formattedMessage);
		
		return new CompilerMessage(level, formattedMessage, range);
	}
	
	private static void printMessages(MessageQueue mq)
	{
		List<Message> messages = mq.getMessages();
		for( int i = 0; i < messages.size(); i++ )
		{
			Message message = messages.get(i);
			
			s_logger.severe(message.getMessageLevel() + ": " + message.toString());
		}
	}
	
	private static boolean isEmptyModule(ParseTreeNode node)
	{
		if( node == null )  return true;
		
		if( node instanceof CajoledModule )
		{
			FunctionConstructor constructor = ((CajoledModule)node).getInstantiateMethod();
			Block body = constructor.getBody();
			if( body.children().size() == 1 )
			{
				for( Statement statement : body.children() )
				{
					if( statement instanceof ReturnStmt )
					{
						return ((ReturnStmt) statement).getReturnValue() == null;
					}
				}
			}
		}
		return false;
	}
	
	/*private static void transmuteSnapNode(Node node)
	{
		Document doc = node.getOwnerDocument();
		Element element = doc.createElement("a");
		
		// Copy the attributes to the new element
		Attr address = null;
		NamedNodeMap attrs = node.getAttributes();
		for (int i=0; i<attrs.getLength(); i++)
		{
		    Attr attr = (Attr)doc.importNode(attrs.item(i), true);
		    
		    if( attr.getNodeName().equals("address") )
		    {
		    	address = attr;
		    }
		    else if( !attr.getNodeName().contains("href") )
		    {
		    	element.getAttributes().setNamedItem(attr);
		    }
		}
		
		if( address != null )
		{
			Attr href = doc.createAttribute("href");
			String addressValue = address.getValue();
			addressValue = StringEscapeUtils.escapeEcmaScript(addressValue);
			href.setNodeValue("javascript:$$$$.snapTo('"+addressValue+"');");
			element.setAttributeNode(href);
		}

		Node sibling = node.getFirstChild();
		while( sibling != null )
		{
			Node nextSibling = sibling.getNextSibling();
			node.removeChild(sibling);
			element.appendChild(sibling);
			sibling = nextSibling;
		}

		// Replace the old node with the new node
		node.getParentNode().replaceChild(element, node);
	}*/
	
	private static void transformDom(Node node, String apiNamespace)
	{
		if( node.getNodeName().equals("a") )
		{
			boolean setTarget = true;
				
			Node attrNode = node.getAttributes().getNamedItem("href");
			if( attrNode instanceof Attr )
			{
				Attr href = (Attr) attrNode;

				CellAddress address = new CellAddress(href.getValue());
				
				if( address.getParseError() == E_CellAddressParseError.NO_ERROR )
				{
					String rawAddress = address.getRaw();
					href.setNodeValue(U_Code.transformPathToJavascript(apiNamespace, rawAddress));
					
					setTarget = false;
				}
			}
			
			if( setTarget )
			{
				Attr target = node.getOwnerDocument().createAttribute("target");
				target.setNodeValue("_blank"); // compiler should have addressed this, but just being safe.
				node.getAttributes().setNamedItem(target);
			}
			else
			{
				node.getAttributes().removeNamedItem("target");
			}
		}
		
		Node sibling = node.getFirstChild();
		for (; sibling != null; sibling = sibling.getNextSibling())
		{
			transformDom(sibling, apiNamespace);
		}
	}
	
	private static String renderModule(ParseTreeNode node)
	{
		StringBuilder sb = new StringBuilder();
		TokenConsumer tc = node.makeRenderer(sb, null);
		node.render(new RenderContext(tc));
		tc.noMoreTokens();
		return sb.toString();
	}
}
