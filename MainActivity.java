package com.stark.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    WebView web; TextToSpeech tts; SharedPreferences prefs; java.util.concurrent.ExecutorService worker = Executors.newSingleThreadExecutor();
    @Override public void onCreate(Bundle b) { super.onCreate(b); prefs=getSharedPreferences("jarvis",0); tts=new TextToSpeech(this,this); web=new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.setWebChromeClient(new WebChromeClient()); web.setWebViewClient(new WebViewClient()); web.addJavascriptInterface(new Bridge(),"JarvisAndroid"); setContentView(web); web.loadUrl("file:///android_asset/index.html"); }
    @Override public void onInit(int status) { if(status==TextToSpeech.SUCCESS){ tts.setLanguage(new Locale("tr","TR")); tts.setSpeechRate(0.91f); tts.setPitch(0.82f); } }
    void js(String s){ runOnUiThread(()->web.evaluateJavascript("window.onJarvisReply && window.onJarvisReply("+JSONObject.quote(s)+")",null)); }
    class Bridge {
        @JavascriptInterface public String getEndpoint(){return prefs.getString("endpoint","");}
        @JavascriptInterface public void saveEndpoint(String e){prefs.edit().putString("endpoint",e.trim().replaceAll("/$", "")).apply();}
        @JavascriptInterface public void speak(String s){runOnUiThread(()->{if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis");});}
        @JavascriptInterface public void listen(){runOnUiThread(()->{if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},7);js("Mikrofon izni istendi. İzin verdikten sonra yeniden dene.");return;} try { Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr-TR"); startActivityForResult(i,8); }catch(Exception e){js("Bu cihazda konuşma tanıma başlatılamadı.");}});}
        @JavascriptInterface public void ask(String message){String endpoint=prefs.getString("endpoint",""); if(endpoint.isEmpty()){js("Önce aşağıdaki ayarlara HTTPS yapay zekâ sunucusu adresini gir. API anahtarını bu uygulamaya yazma.");return;} worker.execute(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL(endpoint+"/api/chat").openConnection();c.setRequestMethod("POST");c.setConnectTimeout(12000);c.setReadTimeout(45000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");try(OutputStreamWriter w=new OutputStreamWriter(c.getOutputStream(),"UTF-8")){w.write(new JSONObject().put("message",message).toString());} int code=c.getResponseCode(); java.io.InputStream in=code<400?c.getInputStream():c.getErrorStream();java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream(); byte[] chunk=new byte[4096]; int n; while((n=in.read(chunk))!=-1) buffer.write(chunk,0,n); String body=new String(buffer.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);String reply=new JSONObject(body).optString("reply","Yanıt alınamadı.");js(reply);}catch(Exception e){js("Sunucuya bağlanamadım. HTTPS adresini ve internet bağlantısını kontrol et.");}finally{if(c!=null)c.disconnect();}});}
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==8&&resultCode==RESULT_OK&&data!=null){java.util.ArrayList<String> r=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(r!=null&&!r.isEmpty())js("__HEARD__"+r.get(0));}}
    @Override public void onDestroy(){if(tts!=null)tts.shutdown();worker.shutdown();if(web!=null)web.destroy();super.onDestroy();}
}
