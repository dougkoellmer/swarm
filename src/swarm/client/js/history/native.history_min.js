"object"!==typeof JSON&&(JSON={});
(function(){function c(b){return 10>b?"0"+b:b}function p(d){b.lastIndex=0;return b.test(d)?'"'+d.replace(b,function(b){var d=k[b];return"string"===typeof d?d:"\\u"+("0000"+b.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+d+'"'}function f(b,c){var e,a,k,t,s=d,q,g=c[b];g&&"object"===typeof g&&"function"===typeof g.toJSON&&(g=g.toJSON(b));"function"===typeof m&&(g=m.call(c,b,g));switch(typeof g){case "string":return p(g);case "number":return isFinite(g)?String(g):"null";case "boolean":case "null":return String(g);
case "object":if(!g)return"null";d+=h;q=[];if("[object Array]"===Object.prototype.toString.apply(g)){t=g.length;for(e=0;e<t;e+=1)q[e]=f(e,g)||"null";k=0===q.length?"[]":d?"[\n"+d+q.join(",\n"+d)+"\n"+s+"]":"["+q.join(",")+"]";d=s;return k}if(m&&"object"===typeof m)for(t=m.length,e=0;e<t;e+=1)"string"===typeof m[e]&&(a=m[e],(k=f(a,g))&&q.push(p(a)+(d?": ":":")+k));else for(a in g)Object.prototype.hasOwnProperty.call(g,a)&&(k=f(a,g))&&q.push(p(a)+(d?": ":":")+k);k=0===q.length?"{}":d?"{\n"+d+q.join(",\n"+
d)+"\n"+s+"}":"{"+q.join(",")+"}";d=s;return k}}"function"!==typeof Date.prototype.toJSON&&(Date.prototype.toJSON=function(b){return isFinite(this.valueOf())?this.getUTCFullYear()+"-"+c(this.getUTCMonth()+1)+"-"+c(this.getUTCDate())+"T"+c(this.getUTCHours())+":"+c(this.getUTCMinutes())+":"+c(this.getUTCSeconds())+"Z":null},String.prototype.toJSON=Number.prototype.toJSON=Boolean.prototype.toJSON=function(b){return this.valueOf()});var e=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
b=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,d,h,k={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},m;"function"!==typeof JSON.stringify&&(JSON.stringify=function(b,c,e){var a;h=d="";if("number"===typeof e)for(a=0;a<e;a+=1)h+=" ";else"string"===typeof e&&(h=e);if((m=c)&&"function"!==typeof c&&("object"!==typeof c||"number"!==typeof c.length))throw Error("JSON.stringify");return f("",{"":b})});
"function"!==typeof JSON.parse&&(JSON.parse=function(b,d){function c(a,b){var e,h,g=a[b];if(g&&"object"===typeof g)for(e in g)Object.prototype.hasOwnProperty.call(g,e)&&(h=c(g,e),void 0!==h?g[e]=h:delete g[e]);return d.call(a,b,g)}var a;b=String(b);e.lastIndex=0;e.test(b)&&(b=b.replace(e,function(a){return"\\u"+("0000"+a.charCodeAt(0).toString(16)).slice(-4)}));if(/^[\],:{}\s]*$/.test(b.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,"@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,
"]").replace(/(?:^|:|,)(?:\s*\[)+/g,"")))return a=eval("("+b+")"),"function"===typeof d?c({"":a},""):a;throw new SyntaxError("JSON.parse");})})();
(function(c,p){var f=c.History=c.History||{};if("undefined"!==typeof f.Adapter)throw Error("History.js Adapter has already been loaded...");f.Adapter={handlers:{},_uid:1,uid:function(e){return e._uid||(e._uid=f.Adapter._uid++)},bind:function(e,b,d){var c=f.Adapter.uid(e);f.Adapter.handlers[c]=f.Adapter.handlers[c]||{};f.Adapter.handlers[c][b]=f.Adapter.handlers[c][b]||[];f.Adapter.handlers[c][b].push(d);e["on"+b]=function(b,d){return function(c){f.Adapter.trigger(b,d,c)}}(e,b)},trigger:function(c,
b,d){d=d||{};c=f.Adapter.uid(c);var h,k;f.Adapter.handlers[c]=f.Adapter.handlers[c]||{};f.Adapter.handlers[c][b]=f.Adapter.handlers[c][b]||[];h=0;for(k=f.Adapter.handlers[c][b].length;h<k;++h)f.Adapter.handlers[c][b][h].apply(this,[d])},extractEventData:function(c,b){return b&&b[c]||p},onDomLoad:function(e){var b=c.setTimeout(function(){e()},2E3);c.onload=function(){clearTimeout(b);e()}}};"undefined"!==typeof f.init&&f.init()})(window);
(function(c,p){var f=c.document,e=c.setInterval||e,b=c.History=c.History||{};if("undefined"!==typeof b.initHtml4)throw Error("History.js HTML4 Support has already been loaded...");b.initHtml4=function(){if("undefined"!==typeof b.initHtml4.initialized)return!1;b.initHtml4.initialized=!0;b.enabled=!0;b.savedHashes=[];b.isLastHash=function(d){var c=b.getHashByIndex();return d===c};b.isHashEqual=function(b,c){b=encodeURIComponent(b).replace(/%25/g,"%");c=encodeURIComponent(c).replace(/%25/g,"%");return b===
c};b.saveHash=function(d){if(b.isLastHash(d))return!1;b.savedHashes.push(d);return!0};b.getHashByIndex=function(d){return"undefined"===typeof d?b.savedHashes[b.savedHashes.length-1]:0>d?b.savedHashes[b.savedHashes.length+d]:b.savedHashes[d]};b.discardedHashes={};b.discardedStates={};b.discardState=function(d,c,e){var f=b.getHashByState(d);b.discardedStates[f]={discardedState:d,backState:e,forwardState:c};return!0};b.discardHash=function(c,e,f){b.discardedHashes[c]={discardedHash:c,backState:f,forwardState:e};
return!0};b.discardedState=function(c){c=b.getHashByState(c);return b.discardedStates[c]||!1};b.discardedHash=function(c){return b.discardedHashes[c]||!1};b.recycleState=function(c){var e=b.getHashByState(c);b.discardedState(c)&&delete b.discardedStates[e];return!0};b.emulated.hashChange&&(b.hashChangeInit=function(){b.checkerFunction=null;var d="",h,k,m,l=Boolean(b.getHash());b.isInternetExplorer()?(h=f.createElement("iframe"),h.setAttribute("id","historyjs-iframe"),h.setAttribute("src","#"),h.style.display=
"none",f.body.appendChild(h),h.contentWindow.document.open(),h.contentWindow.document.close(),k="",m=!1,b.checkerFunction=function(){if(m)return!1;m=!0;var e=b.getHash(),f=b.getHash(h.contentWindow.document);e!==d?(d=e,f!==e&&(k=e,h.contentWindow.document.open(),h.contentWindow.document.close(),h.contentWindow.document.location.hash=b.escapeHash(e)),b.Adapter.trigger(c,"hashchange")):f!==k&&(k=f,l&&""===f?b.back():b.setHash(f,!1));m=!1;return!0}):b.checkerFunction=function(){var e=b.getHash()||"";
e!==d&&(d=e,b.Adapter.trigger(c,"hashchange"));return!0};b.intervalList.push(e(b.checkerFunction,b.options.hashChangeInterval));return!0},b.Adapter.onDomLoad(b.hashChangeInit));b.emulated.pushState&&(b.onHashChange=function(d){d=d&&d.newURL||b.getLocationHref();var e=b.getHashByUrl(d);if(b.isLastHash(e))return b.busy(!1),!1;b.doubleCheckComplete();b.saveHash(e);if(e&&b.isTraditionalAnchor(e))return b.Adapter.trigger(c,"anchorchange"),b.busy(!1),!1;d=b.extractState(b.getFullUrl(e||b.getLocationHref()),
!0);if(b.isLastSavedState(d))return b.busy(!1),!1;b.getHashByState(d);if(e=b.discardedState(d))return b.getHashByIndex(-2)===b.getHashByState(e.forwardState)?b.back(!1):b.forward(!1),!1;b.pushState(d.data,d.title,encodeURI(d.url),!1);return!0},b.Adapter.bind(c,"hashchange",b.onHashChange),b.pushState=function(d,e,f,m){f=encodeURI(f).replace(/%25/g,"%");if(b.getHashByUrl(f))throw Error("History.js does not support states with fragment-identifiers (hashes/anchors).");if(!1!==m&&b.busy())return b.pushQueue({scope:b,
callback:b.pushState,args:arguments,queue:m}),!1;b.busy(!0);var l=b.createStateObject(d,e,f),n=b.getHashByState(l),p=b.getState(!1),p=b.getHashByState(p),a=b.getHash(),r=b.expectedStateId==l.id;b.storeState(l);b.expectedStateId=l.id;b.recycleState(l);b.setTitle(l);if(n===p)return b.busy(!1),!1;b.saveState(l);r||b.Adapter.trigger(c,"statechange");b.isHashEqual(n,a)||b.isHashEqual(n,b.getShortUrl(b.getLocationHref()))||b.setHash(n,!1);b.busy(!1);return!0},b.replaceState=function(e,f,k,m){k=encodeURI(k).replace(/%25/g,
"%");if(b.getHashByUrl(k))throw Error("History.js does not support states with fragment-identifiers (hashes/anchors).");if(!1!==m&&b.busy())return b.pushQueue({scope:b,callback:b.replaceState,args:arguments,queue:m}),!1;b.busy(!0);var l=b.createStateObject(e,f,k),n=b.getHashByState(l),p=b.getState(!1),a=b.getHashByState(p),r=b.getStateByIndex(-2);b.discardState(p,l,r);n===a?(b.storeState(l),b.expectedStateId=l.id,b.recycleState(l),b.setTitle(l),b.saveState(l),b.Adapter.trigger(c,"statechange"),b.busy(!1)):
b.pushState(l.data,l.title,l.url,!1);return!0});if(b.emulated.pushState&&b.getHash()&&!b.emulated.hashChange)b.Adapter.onDomLoad(function(){b.Adapter.trigger(c,"hashchange")})};"undefined"!==typeof b.init&&b.init()})(window);
(function(c,p){var f=c.console||p,e=c.document,b=c.navigator,d=!1,h=c.setTimeout,k=c.clearTimeout,m=c.setInterval,l=c.clearInterval,n=c.JSON,u=c.alert,a=c.History=c.History||{},r=c.history;try{d=c.sessionStorage,d.setItem("TEST","1"),d.removeItem("TEST")}catch(t){d=!1}n.stringify=n.stringify||n.encode;n.parse=n.parse||n.decode;if("undefined"!==typeof a.init)throw Error("History.js Core has already been loaded...");a.init=function(b){if("undefined"===typeof a.Adapter)return!1;"undefined"!==typeof a.initCore&&
a.initCore();"undefined"!==typeof a.initHtml4&&a.initHtml4();return!0};a.initCore=function(s){if("undefined"!==typeof a.initCore.initialized)return!1;a.initCore.initialized=!0;a.options=a.options||{};a.options.hashChangeInterval=a.options.hashChangeInterval||100;a.options.safariPollInterval=a.options.safariPollInterval||500;a.options.doubleCheckInterval=a.options.doubleCheckInterval||500;a.options.disableSuid=a.options.disableSuid||!1;a.options.storeInterval=a.options.storeInterval||1E3;a.options.busyDelay=
a.options.busyDelay||250;a.options.debug=a.options.debug||!1;a.options.initialTitle=a.options.initialTitle||e.title;a.options.html4Mode=a.options.html4Mode||!1;a.options.delayInit=a.options.delayInit||!1;a.intervalList=[];a.clearAllIntervals=function(){var g,b=a.intervalList;if("undefined"!==typeof b&&null!==b){for(g=0;g<b.length;g++)l(b[g]);a.intervalList=null}};a.debug=function(){a.options.debug&&a.log.apply(a,arguments)};a.log=function(){var a=!("undefined"===typeof f||"undefined"===typeof f.log||
"undefined"===typeof f.log.apply),b=e.getElementById("log"),c,d,h,k;a?(d=Array.prototype.slice.call(arguments),c=d.shift(),"undefined"!==typeof f.debug?f.debug.apply(f,[c,d]):f.log.apply(f,[c,d])):c="\n"+arguments[0]+"\n";d=1;for(h=arguments.length;d<h;++d){k=arguments[d];if("object"===typeof k&&"undefined"!==typeof n)try{k=n.stringify(k)}catch(l){}c+="\n"+k+"\n"}b?(b.value+=c+"\n-----\n",b.scrollTop=b.scrollHeight-b.clientHeight):a||u(c);return!0};a.getInternetExplorerMajorVersion=function(){var g=
a.getInternetExplorerMajorVersion,b;if("undefined"!==typeof a.getInternetExplorerMajorVersion.cached)b=a.getInternetExplorerMajorVersion.cached;else{b=3;for(var c=e.createElement("div"),d=c.getElementsByTagName("i");(c.innerHTML="\x3c!--[if gt IE "+ ++b+"]><i></i><![endif]--\x3e")&&d[0];);b=4<b?b:!1}return g.cached=b};a.isInternetExplorer=function(){return a.isInternetExplorer.cached="undefined"!==typeof a.isInternetExplorer.cached?a.isInternetExplorer.cached:Boolean(a.getInternetExplorerMajorVersion())};
a.emulated=a.options.html4Mode?{pushState:!0,hashChange:!0}:{pushState:!Boolean(c.history&&c.history.pushState&&c.history.replaceState&&!(/ Mobile\/([1-7][a-z]|(8([abcde]|f(1[0-8]))))/i.test(b.userAgent)||/AppleWebKit\/5([0-2]|3[0-2])/i.test(b.userAgent))),hashChange:Boolean(!("onhashchange"in c||"onhashchange"in e)||a.isInternetExplorer()&&8>a.getInternetExplorerMajorVersion())};a.enabled=!a.emulated.pushState;a.bugs={setHash:Boolean(!a.emulated.pushState&&"Apple Computer, Inc."===b.vendor&&/AppleWebKit\/5([0-2]|3[0-3])/.test(b.userAgent)),
safariPoll:Boolean(!a.emulated.pushState&&"Apple Computer, Inc."===b.vendor&&/AppleWebKit\/5([0-2]|3[0-3])/.test(b.userAgent)),ieDoubleCheck:Boolean(a.isInternetExplorer()&&8>a.getInternetExplorerMajorVersion()),hashEscape:Boolean(a.isInternetExplorer()&&7>a.getInternetExplorerMajorVersion())};a.isEmptyObject=function(a){for(var b in a)if(a.hasOwnProperty(b))return!1;return!0};a.cloneObject=function(a){a?(a=n.stringify(a),a=n.parse(a)):a={};return a};a.getRootUrl=function(){var a=e.location.protocol+
"//"+(e.location.hostname||e.location.host);e.location.port&&(a+=":"+e.location.port);return a+"/"};a.getBaseHref=function(){var a=e.getElementsByTagName("base"),b=null,b="";1===a.length&&(b=a[0],b=b.href.replace(/[^\/]+$/,""));(b=b.replace(/\/+$/,""))&&(b+="/");return b};a.getBaseUrl=function(){return a.getBaseHref()||a.getBasePageUrl()||a.getRootUrl()};a.getPageUrl=function(){return((a.getState(!1,!1)||{}).url||a.getLocationHref()).replace(/\/+$/,"").replace(/[^\/]+$/,function(a,b,c){return/\./.test(a)?
a:a+"/"})};a.getBasePageUrl=function(){return a.getLocationHref().replace(/[#\?].*/,"").replace(/[^\/]+$/,function(a,b,c){return/[^\/]$/.test(a)?"":a}).replace(/\/+$/,"")+"/"};a.getFullUrl=function(g,b){var c=g,d=g.substring(0,1);b="undefined"===typeof b?!0:b;/[a-z]+\:\/\//.test(g)||(c="/"===d?a.getRootUrl()+g.replace(/^\/+/,""):"#"===d?a.getPageUrl().replace(/#.*/,"")+g:"?"===d?a.getPageUrl().replace(/[\?#].*/,"")+g:b?a.getBaseUrl()+g.replace(/^(\.\/)+/,""):a.getBasePageUrl()+g.replace(/^(\.\/)+/,
""));return c.replace(/\#$/,"")};a.getShortUrl=function(g){var b=a.getBaseUrl(),c=a.getRootUrl();a.emulated.pushState&&(g=g.replace(b,""));g=g.replace(c,"/");a.isTraditionalAnchor(g)&&(g="./"+g);return g.replace(/^(\.\/)+/g,"./").replace(/\#$/,"")};a.getLocationHref=function(a){a=a||e;return a.URL===a.location.href?a.location.href:a.location.href===decodeURIComponent(a.URL)?a.URL:a.location.hash&&decodeURIComponent(a.location.href.replace(/^[^#]+/,""))===a.location.hash||-1==a.URL.indexOf("#")&&-1!=
a.location.href.indexOf("#")?a.location.href:a.URL||a.location.href};a.store={};a.idToState=a.idToState||{};a.stateToId=a.stateToId||{};a.urlToId=a.urlToId||{};a.storedStates=a.storedStates||[];a.savedStates=a.savedStates||[];a.normalizeStore=function(){a.store.idToState=a.store.idToState||{};a.store.urlToId=a.store.urlToId||{};a.store.stateToId=a.store.stateToId||{}};a.getState=function(b,c){"undefined"===typeof b&&(b=!0);"undefined"===typeof c&&(c=!0);var d=a.getLastSavedState();!d&&c&&(d=a.createStateObject());
b&&(d=a.cloneObject(d),d.url=d.cleanUrl||d.url);return d};a.getIdByState=function(b){var c=a.extractId(b.url),d;if(!c)if(d=a.getStateString(b),"undefined"!==typeof a.stateToId[d])c=a.stateToId[d];else if("undefined"!==typeof a.store.stateToId[d])c=a.store.stateToId[d];else{for(;c=(new Date).getTime()+String(Math.random()).replace(/\D/g,""),"undefined"!==typeof a.idToState[c]||"undefined"!==typeof a.store.idToState[c];);a.stateToId[d]=c;a.idToState[c]=b}return c};a.normalizeState=function(b){var c;
b&&"object"===typeof b||(b={});if("undefined"!==typeof b.normalized)return b;b.data&&"object"===typeof b.data||(b.data={});c={normalized:!0};c.title=b.title||"";c.url=a.getFullUrl(b.url?b.url:a.getLocationHref());c.hash=a.getShortUrl(c.url);c.data=a.cloneObject(b.data);c.id=a.getIdByState(c);c.cleanUrl=c.url.replace(/\??\&_suid.*/,"");c.url=c.cleanUrl;b=!a.isEmptyObject(c.data);(c.title||b)&&!0!==a.options.disableSuid&&(c.hash=a.getShortUrl(c.url).replace(/\??\&_suid.*/,""),/\?/.test(c.hash)||(c.hash+=
"?"),c.hash+="&_suid="+c.id);c.hashedUrl=a.getFullUrl(c.hash);(a.emulated.pushState||a.bugs.safariPoll)&&a.hasUrlDuplicate(c)&&(c.url=c.hashedUrl);return c};a.createStateObject=function(b,c,d){b={data:b,title:c,url:d};return a.normalizeState(b)};a.getStateById=function(b){b=String(b);return a.idToState[b]||a.store.idToState[b]||p};a.getStateString=function(b){b={data:a.normalizeState(b).data,title:b.title,url:b.url};return n.stringify(b)};a.getStateId=function(b){return a.normalizeState(b).id};a.getHashByState=
function(b){return a.normalizeState(b).hash};a.extractId=function(a){a=-1!=a.indexOf("#")?a.split("#")[0]:a;return((a=/(.*)\&_suid=([0-9]+)$/.exec(a))?String(a[2]||""):"")||!1};a.isTraditionalAnchor=function(a){return!/[\/\?\.]/.test(a)};a.extractState=function(b,c){var d=null,e,f;c=c||!1;(e=a.extractId(b))&&(d=a.getStateById(e));d||(f=a.getFullUrl(b),(e=a.getIdByUrl(f)||!1)&&(d=a.getStateById(e)),d||!c||a.isTraditionalAnchor(b)||(d=a.createStateObject(null,null,f)));return d};a.getIdByUrl=function(b){return a.urlToId[b]||
a.store.urlToId[b]||p};a.getLastSavedState=function(){return a.savedStates[a.savedStates.length-1]||p};a.getLastStoredState=function(){return a.storedStates[a.storedStates.length-1]||p};a.hasUrlDuplicate=function(b){var c=!1;return(c=a.extractState(b.url))&&c.id!==b.id};a.storeState=function(b){a.urlToId[b.url]=b.id;a.storedStates.push(a.cloneObject(b));return b};a.isLastSavedState=function(b){var c=!1;a.savedStates.length&&(b=b.id,c=a.getLastSavedState(),c=c.id,c=b===c);return c};a.saveState=function(b){if(a.isLastSavedState(b))return!1;
a.savedStates.push(a.cloneObject(b));return!0};a.getStateByIndex=function(b){return"undefined"===typeof b?a.savedStates[a.savedStates.length-1]:0>b?a.savedStates[a.savedStates.length+b]:a.savedStates[b]};a.getCurrentIndex=function(){return 1>a.savedStates.length?0:a.savedStates.length-1};a.getHash=function(b){b=a.getLocationHref(b);return a.getHashByUrl(b)};a.unescapeHash=function(b){b=a.normalizeHash(b);return decodeURIComponent(b)};a.normalizeHash=function(a){return a.replace(/[^#]*#/,"").replace(/#.*/,
"")};a.setHash=function(b,c){var d;if(!1!==c&&a.busy())return a.pushQueue({scope:a,callback:a.setHash,args:arguments,queue:c}),!1;a.busy(!0);(d=a.extractState(b,!0))&&!a.emulated.pushState?a.pushState(d.data,d.title,d.url,!1):a.getHash()!==b&&(a.bugs.setHash?(d=a.getPageUrl(),a.pushState(null,null,d+"#"+b,!1)):e.location.hash=b);return a};a.escapeHash=function(b){b=a.normalizeHash(b);b=c.encodeURIComponent(b);a.bugs.hashEscape||(b=b.replace(/\%21/g,"!").replace(/\%26/g,"&").replace(/\%3D/g,"=").replace(/\%3F/g,
"?"));return b};a.getHashByUrl=function(b){b=String(b).replace(/([^#]*)#?([^#]*)#?(.*)/,"$2");return a.unescapeHash(b)};a.setTitle=function(b){var c=b.title,d;c||(d=a.getStateByIndex(0))&&d.url===b.url&&(c=d.title||a.options.initialTitle);try{e.getElementsByTagName("title")[0].innerHTML=c.replace("<","&lt;").replace(">","&gt;").replace(" & "," &amp; ")}catch(f){}e.title=c;return a};a.queues=[];a.busy=function(b){"undefined"!==typeof b?a.busy.flag=b:"undefined"===typeof a.busy.flag&&(a.busy.flag=!1);
if(!a.busy.flag){k(a.busy.timeout);var c=function(){var b,d;if(!a.busy.flag)for(b=a.queues.length-1;0<=b;--b)d=a.queues[b],0!==d.length&&(d=d.shift(),a.fireQueueItem(d),a.busy.timeout=h(c,a.options.busyDelay))};a.busy.timeout=h(c,a.options.busyDelay)}return a.busy.flag};a.busy.flag=!1;a.fireQueueItem=function(b){return b.callback.apply(b.scope||a,b.args||[])};a.pushQueue=function(b){a.queues[b.queue||0]=a.queues[b.queue||0]||[];a.queues[b.queue||0].push(b);return a};a.queue=function(b,c){"function"===
typeof b&&(b={callback:b});"undefined"!==typeof c&&(b.queue=c);a.busy()?a.pushQueue(b):a.fireQueueItem(b);return a};a.clearQueue=function(){a.busy.flag=!1;a.queues=[];return a};a.stateChanged=!1;a.doubleChecker=!1;a.doubleCheckComplete=function(){a.stateChanged=!0;a.doubleCheckClear();return a};a.doubleCheckClear=function(){a.doubleChecker&&(k(a.doubleChecker),a.doubleChecker=!1);return a};a.doubleCheck=function(b){a.stateChanged=!1;a.doubleCheckClear();a.bugs.ieDoubleCheck&&(a.doubleChecker=h(function(){a.doubleCheckClear();
a.stateChanged||b();return!0},a.options.doubleCheckInterval));return a};a.safariStatePoll=function(){var b=a.extractState(a.getLocationHref());if(!a.isLastSavedState(b))return b||a.createStateObject(),a.Adapter.trigger(c,"popstate"),a};a.back=function(b){if(!1!==b&&a.busy())return a.pushQueue({scope:a,callback:a.back,args:arguments,queue:b}),!1;a.busy(!0);a.doubleCheck(function(){a.back(!1)});r.go(-1);return!0};a.forward=function(b){if(!1!==b&&a.busy())return a.pushQueue({scope:a,callback:a.forward,
args:arguments,queue:b}),!1;a.busy(!0);a.doubleCheck(function(){a.forward(!1)});r.go(1);return!0};a.go=function(b,c){var d;if(0<b)for(d=1;d<=b;++d)a.forward(c);else if(0>b)for(d=-1;d>=b;--d)a.back(c);else throw Error("History.go: History.go requires a positive or negative integer passed.");return a};a.emulated.pushState?(s=function(){},a.pushState=a.pushState||s,a.replaceState=a.replaceState||s):(a.onPopState=function(b,d){var e=!1;a.doubleCheckComplete();if(e=a.getHash())return(e=a.extractState(e||
a.getLocationHref(),!0))?a.replaceState(e.data,e.title,e.url,!1):(a.Adapter.trigger(c,"anchorchange"),a.busy(!1)),a.expectedStateId=!1;(e=(e=a.Adapter.extractEventData("state",b,d)||!1)?a.getStateById(e):a.expectedStateId?a.getStateById(a.expectedStateId):a.extractState(a.getLocationHref()))||(e=a.createStateObject(null,null,a.getLocationHref()));a.expectedStateId=!1;if(a.isLastSavedState(e))return a.busy(!1),!1;a.storeState(e);a.saveState(e);a.setTitle(e);a.Adapter.trigger(c,"statechange");a.busy(!1);
return!0},a.Adapter.bind(c,"popstate",a.onPopState),a.pushState=function(b,d,e,f){if(a.getHashByUrl(e)&&a.emulated.pushState)throw Error("History.js does not support states with fragement-identifiers (hashes/anchors).");if(!1!==f&&a.busy())return a.pushQueue({scope:a,callback:a.pushState,args:arguments,queue:f}),!1;a.busy(!0);var h=a.createStateObject(b,d,e);a.isLastSavedState(h)?a.busy(!1):(a.storeState(h),a.expectedStateId=h.id,r.pushState(h.id,h.title,h.url),a.Adapter.trigger(c,"popstate"));return!0},
a.replaceState=function(b,d,e,f){if(a.getHashByUrl(e)&&a.emulated.pushState)throw Error("History.js does not support states with fragement-identifiers (hashes/anchors).");if(!1!==f&&a.busy())return a.pushQueue({scope:a,callback:a.replaceState,args:arguments,queue:f}),!1;a.busy(!0);var h=a.createStateObject(b,d,e);a.isLastSavedState(h)?a.busy(!1):(a.storeState(h),a.expectedStateId=h.id,r.replaceState(h.id,h.title,h.url),a.Adapter.trigger(c,"popstate"));return!0});if(d)try{a.store=n.parse(d.getItem("History.store"))||
{}}catch(q){a.store={}}else a.store={};a.normalizeStore();a.Adapter.bind(c,"unload",a.clearAllIntervals);a.saveState(a.storeState(a.extractState(a.getLocationHref(),!0)));d&&(a.onUnload=function(){var b,c;try{b=n.parse(d.getItem("History.store"))||{}}catch(e){b={}}b.idToState=b.idToState||{};b.urlToId=b.urlToId||{};b.stateToId=b.stateToId||{};for(c in a.idToState)a.idToState.hasOwnProperty(c)&&(b.idToState[c]=a.idToState[c]);for(c in a.urlToId)a.urlToId.hasOwnProperty(c)&&(b.urlToId[c]=a.urlToId[c]);
for(c in a.stateToId)a.stateToId.hasOwnProperty(c)&&(b.stateToId[c]=a.stateToId[c]);a.store=b;a.normalizeStore();b=n.stringify(b);try{d.setItem("History.store",b)}catch(f){if(f.code===DOMException.QUOTA_EXCEEDED_ERR)d.length&&(d.removeItem("History.store"),d.setItem("History.store",b));else throw f;}},a.intervalList.push(m(a.onUnload,a.options.storeInterval)),a.Adapter.bind(c,"beforeunload",a.onUnload),a.Adapter.bind(c,"unload",a.onUnload));if(!a.emulated.pushState&&(a.bugs.safariPoll&&a.intervalList.push(m(a.safariStatePoll,
a.options.safariPollInterval)),"Apple Computer, Inc."===b.vendor||"Mozilla"===(b.appCodeName||""))&&(a.Adapter.bind(c,"hashchange",function(){a.Adapter.trigger(c,"popstate")}),a.getHash()))a.Adapter.onDomLoad(function(){a.Adapter.trigger(c,"hashchange")})};a.options&&a.options.delayInit||a.init()})(window);
