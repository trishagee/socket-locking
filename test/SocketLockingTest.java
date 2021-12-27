import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SocketLockingTest {
  @Test
  // this works
  void testSendAndReceiveOneClient() throws IOException, InterruptedException {
    // given
    int numberOfClients = 1;
    CountDownLatch latch = new CountDownLatch(numberOfClients);
    CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
    Socket socket = connect();
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfClients);

    // when
    String message = "Hi";
    createClientListeningForMessage(executorService, latch, results);
    assertTrue(send(socket, message));

    latch.await();
    checkReceivedMessages(results, message);

    // finally
    socket.close();
  }

  @Test
  // this does not, it hangs
  void testSendAndReceiveTwoClients() throws IOException, InterruptedException {
    // given
    int numberOfClients = 2;
    CountDownLatch latch = new CountDownLatch(numberOfClients);
    CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
    Socket socket = connect();
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfClients);

    // when
    String message = "Hi";
    createClientListeningForMessage(executorService, latch, results);
    createClientListeningForMessage(executorService, latch, results);
    assertTrue(send(socket, message));

    latch.await();
    checkReceivedMessages(results, message);

    // finally
    socket.close();
  }

  private void checkReceivedMessages(CopyOnWriteArrayList<String> results, String message) {
    for (String result : results) {
      Assertions.assertEquals(message, result);
    }
  }

  private void createClientListeningForMessage(ExecutorService executorService, CountDownLatch latch, CopyOnWriteArrayList<String> results) throws IOException {
    Socket socket = connect();
    executorService.submit(() -> {
      String messageReceived = receive(socket);
      results.add(messageReceived);
      latch.countDown();
      return messageReceived;
    });
  }

  private Socket connect() throws IOException {
    return new Socket("127.0.0.1", 5000);
  }

  public boolean send(Socket socket, String payload) throws IOException {
    PrintWriter writer = new PrintWriter(socket.getOutputStream());
    writer.println(payload);
    writer.flush();
    return true;
  }

  public String receive(Socket socket) {
    try (InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
         BufferedReader reader = new BufferedReader(streamReader)) {
      return reader.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}