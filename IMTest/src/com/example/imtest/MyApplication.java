package com.example.imtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {

	private static XMPPConnection connection;
	private static ConnectionConfiguration config;
	private static Map<String, List<HashMap<String, String>>> offlineMsg;
	@Override
	public void onCreate() {
		super.onCreate();
		offlineMsg = new HashMap<String, List<HashMap<String, String>>>();  
		configure(ProviderManager.getInstance());
	}

    public static Map<String, List<HashMap<String, String>>> getOfflineMsg() {
		return offlineMsg;
	}




	public static void setOfflineMsg(Map<String, List<HashMap<String, String>>> offlineMsg) {
		MyApplication.offlineMsg = offlineMsg;
	}




    public void configure(ProviderManager pm) {   
	    pm.addIQProvider("query", "jabber:iq:private",

	            new PrivateDataManager.PrivateDataIQProvider());

	    try {

	        pm.addIQProvider("query", "jabber:iq:time",

	                Class.forName("org.jivesoftware.smackx.packet.Time"));

	    } catch (ClassNotFoundException e) {

	        Log.w("TestClient",

	                "Can't load class for org.jivesoftware.smackx.packet.Time");

	    }
	    pm.addExtensionProvider("x", "jabber:x:roster",

	            new RosterExchangeProvider());
	    pm.addExtensionProvider("x", "jabber:x:event",

	            new MessageEventProvider());
	    pm.addExtensionProvider("active",

	            "http://jabber.org/protocol/chatstates",

	            new ChatStateExtension.Provider());
	    pm.addExtensionProvider("composing",

	            "http://jabber.org/protocol/chatstates",

	            new ChatStateExtension.Provider());
	    pm.addExtensionProvider("paused",

	            "http://jabber.org/protocol/chatstates",

	            new ChatStateExtension.Provider());

	    pm.addExtensionProvider("inactive",

	            "http://jabber.org/protocol/chatstates",

	            new ChatStateExtension.Provider());
	    pm.addExtensionProvider("gone",

	            "http://jabber.org/protocol/chatstates",

	            new ChatStateExtension.Provider());
	    pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",

	            new XHTMLExtensionProvider());

	    pm.addExtensionProvider("x", "jabber:x:conference",

	            new GroupChatInvitation.Provider());
	    pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
	            new DiscoverItemsProvider());

	    pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",

	            new DiscoverInfoProvider());
	    pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
	    pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
	            new MUCUserProvider());
	    pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
	            new MUCAdminProvider());

	    pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
	            new MUCOwnerProvider());
	    pm.addExtensionProvider("x", "jabber:x:delay",
	            new DelayInformationProvider());
	    try {

	        pm.addIQProvider("query", "jabber:iq:version",

	                Class.forName("org.jivesoftware.smackx.packet.Version"));

	    } catch (ClassNotFoundException e) {

	        // Not sure what's happening here.

	    }
	    pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
	    pm.addIQProvider("offline", "http://jabber.org/protocol/offline",

	            new OfflineMessageRequest.Provider());
	    pm.addExtensionProvider("offline","http://jabber.org/protocol/offline",new OfflineMessageInfo.Provider());
	    pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
	    pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
	    pm.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup",new SharedGroupsInfo.Provider());
	    pm.addExtensionProvider("addresses","http://jabber.org/protocol/address",new MultipleAddressesProvider());
	    pm.addIQProvider("si", "http://jabber.org/protocol/si",new StreamInitiationProvider());
	    pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
	            new BytestreamsProvider());
	    pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

	}
	
	public static XMPPConnection getConnection() {
		return connection;
	}


	public static void setConnection(XMPPConnection connection) {
		MyApplication.connection = connection;
	}


	public static boolean conServer() {  
		
	    config = new ConnectionConfiguration(  
	            "115.200.44.131",5222);  
	    /** 是否启用安全验证 */  
	    config.setSASLAuthenticationEnabled(false);  
	    config.setSecurityMode(SecurityMode.disabled);
	    config.setCompressionEnabled(false);
	    config.setSendPresence(false);
	    /** 是否启用调试 */  
	    // config.setDebuggerEnabled(true);  
	    /** 创建connection链接 */  
	    try {  
	        connection = new XMPPConnection(config);  
	        /** 建立连接 */  
	        connection.connect();  
	        return true;  
	    } catch (XMPPException e) {  
	        e.printStackTrace();  
	    }  
	    return false;  
	}  
}
