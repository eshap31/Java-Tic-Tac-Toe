package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication with a client
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private GameServer gameServer;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running = true;

    /**
     * Creates a new client handler
     * @param clientSocket The client socket
     * @param gameServer The game server
     */
    public ClientHandler(Socket clientSocket, GameServer gameServer) {
        this.clientSocket = clientSocket;
        this.gameServer = gameServer;

        try {
            // Set up input/output streams
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
            close();
        }
    }

    /**
     * Main loop for handling client messages
     */
    @Override
    public void run() {
        try {
            String inputLine;
            while (running && (inputLine = in.readLine()) != null) {
                processMessage(inputLine);
            }
        } catch (IOException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            close();
        }
    }

    /**
     * Processes a message from the client
     * @param message The message to process
     */
    private void processMessage(String message) {
        System.out.println("Received message: " + message);

        // Split the message into parts
        String[] parts = message.split(":");
        String command = parts[0];

        try {
            switch (command) {
                case "REGISTER":
                    // Format: REGISTER:playerName:boardSize
                    if (parts.length >= 3) {
                        String playerName = parts[1];
                        int boardSize = Integer.parseInt(parts[2]);
                        gameServer.registerPlayer(this, playerName, boardSize);
                    }
                    break;

                case "MOVE":
                    // Format: MOVE:gameId:row:col
                    if (parts.length >= 4) {
                        int gameId = Integer.parseInt(parts[1]);
                        int row = Integer.parseInt(parts[2]);
                        int col = Integer.parseInt(parts[3]);
                        gameServer.processMove(this, gameId, row, col);
                    }
                    break;

                case "DISCONNECT":
                    close();
                    break;

                default:
                    System.out.println("Unknown command: " + command);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            sendMessage("ERROR:Invalid message format");
        }
    }

    /**
     * Sends a message to the client
     * @param message The message to send
     */
    public void sendMessage(String message) {
        if (out != null && !clientSocket.isClosed()) {
            out.println(message);
        }
    }

    /**
     * Closes the connection
     */
    public void close() {
        running = false;

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }

            // Notify the server
            gameServer.disconnectClient(this);

        } catch (IOException e) {
            System.err.println("Error closing client handler: " + e.getMessage());
        }
    }

    /**
     * Returns the client's address
     */
    public String getClientAddress() {
        return clientSocket.getInetAddress().getHostAddress();
    }
}