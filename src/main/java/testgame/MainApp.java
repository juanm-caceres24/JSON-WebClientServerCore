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
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Web Port (default: 8080): ");
        int port = 8080;
        String portInput = scanner.nextLine();
        if (!portInput.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Using 8080.");
            }
        }
        scanner.close();

        System.out.println("Use the followng URLs to connect to the server:");
        System.out.println(" >> Localhost: http://127.0.0.1:" + port);
        printNetworkInterfaces(port);
        printPublicIp(port);

        TicTacToeLogic gameLogic = new TicTacToeLogic();
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
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("100.")) {
                            System.out.println(" >> Tailscale VPN: http://" + ip + ":" + port);
                        } else {
                            System.out.println(" >> Local LAN (" + iface.getName() + "): http://" + ip + ":" + port);
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
                String publicIp = in.readLine();
                System.out.println(" >> Public IP (Required Port Forward): http://" + publicIp + ":" + port);
            }
        } catch (Exception e) {
            System.out.println(" >> Public IP: Unavailable");
        }
    }
}
