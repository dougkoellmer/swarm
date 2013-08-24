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

import swarm.server.structs.bhServerCode;
import swarm.shared.account.bhE_SignUpValidationError;
import swarm.shared.account.bhI_SignUpCredentialValidator;
import swarm.shared.app.bhS_App;
import swarm.shared.code.bhA_CodeCompiler;
import swarm.shared.code.bhCompilerResult;
import swarm.shared.code.bhCompilerMessage;
import swarm.shared.code.bhE_CompilationStatus;
import swarm.shared.code.bhE_CompilerMessageLevel;
import swarm.shared.code.bhFileRange;
import swarm.shared.code.bhU_Code;
import swarm.shared.entities.bhE_CodeSafetyLevel;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCode;
import swarm.shared.structs.bhCodePrivileges;
import swarm.shared.structs.bhE_CellAddressParseError;
import swarm.shared.structs.bhE_NetworkPrivilege;
import swarm.shared.thirdparty.bhS_Caja;

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

public class bhServerCodeCompiler extends bhA_CodeCompiler
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
	
	private static final Logger s_logger = Logger.getLogger(bhServerCodeCompiler.class.getName());
	
	private Pair<HtmlSchema, List<Message>> m_customHtmlSchema;
	
	public bhServerCodeCompiler()
	{		
		super();
	}

	private synchronized void createHtmlSchema()
	{
		//MessageQueue dummyQueue = new SimpleMessageQueue();
		if( m_customHtmlSchema == null )
		{
			SimpleMessageQueue cacheMq = new SimpleMessageQueue();
			URI elSrc = URI.create(
					"resource:///b33hive/server/code/"
							+ "htmlall-elements.json");
			URI attrSrc = URI.create(
					"resource:///b33hive/server/code/"
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
	
	protected bhCompilerResult createResult()
	{
		return new bhCompilerResult();
	}
	
	@Override
	public bhCompilerResult compile(bhCode sourceCode, bhCodePrivileges privileges, String namespace)
	{
		namespace += bhS_Caja.CAJA_NAMESPACE_SUFFIX;
		
		bhCompilerResult result = (bhCompilerResult) super.compile(sourceCode, privileges, namespace);
		
		if( result.getStatus() != bhE_CompilationStatus.NO_ERROR )
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
		
		bhUriPolicy uriPolicy = new bhUriPolicy(privileges.getNetworkPrivilege());
		
		PluginMeta meta = new PluginMeta(fetcher, uriPolicy);
		meta.setIdClass(namespace);
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
			
			return result.onFailure(bhE_CompilationStatus.COMPILER_EXCEPTION);
		}
		
		bhHtmlSchema schema = new bhHtmlSchema(this.getHtmlSchema());
		bhHtmlPreProcessor preProcessor = new bhHtmlPreProcessor(htmlDom, schema);
		
		boolean hasNoscriptContent = preProcessor.hasNoscriptContent();
		Dom noScriptHtmlDom = htmlDom;
		if( hasNoscriptContent )
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
			transformDom(staticHtmlDocument);
		}
		
		String staticHtml = staticHtmlDocument != null ? Nodes.render(staticHtmlDocument) : "";
		
		if( hasNoscriptContent )
		{
			preProcessor.injectNoscriptTag();
			schema.setToNoScriptMode();
			PluginCompiler noScriptCompiler = compile(noScriptHtmlDom, result, meta, schema, messageQueue);
			
			if( noScriptCompiler == null )  return result;
			
			//--- DRK > Create splash code.
			Node noScriptHtmlDocument = noScriptCompiler.getStaticHtml();
			String noScriptHtml = noScriptHtmlDocument != null ? Nodes.render(noScriptHtmlDocument) : "";
			bhServerCode splashCode = new bhServerCode(noScriptHtml, bhE_CodeType.SPLASH);
			splashCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_STATIC_SANDBOX);
			
			bhServerCode compiledCode;
			
			if( !hasJavaScript)
			{
				compiledCode = new bhServerCode(staticHtml, bhE_CodeType.COMPILED);
				compiledCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_STATIC_SANDBOX);
			}
			else
			{				
				compiledCode = new bhServerCode(sourceCode.getRawCode(), bhE_CodeType.COMPILED);
				compiledCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX);
			}
			
			return result.onSuccess(splashCode, compiledCode);
		}
		else
		{
			if( !hasJavaScript)
			{
				bhServerCode splashCode = new bhServerCode(staticHtml, bhE_CodeType.SPLASH, bhE_CodeType.COMPILED);
				splashCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_STATIC_SANDBOX);
				
				return result.onSuccess(splashCode);
			}
			else
			{
				bhServerCode splashCode = new bhServerCode(staticHtml, bhE_CodeType.SPLASH);
				splashCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_STATIC_SANDBOX);
				
				bhServerCode compiledCode = new bhServerCode(sourceCode.getRawCode(), bhE_CodeType.COMPILED);
				compiledCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX);
				
				//TODO: May minify compiled code in future, maybe as an optional step.
				
				return result.onSuccess(splashCode, compiledCode);
			}
		}
	}
	
	private static boolean hasJavaScript(PluginCompiler compiler, bhHtmlPreProcessor preProcessor)
	{
		ParseTreeNode jsModule = compiler.getJavascript();
		boolean isEmptyJsModule = isEmptyModule(jsModule);
		return !isEmptyJsModule || preProcessor.foundJavaScript();
	}
	
	private static PluginCompiler compile(Dom htmlDom, bhCompilerResult result_out, PluginMeta meta, HtmlSchema schema, MessageQueue messageQueue)
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
			
			result_out.onFailure(bhE_CompilationStatus.COMPILER_EXCEPTION);
			
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
				bhCompilerMessage compilerError = createMessage(message);
				
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
				
				result_out.onFailure(bhE_CompilationStatus.COMPILER_EXCEPTION);
			}
		}
		else
		{
			// result has compiler errors inside already.
		}
		
		return null;
	}
	
	private static bhCompilerMessage createMessage(Message message)
	{
		bhFileRange range = null;
		String formattedMessage = null;
		bhE_CompilerMessageLevel level = null;
		
		if( message.getMessageLevel().ordinal() <= MessageLevel.LINT.ordinal() )
		{
			level = bhE_CompilerMessageLevel.LINT;
		}
		else if( message.getMessageLevel().ordinal() <= MessageLevel.WARNING.ordinal() )
		{
			level = bhE_CompilerMessageLevel.WARNING;
		}
		else
		{
			level = bhE_CompilerMessageLevel.ERROR;
		}
		
		List<MessagePart> parts = message.getMessageParts();
		MessagePart[] modParts = new MessagePart[parts.size()];
		
		for( int i = 0; i < parts.size(); i++ )
		{
			MessagePart part = parts.get(i);
			if( part instanceof FilePosition )
			{
				FilePosition filePosition = (FilePosition) part;
				range = new bhFileRange
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
		
		return new bhCompilerMessage(level, formattedMessage, range);
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
	
	private static void transformDom(Node node)
	{
		if( node.getNodeName().equals("a") )
		{
			boolean setTarget = true;
				
			Node attrNode = node.getAttributes().getNamedItem("href");
			if( attrNode instanceof Attr )
			{
				Attr href = (Attr) attrNode;

				bhCellAddress address = new bhCellAddress(href.getValue());
				
				if( address.getParseError() == bhE_CellAddressParseError.NO_ERROR )
				{
					String rawAddress = address.getRawAddress();
					href.setNodeValue(bhU_Code.transformPathToJavascript(rawAddress));
					
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
			transformDom(sibling);
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
