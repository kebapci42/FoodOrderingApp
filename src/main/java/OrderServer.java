import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class OrderServer {

    public static void main(String[] args) {
        int port = 6000;

        System.out.println("🟢 Order Server started on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                System.out.println("\n📦 New Order Received:");
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }

                clientSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
