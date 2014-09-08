package echoserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Utils;

public class EchoServer {

  private static boolean keepRunning = true;
  private static ServerSocket serverSocket;
  private static final Properties properties = Utils.initProperties("server.properties");

  private List<ClientHandler> handlers = Collections.synchronizedList(new ArrayList<ClientHandler>());

  public static void stopServer() {
    keepRunning = false;
  }

  public static void main(String[] args) {
    int port = Integer.parseInt(properties.getProperty("port"));
    String ip = properties.getProperty("serverIp");
    String logFile = properties.getProperty("logFile");
    new EchoServer().runServer(logFile, ip, port);
  }

  public synchronized void sendToAll(String msg) {
    String msgUpperCased = msg.toUpperCase();
    for (ClientHandler handler : handlers) {
      handler.send(msgUpperCased);
    }
  }

  void addHandler(ClientHandler ch) {
    handlers.add(ch);
  }

  void removeHandler(ClientHandler ch) {
    handlers.remove(ch);
  }

  private void runServer(String logFile, String ip, int port) {
    Utils.setLogFile(logFile, EchoServer.class.getName());

    Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Sever started");
    try {
      serverSocket = new ServerSocket();
      serverSocket.bind(new InetSocketAddress(ip, port));
      do {
        Socket socket = serverSocket.accept(); //Important Blocking call
        //socket.setSoTimeout(60*60*30);
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Connected to a client");
        new ClientHandler(socket, this).start();
      } while (keepRunning);
    } catch (IOException ex) {
      Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      Utils.closeLogger(EchoServer.class.getName());
    }
  }
}
