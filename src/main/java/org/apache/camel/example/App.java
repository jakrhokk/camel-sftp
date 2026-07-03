package org.apache.camel.example;

import org.apache.camel.main.Main;

public final class App {

    private App() {
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new SftpToS3RouteBuilder());
        main.run(args);
    }
}
