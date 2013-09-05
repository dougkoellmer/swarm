var caja=function(){function B(a){return function(b,d,e){if(b){var f="caja_ajax_"+K++;window[f]=function(a){try{e(a)}finally{window[f]=void 0}};L(f,a?String(a):g.server+"/cajole?url="+encodeURIComponent(b.toString())+"&input-mime-type="+encodeURIComponent(d)+"&transform=PROXY&callback="+encodeURIComponent(f)+"&alt=json-in-script")}else e(void 0)}}function q(a){return function(){r&&r.console[a].apply(r.console,arguments)}}function c(){throw Error("Calling taming function before Caja is ready");}function C(a,
b,d){if(k!==s)throw Error("Caja cannot be initialized more than once");k=M;x(a,function(a,d){t=a;g.iframe=a.iframe;g.USELESS=a.USELESS;for(var h in g)g[h]===c&&(g[h]=a[h]);a.disableSecurityForDebugger(D);k=E;h={};h.es5Mode=d;"function"===typeof b&&b(h);y(null)},function(a){k=s;d(a)})}function y(a){"function"===typeof a&&u.push(a);if(k===E){for(a=0;a<u.length;a++)setTimeout(u[a],0);u=[]}}function x(a,b,d){z(window);r=a=N(a);g.server=a.server;!1===a.es5Mode||!0!==a.es5Mode&&!Object.getOwnPropertyNames?
F(a,b,d):O(a,b,d)}function N(a){a=a||{};var b={};b.server=String(a.server||a.cajaServer||P);b.resources=String(a.resources||b.server);b.debug=!!a.debug;if("forceES5Mode"in a&&"es5Mode"in a)throw Error("Cannot use both forceES5Mode and es5Mode in the same config");if(void 0!==a.forceES5Mode)b.es5Mode=!!a.forceES5Mode,b.maxAcceptableSeverity="NOT_ISOLATED";else{b.es5Mode=void 0===a.es5Mode?Q:!!a.es5Mode;var d=String(a.maxAcceptableSeverity||"SAFE_SPEC_VIOLATION");"NO_KNOWN_EXPLOIT_SPEC_VIOLATION"===
d&&(d="SAFE_SPEC_VIOLATION",b.acceptNoKnownExploitProblems=!0);b.maxAcceptableSeverity=d}b.console=a.console?a.console:a.log?{log:a.log,warn:a.log,error:a.log,info:a.log}:window.console&&"function"===typeof window.console.log?window.console:{log:function(){},warn:function(){},error:function(){},info:function(){}};if(a.targetAttributePresets){if(!a.targetAttributePresets["default"])throw"targetAttributePresets must contain a default";if(!a.targetAttributePresets.whitelist)throw"targetAttributePresets must contain a whitelist";
if(0===a.targetAttributePresets.whitelist.length)throw"targetAttributePresets.whitelist array must be nonempty";b.targetAttributePresets=a.targetAttributePresets}"object"===typeof a.cajolingServiceClient&&(b.cajolingServiceClient=a.cajolingServiceClient);return b}function z(a){a.Object.FERAL_FRAME_OBJECT___!==a.Object&&(a.___={},a.Object.FERAL_FRAME_OBJECT___=a.Object)}function F(a,b,d){var e=G(a,"es53-guest-frame");m(a,"es53-taming-frame",function(d){d=d.ES53FrameGroup(H,a,d,window,e);b(d,!1)})}
function O(a,b,d){var e=G(a,"ses-single-frame",function(b){b=b.ses||(b.ses={});b.maxAcceptableSeverityName=a.maxAcceptableSeverity;a.acceptNoKnownExploitProblems&&(b.acceptableProblems={DEFINING_READ_ONLY_PROTO_FAILS_SILENTLY:{permit:!0},PUSH_IGNORES_SEALED:{permit:!0,doNotRepair:!0},UNSHIFT_IGNORES_SEALED:{permit:!0},SPLICE_IGNORES_SEALED:{permit:!0},SHIFT_IGNORES_SEALED:{permit:!0},PUSH_DOES_NOT_THROW_ON_FROZEN_ARRAY:{permit:!0,doNotRepair:!0},ARRAYS_DELETE_NONCONFIGURABLE:{permit:!0},ARRAYS_MODIFY_READONLY:{permit:!0},
FREEZE_IS_FRAME_DEPENDENT:{permit:!0},SYNTAX_ERRORS_ARENT_ALWAYS_EARLY:{permit:!0}});b.mitigateSrcGotchas=function(){throw new EvalError("This function is a placeholder that should have been replaced by the real ses.mitigateSrcGotchas.");}});m(a,"utility-frame",function(c){var h=c.ses.mitigateSrcGotchas;e.make(function(c){var e=!0===a.es5Mode;if(c.ses.ok())c=c.SESFrameGroup(H,a,c,window,{mitigateSrcGotchas:h}),b(c,!0);else if(e)if(c=Error("ES5 mode requested but browser is unsupported"),"function"===
typeof d)d(c);else throw c;else a.console.log("Unable to use SES.  Switching to ES53."),F(a,b,d)})})}function G(a,b,d){function c(){if(l===h&&n){var a=n,b=g;l=f;g=n=null;b(a)}}var f="IDLE",h="WAITING",l=f,n,g,k={preload:function(){l===f&&(l="LOADING",n=null,m(a,b,function(a){n=a;c()},d))},make:function(f){"LOADING"===l?(l=h,g=f,c()):m(a,b,f,d)}};k.preload();return k}function m(a,b,d,c){var f=I(b),g=J(a.resources,v+"/"+b+(a.debug?".js?debug=1":".opt.js?debug=1"));setTimeout(function(){f.cajaIframeDone___=
function(){if(v!==f.cajaBuildVersion){var c="Version error: caja.js version "+v+" does not match "+b+" version "+f.cajaBuildVersion+".",e=String(v).split(/[mM]/)[0],g=String(f.cajaBuildVersion).split(/[mM]/)[0];if(e===g)a.console.log(c+"  Continuing because major versions match.");else throw a.console.log(c),Error(c);}d(f)};c&&c(f);setTimeout(function(){var a=f.document,b=a.createElement("script");b.setAttribute("type","text/javascript");b.src=g;a.body.appendChild(b)},0)},0)}function I(a){var b=document.createElement("iframe");
b.style.display="none";b.width=0;b.height=0;b.className=a||"";a=document.getElementsByTagName("script")[0];a.parentNode.insertBefore(b,a);return b.contentWindow}function L(a,b){A||(A=I("loader-frame").document);var c=""+'<script>var $name = parent.window["$name"];\x3c/script>'.replace(/[$]name/g,a)+'<script type="text/javascript" src="$url">\x3c/script>'.replace(/[$]url/g,(""+b).replace(/&/g,"&amp;").replace(/[<]/g,"&lt;").replace(/>/g,"&gt;").replace(/\"/g,"&#34;"));A.write(c)}function J(a,b){a=
a.replace(/\/+$/,"");b=b.replace(/^\/+/,"");return a+"/"+b}function p(a,b,c){if(typeof a!==b)throw new TypeError("expected "+b+" instead of "+typeof a+": "+(c||a));return a}var v="%VERSION%",P="https://caja.appspot.com/",t,u=[],w=[],R=0,s="UNREADY",M="PENDING",E="READY",k=s,r=void 0,Q="GUESS",K=1,D=!1,A,S={net:{rewriter:{NO_NETWORK:function(){return null},ALL:function(a){return String(a)}},fetcher:{USE_XHR:function(a,b,c){var e=new XMLHttpRequest;e.open("GET",a.toString(),!0);e.overrideMimeType(b);
e.onreadystatechange=function(){4==e.readyState&&c({html:e.responseText})};e.send()},USE_AS_PROXY:B},NO_NETWORK:{rewrite:function(){return null},fetch:function(a,b,c){setTimeout(function(){c({})},0)}},ALL:{rewrite:function(a){return String(a)},fetch:B(void 0)},only:function(a){a=String(a);return{rewrite:function(b){b=String(b);return b===a?b:null}}}},ATTRIBUTETYPES:void 0,LOADERTYPES:void 0,URIEFFECTS:void 0},g={initialize:C,load:function(a,b,c,e){b=b||g.policy.net.NO_NETWORK;k===s&&C({});y(function(){t.makeES5Frame(a,
b,c,e)})},whenReady:y,policy:S,iframe:null,USELESS:void 0,tame:c,tamesTo:c,reTamesTo:c,untame:c,unwrapDom:c,markReadOnlyRecord:c,markFunction:c,markCtor:c,markXo4a:c,grantMethod:c,grantRead:c,grantReadWrite:c,adviseFunctionBefore:c,adviseFunctionAfter:c,adviseFunctionAround:c,makeDefensibleObject___:c,makeDefensibleFunction___:c,initFeralFrame:z,makeFrameGroup:x,configure:x,disableSecurityForDebugger:function(a){D=!!a;t&&t.disableSecurityForDebugger(a)},Q:c,console:{error:q("error"),info:q("info"),
log:q("log"),warn:q("warn")},testing_makeDomadoRuleBreaker:c,closureCanary:1},H={documentBaseUrl:function(){var a=document.getElementsByTagName("base");if(0==a.length)return document.location.toString();if(1==a.length){a=a[0].href;if("string"!==typeof a)throw Error("Caja loader error: <base> without a href.");return a}throw Error("Caja loader error: document contains multiple <base>.");},getId:function(a){p(a,"object","imports");var b;b="id___"in a?p(a.id___,"number","id"):a.id___=w.length;w[b]=a;
return b},getImports:function(a){var b=w[p(a,"number","id")];if(void 0===b)throw Error("Internal: imports#",a," unregistered");return b},joinUrl:J,loadCajaFrame:m,prepareContainerDiv:function(a,b,c){b=(c=c||{},c.idClass)||"caja-guest-"+R++ +"___";a&&9===a.nodeType&&(g.console.warn("Warning: Using a document, rather than an element, as a Caja virtual document container is an experimental feature and may not operate correctly or support all features."),z(a.defaultView));return{idClass:b,opt_div:a}},
unregister:function(a){p(a,"object","imports");"id___"in a&&(a=p(a.id___,"number","id"),w[a]=void 0)},readPropertyAsHostFrame:function(a,b){return a[b]}};return g}();"undefined"!==typeof window&&(window.caja=caja);