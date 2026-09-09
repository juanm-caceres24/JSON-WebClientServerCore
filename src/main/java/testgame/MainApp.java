package testgame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;

import server.WebServerCore;

/**
 * Main application class that starts the Tic-Tac-Toe game server and provides network information for clients to connect.
 */
public class MainApp {
    private static final int DEFAULT_PORT = 9000;
    private static final String DEFAULT_LOCALHOST = "127.0.0.1";
    private static final String TICTACTOE_AI_URL = "tictactoe.ai.url";
    private static final String PREDICTION_URL_API = "http://127.0.0.1:8001/predict";
    private static final String PREDICTION_URL_NAME = "TICTACTOE_AI_URL";

    private static int port;
    private static String localIp;
    private static String publicIp;
    private static String predictionUrl;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Web Port (default: " + DEFAULT_PORT + "): ");
        port = DEFAULT_PORT;
        String portInput = scanner.nextLine();
        if (!portInput.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Using " + DEFAULT_PORT + ".");
            }
        }
        predictionUrl = System.getProperty(TICTACTOE_AI_URL, System.getenv().getOrDefault(PREDICTION_URL_NAME, PREDICTION_URL_API));
        System.out.println("Neural network API: " + predictionUrl);
        scanner.close();
        System.out.println("Use the followng URLs to connect to the server:");
        System.out.println(" >> Localhost: http://" + DEFAULT_LOCALHOST + ":" + port);
        printNetworkInterfaces(port);
        printPublicIp(port);
        TicTacToeLogic gameLogic = new TicTacToeLogic(predictionUrl);
        WebServerCore webServer = new WebServerCore(port, gameLogic);
        webServer.start();
    }

    private static void printNetworkInterfaces(int port) {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        localIp = addr.getHostAddress();
                        if (localIp.startsWith("100.")) {
                            System.out.println(" >> Tailscale VPN: http://" + localIp + ":" + port);
                        } else {
                            System.out.println(" >> Local LAN (" + iface.getName() + "): http://" + localIp + ":" + port);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading local network interfaces: " + e.getMessage());
        }
    }

    private static void printPublicIp(int port) {
        try {
            URL url = URI.create("https://api.ipify.org").toURL();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                publicIp = in.readLine();
                System.out.println(" >> Public IP (Required Port Forward): http://" + publicIp + ":" + port);
            }
        } catch (Exception e) {
            System.out.println(" >> Public IP: Unavailable");
        }
    }
}
