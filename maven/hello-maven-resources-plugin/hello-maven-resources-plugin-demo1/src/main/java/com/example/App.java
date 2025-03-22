package com.example;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class App {

    public static void main(String[] args) {
        readProp();
        readYML();
        readOther();
    }

    private static void readYML() {
        System.out.println("readYAML");
        try (InputStream ymlStream = App.class.getClassLoader().getResourceAsStream("application.yml")) {
            Map<String, Object> load = new Yaml().load(ymlStream);
            System.out.println(load);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void readProp() {
        System.out.println("readProps");

        try {
            Properties properties = new Properties();
            properties.load(App.class.getClassLoader().getResourceAsStream("application.properties"));
            System.out.println("config.env URL: " + properties.getProperty("config.env"));
            System.out.println("Database URL: " + properties.getProperty("database.url"));
            System.out.println("Database Username: " + properties.getProperty("database.username"));
            System.out.println("Database Password: " + properties.getProperty("database.password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void readOther() {
        System.out.println("readOther");

        try {
            InputStream inputStream = App.class.getClassLoader().getResourceAsStream("other.text");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line=bufferedReader.readLine())!=null){
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}