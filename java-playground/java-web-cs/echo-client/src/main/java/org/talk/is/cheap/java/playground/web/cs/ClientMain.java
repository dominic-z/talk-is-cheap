package org.talk.is.cheap.java.playground.web.cs;

import org.talk.is.cheap.java.playground.web.cs.client.EchoClient;

import java.io.IOException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Main
 * @date 2022/4/6 10:58 上午
 */
public class ClientMain {

    public static void main(String[] args) throws IOException {
        final EchoClient echoClient = new EchoClient(8080);
        echoClient.start();
    }
}
