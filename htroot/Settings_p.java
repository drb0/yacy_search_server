// Settings.p.java 
// -----------------------
// part of the AnomicHTTPD caching proxy
// (C) by Michael Peter Christen; mc@anomic.de
// first published on http://www.anomic.de
// Frankfurt, Germany, 2004
// last change: 02.05.2004
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Using this software in any meaning (reading, learning, copying, compiling,
// running) means that you agree that the Author(s) is (are) not responsible
// for cost, loss of data or any harm that may be caused directly or indirectly
// by usage of this softare or this documentation. The usage of this software
// is on your own risk. The installation and usage (starting/running) of this
// software may allow other people or application to access your computer and
// any attached devices and is highly dependent on the configuration of the
// software which must be done by the user of the software; the author(s) is
// (are) also not responsible for proper configuration and usage of the
// software, even if provoked by documentation provided together with
// the software.
//
// Any changes to this file according to the GPL as documented in the file
// gpl.txt aside this file in the shipment you received can be done to the
// lines that follows this copyright notice here, but changes must not be
// done inside the copyright notive above. A re-distribution must contain
// the intact and unchanged copyright notice.
// Contributions and changes to the program code must be marked as such.

// You must compile this file with
// javac -classpath .:../Classes Settings_p.java
// if the shell's current path is HTROOT

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import de.anomic.http.httpHeader;
import de.anomic.plasma.plasmaParser;
import de.anomic.plasma.plasmaParserConfig;
import de.anomic.plasma.plasmaSwitchboard;
import de.anomic.plasma.parser.ParserInfo;
import de.anomic.server.serverObjects;
import de.anomic.server.serverSwitch;
import de.anomic.yacy.yacyCore;
import de.anomic.yacy.yacySeedUploader;

public final class Settings_p {
    
    public static serverObjects respond(httpHeader header, serverObjects post, serverSwitch env) {
        // return variable that accumulates replacements
        serverObjects prop = new serverObjects();
        
        //if (post == null) System.out.println("POST: NULL"); else System.out.println("POST: " + post.toString());
        
        String page = (post == null) ? "general" : post.get("page", "general");
        
        if (page.equals("ProxyAccess")) {
            prop.put("settingsTables", "Settings_ProxyAccess.inc");
        }
        else if (page.equals("http")) {
            prop.put("settingsTables", "Settings_Http.inc");
        }
        else if (page.equals("proxy")) {
            prop.put("settingsTables", "Settings_Proxy.inc");
        }
        else if (page.equals("ServerAccess")) {	
            prop.put("settingsTables", "Settings_ServerAccess.inc");
        }
        else if (page.equals("SystemBehaviour")) {
            prop.put("settingsTables", "Settings_SystemBehaviour.inc");
        }
        else if (page.equals("seed")) {
            prop.put("settingsTables", "Settings_Seed.inc");
        }
        else if (page.equals("messageForwarding")) {
            prop.put("settingsTables", "Settings_MessageForwarding.inc");
        }
        else if (page.equals("portForwarding")) {
            prop.put("settingsTables", "Settings_PortForwarding.inc");
        }
        else if (page.equals("parser")) {
            prop.put("settingsTables", "Settings_Parser.inc");
        }
        else if (page.equals("crawler")) {
            prop.put("settingsTables", "Settings_Crawler.inc");
        } else {
            prop.put("settingsTables", "");
        }

        prop.put("port", env.getConfig("port", "8080"));               
        
        prop.putHTML("peerName", env.getConfig("peerName", "nameless"));
        prop.put("staticIP", env.getConfig("staticIP", ""));
        String peerLang = env.getConfig("locale.language", "default");
        if (peerLang.equals("default")) peerLang = "en";
        prop.put("peerLang", peerLang);
        
        // http networking settings
        prop.put("isTransparentProxy", env.getConfig("isTransparentProxy", "false").equals("true") ? "1" : "0"); 
        prop.put("connectionKeepAliveSupport", env.getConfig("connectionKeepAliveSupport", "false").equals("true") ? "1" : "0");
        prop.put("proxy.sendViaHeader", env.getConfig("proxy.sendViaHeader", "false").equals("true") ? "1" : "0");
        prop.put("proxy.sendXForwardedForHeader", env.getConfig("proxy.sendXForwardedForHeader", "true").equals("true") ? "1" : "0");
        
        // remote port forwarding settings
        String[] forwardingMethods = new String[]{"sch","upnp"};
        String currentForwarder = env.getConfig("portForwarding.Type", "none");
        boolean portForwardingAvailable = false;
        int methodCount = 0;
        
        for (int i=0; i < forwardingMethods.length; i++) {
            try {            
                //Class forwarder = Class.forName(env.getConfig("portForwarding." + forwardingMethods[i],"")); // FIXME: why is forwarder declared, but never used?
                prop.put("forwardingMethods_" + methodCount + "_name",forwardingMethods[i]);
                prop.put("forwardingMethods_" + methodCount + "_selected", forwardingMethods[i].equals(currentForwarder)?1:0);
                methodCount++;
            } catch (Exception e) {
            } catch (Error e) {}
        }
        prop.put("forwardingMethods", methodCount);
        
        if (methodCount > 0) portForwardingAvailable = true;
        
        prop.put("portForwarding.Type",currentForwarder);
        prop.put("portForwarding.Available",portForwardingAvailable? "1" : "0");
        prop.put("portForwarding.Enabled",env.getConfig("portForwarding.Enabled","false").equals("true")? "1" : "0");
        
        prop.put("portForwarding.sch.UseProxy",env.getConfig("portForwarding.sch.UseProxy", "false").equals("true") ? "1" : "0");
        prop.put("portForwarding.sch.Port",env.getConfig("portForwarding.sch.Port", ""));        
        prop.put("portForwarding.sch.Host",env.getConfig("portForwarding.sch.Host", ""));
        prop.put("portForwarding.sch.HostPort",env.getConfig("portForwarding.sch.HostPort", ""));
        prop.put("portForwarding.sch.HostUser",env.getConfig("portForwarding.sch.HostUser", ""));
        prop.put("portForwarding.sch.HostPwd",env.getConfig("portForwarding.sch.HostPwd", ""));
        
        // set values
        String s;
        int pos;
        
        // admin password
        if (env.getConfig("adminAccountBase64", "").length() == 0) {
            // no password has been specified
            prop.put("adminuser","admin");
        } else {
            s = env.getConfig("adminAccount", "admin:void");
            pos = s.indexOf(":");
            if (pos < 0) {
                prop.put("adminuser","admin");
            } else {
                prop.put("adminuser",s.substring(0, pos));
            }
        }
        
        // remote proxy
        prop.put("remoteProxyUseChecked", env.getConfig("remoteProxyUse", "false").equals("true") ? 1 : 0);
        prop.put("remoteProxyUse4Yacy", env.getConfig("remoteProxyUse4Yacy", "true").equals("true") ? 1 : 0);
        prop.put("remoteProxyUse4SSL", env.getConfig("remoteProxyUse4SSL", "true").equals("true") ? 1 : 0);
        
        prop.put("remoteProxyHost", env.getConfig("remoteProxyHost", ""));
        prop.put("remoteProxyPort", env.getConfig("remoteProxyPort", ""));
        
        prop.put("remoteProxyUser", env.getConfig("remoteProxyUser", ""));
        prop.put("remoteProxyPwd", env.getConfig("remoteProxyPwd", ""));
        
        prop.put("remoteProxyNoProxy", env.getConfig("remoteProxyNoProxy", ""));
        
        // proxy access filter
        prop.putHTML("proxyfilter", env.getConfig("proxyClient", "*"));
        
        // proxy password
        if ( env.getConfig("use_proxyAccounts", "false").equals("false") ) {
            // no password has been specified
            prop.put("use_proxyAccounts", "0"); //unchecked
        } else {
            prop.put("use_proxyAccounts", "1"); //checked
            /*s = env.getConfig("proxyAccount", "proxy:void");
            pos = s.indexOf(":");
            if (pos < 0) {
                prop.put("proxyuser","proxy");
            } else {
                prop.put("proxyuser",s.substring(0, pos));
            }*/
        }
        
        // server access filter
        prop.putHTML("serverfilter", env.getConfig("serverClient", "*"));
        
        // server password
        if (env.getConfig("serverAccountBase64", "").length() == 0) {
            // no password has been specified
            prop.put("serveruser","server");
        } else {
            s = env.getConfig("serverAccount", "server:void");
            pos = s.indexOf(":");
            if (pos < 0) {
                prop.put("serveruser","server");
            } else {
                prop.put("serveruser",s.substring(0, pos));
            }
        }
        
        // clientIP
        prop.putHTML("clientIP", (String) header.get("CLIENTIP", "<unknown>"), true); // read an artificial header addendum
        
        /* 
         * seed upload settings
         */
        // available methods
        String enabledUploader = env.getConfig("seedUploadMethod", "none");
        
        // for backward compatiblity ....
        if ((enabledUploader.equalsIgnoreCase("Ftp")) || 
                ((enabledUploader.equals("")) &&
                        (env.getConfig("seedFTPPassword","").length() > 0) &&
                        (env.getConfig("seedFilePath", "").length() > 0))) {
            enabledUploader = "Ftp";
            env.setConfig("seedUploadMethod",enabledUploader);
        }                  
        
        HashMap<String, String> uploaders = yacyCore.getSeedUploadMethods();
        prop.put("seedUploadMethods", uploaders.size() + 1);
        prop.put("seedUploadMethods_0_name", "none");
        prop.put("seedUploadMethods_0_selected", enabledUploader.equals("none") ? "1" : "0");
        prop.put("seedUploadMethods_0_file", "");
        
        int count = 0;
        Iterator<String> uploaderKeys = uploaders.keySet().iterator();
        while (uploaderKeys.hasNext()) {
            count++;
            String uploaderName = uploaderKeys.next();
            prop.put("seedUploadMethods_" +count+ "_name", uploaderName);
            prop.put("seedUploadMethods_" +count+ "_selected", uploaderName.equals(enabledUploader) ? "1" : "0");            
            prop.put("seedUploadMethods_" +count+ "_file", "Settings_Seed_Upload" + uploaderName + ".inc");
            
            yacySeedUploader theUploader = yacyCore.getSeedUploader(uploaderName);
            String[] configOptions = theUploader.getConfigurationOptions();
            if (configOptions != null) {
                for (int i=0; i<configOptions.length; i++) {
                    prop.put("seedUploadMethods_" +count+ "_" + configOptions[i], env.getConfig(configOptions[i], ""));
                    // prop.put("seedUpload" + uploaderName,1);
                }
            }
        }
        
        // general settings
        prop.put("seedURL", env.getConfig("seedURL", ""));
        
        /*
         * Message forwarding configuration
         */
        prop.put("msgForwardingEnabled",env.getConfig("msgForwardingEnabled","false").equals("true") ? "1" : "0");
        prop.put("msgForwardingCmd",env.getConfig("msgForwardingCmd", ""));
        prop.put("msgForwardingTo",env.getConfig("msgForwardingTo", ""));
        
        /*
         * Parser Configuration
         */
        plasmaSwitchboard sb = (plasmaSwitchboard)env;
        
        HashMap<String, plasmaParserConfig> configList = plasmaParser.getParserConfigList();        
        plasmaParserConfig[] configArray = (plasmaParserConfig[]) configList.values().toArray(new plasmaParserConfig[configList.size()]);
        
        HashSet<ParserInfo> parserInfos = new HashSet<ParserInfo>(sb.parser.getAvailableParserList().values());
        
//        // fetching a list of all available mimetypes
//        List availableParserKeys = Arrays.asList(availableParsers.entrySet().toArray(new ParserInfo[availableParsers.size()]));
//        
//        // sort it
//        Collections.sort(availableParserKeys);
        
        // loop through the mimeTypes and add it to the properties
        boolean[] allParsersEnabled = new boolean[configList.size()];
        for (int i=0; i<configArray.length; i++)
        	allParsersEnabled[i] = true;
        int parserIdx = 0;
        
        Iterator<ParserInfo> availableParserIter = parserInfos.iterator();
        while (availableParserIter.hasNext()) {
            ParserInfo parserInfo = availableParserIter.next();
            prop.put("parser_" + parserIdx + "_name", parserInfo.parserName);
            prop.putHTML("parser_" + parserIdx + "_version", parserInfo.parserVersionNr, true);
            prop.put("parser_" + parserIdx + "_usage", parserInfo.usageCount);
            prop.put("parser_" + parserIdx + "_colspan", configArray.length);
            
            int mimeIdx = 0;
            Enumeration<String> mimeTypeIter = parserInfo.supportedMimeTypes.keys();
            while (mimeTypeIter.hasMoreElements()) {
                String mimeType = mimeTypeIter.nextElement();
                
                prop.put("parser_" + parserIdx + "_mime_" + mimeIdx + "_mimetype", mimeType);
                //prop.put("parser_" + parserIdx + "_name", parserName);
                //prop.put("parser_" + parserIdx + "_shortname", parserName.substring(parserName.lastIndexOf(".")+1));
                for (int i=0; i<configArray.length; i++) {
                    HashSet<String> enabledParsers =  configArray[i].getEnabledParserList();
                    prop.put("parser_" + parserIdx + "_mime_" + mimeIdx + "_parserMode_" + i + "_optionName", configArray[i].parserMode + "." + mimeType);
                    prop.put("parser_" + parserIdx + "_mime_" + mimeIdx + "_parserMode_" + i + "_status", enabledParsers.contains(mimeType) ? "1" : "0");
                    allParsersEnabled[i] &= enabledParsers.contains(mimeType);
                }
                prop.put("parser_" + parserIdx + "_mime_" + mimeIdx + "_parserMode", configArray.length);
                mimeIdx++;
            }
            prop.put("parser_" + parserIdx + "_mime", mimeIdx);
            
            parserIdx++;
        }
        
        for (int i=0; i<configArray.length; i++) {
            prop.put("parserMode_" + i + "_name",configArray[i].parserMode);
            prop.put("parserMode_" + i + "_allParserEnabled",allParsersEnabled[i] ? "1" : "0");
        }
        prop.put("parserMode",configArray.length);
        prop.put("parser", parserIdx);
        prop.put("parser.colspan", configArray.length+2);
        
        // Crawler settings
        prop.put("crawler.clientTimeout",sb.getConfig("crawler.clientTimeout", "10000"));
        prop.put("crawler.http.maxFileSize",sb.getConfig("crawler.http.maxFileSize", "-1"));
        prop.put("crawler.ftp.maxFileSize",sb.getConfig("crawler.ftp.maxFileSize", "-1"));
        
        // return rewrite properties
        return prop;
    }
    
}
