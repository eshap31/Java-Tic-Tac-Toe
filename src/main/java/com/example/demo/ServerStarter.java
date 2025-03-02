package com.example.demo;

/*
runs the server
 */
public class ServerStarter
{
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}