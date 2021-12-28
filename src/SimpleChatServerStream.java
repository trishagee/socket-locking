import java.io.BufferedReader;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SimpleChatServerStream {
  ArrayList<Writer> clientOutputStreams;

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
        Writer writer = new OutputStreamWriter(Channels.newOutputStream(clientSocket), StandardCharsets.UTF_8);
        clientOutputStreams.add(writer);
        Thread t = new Thread(new ClientHandler(clientSocket));
        t.start();
        System.out.println("got a connection");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  } // close go

  public void tellEveryone(String message) {
    for (Writer writer : clientOutputStreams) {
      try {
        writer.append(message).write("\n");
        writer.flush();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } // end while
  } // close tellEveryone

  public class ClientHandler implements Runnable {
    BufferedReader reader;
    SocketChannel socket;

    public ClientHandler(SocketChannel clientSocket) {
      try {
        socket = clientSocket;
        reader = new BufferedReader(new InputStreamReader(Channels.newInputStream(socket), StandardCharsets.UTF_8));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } // close constructor

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
    } // close run
  } // close inner class
} // close class