package org.talk.is.cheap.java.playground.web.cs;

import org.talk.is.cheap.java.playground.web.cs.server.BlockingEchoServer;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Main
 * @date 2022/4/6 9:53 上午
 */
public class ServerMain {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        final BlockingEchoServer blockingEchoServer = new BlockingEchoServer(PORT);
        blockingEchoServer.start();


    }
}
