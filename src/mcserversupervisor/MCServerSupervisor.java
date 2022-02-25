package mcserversupervisor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;

import static mcserversupervisor.MCServerSupervisor.serv;

import org.eclipse.paho.client.mqttv3.*;


public class MCServerSupervisor {
    static Properties prop = new Properties();
    static String fileName = "supervisor.config";
    
    static String ipaddress;
    static String pass;
    static String username;
    static String commandTopic;
    static String servControlTopic;
    static String sysControlTopic;
    static boolean gui;
    static String serverfile;
    static String serverDir = null;
    
    static OutputStream stdIn = null;
    
    static Process serv = null;
    static StartExp1 m1 = null;
    static IMqttClient client = null;

    public static void main(String[] args) throws Exception{
        try (FileInputStream fis = new FileInputStream(fileName)) {
        prop.load(fis);
        }
        serverfile = prop.getProperty("server_file");
        ipaddress = prop.getProperty("broker_address");
        pass = prop.getProperty("broker_pass");
        username = prop.getProperty("broker_user");
        if(prop.getProperty("server_gui", "false") == "true") {
            gui = true;
        } else {
            gui = false;
        }
        commandTopic = prop.getProperty("topic_command");
        servControlTopic = prop.getProperty("topic_control_server");
        sysControlTopic = prop.getProperty("topic_control_supervisor");
        if (prop.containsKey("working_directory")) serverDir = prop.getProperty("working_directory");
        String publisherId = UUID.randomUUID().toString(); 
        client = new MqttClient("tcp://"+ipaddress,publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setPassword(pass.toCharArray());
        options.setUserName(username);
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        
        client.connect(options);
        
        m1 = new StartExp1();
        
        client.subscribe(commandTopic, (topiccmd, recmsgcmd) -> {
            
        byte[] payloadcmd = recmsgcmd.getPayload();
        String strcmd=new String(payloadcmd);
            System.out.println("[Supervisor]: Received Command \""+strcmd+"\", passing it to the MC Server.");
            sendToServer(strcmd);
        });
        
        client.subscribe(servControlTopic, (topic, recmsg) -> {
            
        byte[] payload = recmsg.getPayload();
        String str=new String(payload);
        if (str.equalsIgnoreCase("ON")) {
            if (serv == null) {
            System.out.println("[SUPERVISOR]: Server Starting...");
            activateServer();
            } else {
            System.out.println("[SUPERVISOR]: Already Running!");    
        }
        }
        if (str.equalsIgnoreCase("OFF")) {
            if (serv != null) {
                System.out.println("[SUPERVISOR]: Server Stopping...");
                stopServer();
            } else {
                System.out.println("[SUPERVISOR]: Server isn't running! Start it first.");
            }
            
        }   
        });
        
        client.subscribe(sysControlTopic, (topicsys, recmsgsys) -> {
        byte[] payloadsys = recmsgsys.getPayload();
        String strsys=new String(payloadsys);  
        if (strsys.equalsIgnoreCase("OFF")) {
            System.out.println("[SUPERVISOR]: Shutting Down...");
            client.unsubscribe("servercontrol");
            client.unsubscribe("systemcontrolcontrol");
            client.disconnectForcibly();
            System.out.println("[SUPERVISOR]: Shut Down.");
        }
        }); 
        System.out.println("[SUPERVISOR]: Waiting for Start Signal...");
        
    }
    
    static void activateServer() {
        m1.start();
    }
    static void stopServer() {
        sendToServer("stop");
        m1.interrupt();
    }
    static void sendToServer(String cmd) {
        try {
        String cmdstring = cmd + "\n";    
        stdIn.write (cmdstring.getBytes ("US-ASCII"));
        stdIn.flush ();
        
        } catch (IOException e) {
        
        }
    }
}

class StartExp1 extends Thread {
    public void run(){
        while (!MCServerSupervisor.m1.isInterrupted()) {
            try {
                ProcessBuilder pb;
                if (MCServerSupervisor.gui) {
                    pb = new ProcessBuilder("java", "-jar", MCServerSupervisor.serverfile);
                } else {
                    pb = new ProcessBuilder("java", "-jar", MCServerSupervisor.serverfile, "nogui");
                }
                if (MCServerSupervisor.serverDir != null) pb.directory(new File(MCServerSupervisor.serverDir));
                serv = pb.start();
                BufferedReader in = new BufferedReader(new InputStreamReader(serv.getInputStream()));
                MCServerSupervisor.stdIn = serv.getOutputStream();
                String s = "";
                while((s = in.readLine()) != null){
                    System.out.println(s);
                }
            
            } catch (IOException e) {
            
            } 
            
        }
        
        System.out.println("[Thread]: Stopping...");
        serv.destroyForcibly();
        System.out.println("[Thread]: Stopped");
   
    }
}