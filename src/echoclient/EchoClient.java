package echoclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

public class EchoClient extends Thread{

  Socket socket;
  private int port;
  private InetAddress serverAddress;
  private Scanner input;
  private PrintWriter output;
  List<EchoListener> listeners = new ArrayList();

  public void connect(String address, int port) throws UnknownHostException, IOException {
    this.port = port;
    serverAddress = InetAddress.getByName(address);
    socket = new Socket(serverAddress, port);
    input = new Scanner(socket.getInputStream());
    output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
    start();
    
  }

  public void send(String msg) throws IOException {
    if(socket.isOutputShutdown()){
      throw new IOException("Outbound socket is closed");
    }
    output.println(msg);
  }

  public void stopClient() throws IOException {
    output.println(ProtocolStrings.STOP);
    socket.shutdownOutput(); 
  }

  public void registerEchoListener(EchoListener l) {
    listeners.add(l);
  }

  public void unRegisterEchoListener(EchoListener l) {
    listeners.remove(l);
  }

  private void notifyListeners(String msg) {
    for (EchoListener listener : listeners) {
      listener.messageArrived(msg);
    }
  }

  public void run() {
    String msg = input.nextLine();
    while (!msg.equals(ProtocolStrings.STOP)) {
      notifyListeners(msg);
      msg = input.nextLine();
    }
    try {
      socket.close();
      input.close();
      input = null;
    } catch (IOException ex) {
      Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public String receive() {
    String msg = input.nextLine();
    if (msg.equals(ProtocolStrings.STOP)) {
      try {
        socket.close();
      } catch (IOException ex) {
        Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return msg;
  }

  public static void main(String[] args) throws InterruptedException {
    int port = 9090;
    String ip = "localhost";
    if (args.length == 2) {
      port = Integer.parseInt(args[0]);
      ip = args[1];
    }
    try {
      EchoClient tester = new EchoClient();
      tester.connect(ip, port);

      tester.registerEchoListener(new EchoListener() {

        @Override
        public void messageArrived(String data) {
          System.out.println("Got: " + data);
        }
      });
      //tester.stopClient();
      
     
      tester.send("Hello World");
      System.out.println("Press any key to stop");
      System.in.read();
      
      //System.in.read();      
    } catch (UnknownHostException ex) {
      Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
