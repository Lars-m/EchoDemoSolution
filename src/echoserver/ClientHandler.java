package echoserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

/**
 * @author Lars Mortensen
 */
public class ClientHandler extends Thread {

  Scanner input;
  PrintWriter writer;
  Socket socket;
  EchoServer server;

  public ClientHandler(Socket socket, EchoServer server) throws IOException {
    input = new Scanner(socket.getInputStream());
    writer = new PrintWriter(socket.getOutputStream(), true);
    this.socket = socket;
    this.server = server;
  }
  
  void send(String msg){
    writer.println(msg);
  }

  @Override
  public void run() {
    try{
    server.addHandler(this);
    String message = input.nextLine(); //IMPORTANT blocking call
    Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message));
    while (!message.equals(ProtocolStrings.STOP)) {
      //writer.println(message.toUpperCase());
      server.sendToAll(message);
      
      Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message.toUpperCase()));
      message = input.nextLine(); //IMPORTANT blocking call
    }
    }catch(NoSuchElementException  ste){
        Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, "Socket timed out", ste);
    }
    writer.println(ProtocolStrings.STOP);//Echo the stop message back to the client for a nice closedown
    try {
      input.close();
      writer.close();
      socket.close();
    } catch (IOException ex) {
      Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Closed a Connection");
      server.removeHandler(this);
    }
  }
}
