$wnd.showcase.runAsyncCallback24("function Eqb(a){this.a=a}\nfunction Gqb(a){this.a=a}\nfunction Iqb(a){this.a=a}\nfunction Nqb(a,b){this.a=a;this.b=b}\nfunction sUb(a){return XIb(),a.hb}\nfunction wUb(a,b){pUb(a,b);gp((XIb(),a.hb),b)}\nfunction OIb(){var a;if(!LIb||RIb()){a=new jgc;QIb(a);LIb=a}return LIb}\nfunction RIb(){var a=$doc.cookie;if(a!=MIb){MIb=a;return true}else{return false}}\nfunction gp(b,c){try{b.remove(c)}catch(a){b.removeChild(b.childNodes[c])}}\nfunction SIb(a){NIb&&(a=encodeURIComponent(a));$doc.cookie=a+'=;expires=Fri, 02-Jan-1970 00:00:00 GMT'}\nfunction Bqb(a){var b,c,d,e;if(sUb(a.c).options.length<1){BWb(a.a,'');BWb(a.b,'');return}e=sUb(a.c).selectedIndex;b=tUb(a.c,e);c=(d=OIb(),FO(b==null?fbc(Bgc(d.d,null)):Rgc(d.e,b)));BWb(a.a,b);BWb(a.b,c)}\nfunction Aqb(a,b){var c,d,e,f,g,h;gh(a.c).options.length=0;h=0;e=new tcc(OIb());for(d=(g=e.a.Vh().fc(),new ycc(g));d.a.Og();){c=(f=BO(d.a.Pg(),36),FO(f._h()));oUb(a.c,c);E8b(c,b)&&(h=gh(a.c).options.length-1)}um((nm(),mm),new Nqb(a,h))}\nfunction QIb(b){var c=$doc.cookie;if(c&&c!=''){var d=c.split('; ');for(var e=d.length-1;e>=0;--e){var f,g;var h=d[e].indexOf('=');if(h==-1){f=d[e];g=''}else{f=d[e].substring(0,h);g=d[e].substring(h+1)}if(NIb){try{f=decodeURIComponent(f)}catch(a){}try{g=decodeURIComponent(g)}catch(a){}}b.Xh(f,g)}}}\nfunction zqb(a){var b,c,d;c=new wSb(3,3);a.c=new yUb;b=new eMb('Supprimer');Fh((XIb(),b.hb),Fpc,true);RRb(c,0,0,'<b><b>Cookies existants:<\\/b><\\/b>');URb(c,0,1,a.c);URb(c,0,2,b);a.a=new KWb;RRb(c,1,0,'<b><b>Nom:<\\/b><\\/b>');URb(c,1,1,a.a);a.b=new KWb;d=new eMb('Sauvegarder Cookie');Fh(d.hb,Fpc,true);RRb(c,2,0,'<b><b>Valeur:<\\/b><\\/b>');URb(c,2,1,a.b);URb(c,2,2,d);Mh(d,new Eqb(a),(It(),It(),Ht));Mh(a.c,new Gqb(a),(Bt(),Bt(),At));Mh(b,new Iqb(a),(null,Ht));Aqb(a,null);return c}\nC8(466,1,zmc,Eqb);_.Sc=function Fqb(a){var b,c,d;c=xWb(this.a.a);d=xWb(this.a.b);b=new rN($7(e8((new pN).q.getTime()),Mqc));if((B8b(),c).length<1){$wnd.alert('Vous devez indiquer un nom de cookie');return}TIb(c,d,b);Aqb(this.a,c)};var KZ=J7b(Nmc,'CwCookies/1',466);C8(467,1,Amc,Gqb);_.Rc=function Hqb(a){Bqb(this.a)};var LZ=J7b(Nmc,'CwCookies/2',467);C8(468,1,zmc,Iqb);_.Sc=function Jqb(a){var b,c;c=gh(this.a.c).selectedIndex;if(c>-1&&c<gh(this.a.c).options.length){b=tUb(this.a.c,c);SIb(b);wUb(this.a.c,c);Bqb(this.a)}};var MZ=J7b(Nmc,'CwCookies/3',468);C8(469,1,Imc);_.Bc=function Mqb(){Vab(this.b,zqb(this.a))};C8(470,1,{},Nqb);_.Dc=function Oqb(){this.b<gh(this.a.c).options.length&&xUb(this.a.c,this.b);Bqb(this.a)};_.b=0;var OZ=J7b(Nmc,'CwCookies/5',470);var LIb=null,MIb;zjc(Bl)(24);\n//# sourceURL=showcase-24.js\n")