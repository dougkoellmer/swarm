package swarm.shared.thirdparty;

public class S_Caja
{
	public static final String CAJA_NAMESPACE_SUFFIX = "___";
	
	public static final String OUTER_CONTAINER_STYLE = "position: relative; overflow: hidden; display: block; margin: 0px; padding: 0px;";
	public static final String OUTER_CONTAINER_CLASS = "caja-vdoc-outer caja-vdoc-wrapper";
	public static final String INNER_CONTAINER_STYLE = "display: block; position: relative;";
	public static final String INNER_CONTAINER_CLASS = "static-caja-vdoc caja-vdoc-inner caja-vdoc-wrapper vdoc-container___";
	
	public static final String OUTER_DIV = "<div style='"+OUTER_CONTAINER_STYLE+"' class='"+OUTER_CONTAINER_CLASS+"'>";
	public static final String INNER_DIV = "<div style='"+INNER_CONTAINER_STYLE+"' class='"+INNER_CONTAINER_CLASS+" {{namespace}}'>";
	public static final String CAJA_DIVS_START = OUTER_DIV+INNER_DIV;
	public static final String CAJA_DIVS_END = "</div></div>";
}
