dotnAd.viewableActObj = {'300x250-r14':1, '300x250-r15':1, '300x250-r16':1, '970x250-top':1, '970x250-middle':1};
dotnAd.objAsync={};
dotnAd.asyncRequired = true;
dotnAd.advRefreshModule = 1;
dotnAd.advRefreshCount =1;
dotnAd.asyncRequired = true;
dotnAd.attachCssMHTop = true;



var dotSpotIntro = '';
var dotSpotVip = '';
var dotSpotInvedeeo = '';

function damAdShow(sizepos){
var damAdBanner='';
dotnAd.log("damAdShow"+sizepos);
try{
if(sizepos=='610x30-floorad'){
} else if(sizepos=='0x0-over'){
 var callOvl=true;dotnAd.isDisabledOvl=true;
 try{
  if(document.location.href!=null) {
   var myRe=/^(.*):\/\/([^\/]*)\/(.*)$/i;
   var mA = myRe.exec(document.location.href);  
   if(mA[2]!=""){
    var cN="dnadovli";var cM=30;var cD=mA[2];var cS=(mA[1]=="https")?true:false;
    if(dotnAd.getCookie(cN)==undefined){
     dotnAd.setCookie(cN,1,cM,"/","",cS);
    }else{callOvl=false;}
   }
  }
 }catch(e){}
 if(callOvl){
  
 }
}else if(sizepos=='300x250-r14'){


if (typeof dotnAd.isStickyTest !== "undefined") {
    var stickyData = {"flCapId":"-1","flightId": "-1", "adId": "-1", "mpId": "-1"};
    try{window.top.toStickAd=sizepos}catch(e){}
    dotnAd.gestineStickyBox = true;
    damAdBanner += ' <style> .extra .adv {height: auto; min-height: 250px; } </style>';
    damAdBanner += ' <style>'+dotnAd.getCssSticky("dotnAd-300x250-r14")+'</style>';
    dotnAd.attachSticky("dotnAd-300x250-r14", stickyData);
}
} else if(sizepos=='988x90-custom'){
}else if(sizepos=='970x250-top'){
   var hTop = 0; var strMHCSS = '';
   try {hTop  = jQuery(".header").height();} catch(e){}

   var damAdMPOStyle = ' .adv-970topcustom object{padding: 0 !important;} ' + strMHCSS + ' .adv-970topcustom{background-color: #ffffff;} ';
   if (dotnAd.attachCssMHTop) {
       damAdMPOStyle += dotnAd.getAnsa3CssMHTop();
   }
   damAdBanner += '<style>'+damAdMPOStyle+'</style>';
}else if(sizepos=='970x250-middle'){
}else if(sizepos=='984x45-strip'){
}else if(sizepos=='728x90-top'){
}else if(sizepos=='210x90-b14'){
}else if(sizepos=='210x90-b15'){
}else if(sizepos=='300x250-r15'){
damAdBanner+="<style>.extra .advr15{height:auto;}</style>";
}else if(sizepos=='300x250-r16'){
}else if(sizepos=='300x250-r17'){
}else if(sizepos=='viralize-inveedeo'){
}else if(sizepos=='300x100-casa'){
}else if(sizepos=='300x100-vd'){
}else if(sizepos=='300x60-casa'){
}else if(sizepos=='300x60-vd'){
}else if(sizepos=='234x90-head'){
}else if(sizepos=='300x150-bx'){
}else if(sizepos=='1x1-vertical'){
}
}catch(e){try{console.log("AB: "+sizepos+" -> "+e);}catch(e){}}
return damAdBanner;}
try{dotnAd.attachCssToHead('#banner970x30striphide, .pg-home #banner300x250r14hide{height: auto;}');}catch(e){}

dotnAd.advRefreshCB=function(){
  //Rimuoviamo i div dei precedenti redirect
  try{jQuery("div[id^='scr_'][id$='DIV']").remove();} catch(e) {}
  if (typeof window.noRemoveEyeDiv === 'undefined' || !window.noRemoveEyeDiv) {
    try {jQuery("#eyeDiv").remove();} catch(e) {}
  }
  try{jQuery("div[id^='TFIBVDynamicDiv']").remove(); $("div[id^='TFIBVSemiTransDiv']").remove();} catch(e){}
  
  //Elenco di position da aggiornare
  z={};z.type="s";z.size="300x250";z.pos="r14";dotnAd.callbackSerializer.add(z);
  z={};z.type="s";z.size="984x45";z.pos="strip";dotnAd.callbackSerializer.add(z);
}

try {
   dotnAd.attachRubiconSyncUserScript("0", "eu", "it");
} catch(e) {}