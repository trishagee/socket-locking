import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class SimpleChatServerStream {
  ArrayList<PrintWriter> clientOutputStreams;

  public static void main(String[] args) {
    new SimpleChatServerStream().go();
  }

  public void go() {
    clientOutputStreams = new ArrayList<>();
    try {
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.bind(new InetSocketAddress("localhost", 5000));

      while (true) {
        SocketChannel clientSocket = serverSocketChannel.accept();
        PrintWriter writer = new PrintWriter(Channels.newOutputStream(clientSocket));
        clientOutputStreams.add(writer);
        Thread t = new Thread(new ClientHandler(clientSocket));
        t.start();
        System.out.println("got a connection");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void tellEveryone(String message) {
    for (PrintWriter writer : clientOutputStreams) {
      try {
        writer.println(message);
        writer.flush();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  public class ClientHandler implements Runnable {
    BufferedReader reader;
    SocketChannel socket;

    public ClientHandler(SocketChannel clientSocket) {
      try {
        socket = clientSocket;
        InputStreamReader isReader = new InputStreamReader(Channels.newInputStream(socket));
        reader = new BufferedReader(isReader);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    public void run() {
      String message;
      try {
        while ((message = reader.readLine()) != null) {
          System.out.println("read " + message);
          tellEveryone(message);
        } // close while
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}