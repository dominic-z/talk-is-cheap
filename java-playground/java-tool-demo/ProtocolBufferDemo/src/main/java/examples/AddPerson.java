package examples;// See README.txt for information and build instructions.

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.Timestamp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AddPerson {
    // This function fills in a Person message based on user input.
    public static Person PromptForAddress() {
        Person.Builder person = Person.newBuilder();

        System.out.println("Enter person ID: ");
        person.setId(Integer.parseInt("123"));

        System.out.println("Enter name: ");
        person.setName("personName");

        System.out.println("Enter email address (blank for none): ");
        String email = "dom@123456.com";
        person.setEmail(email);


        System.out.println("Enter a phone number (or leave blank to finish): ");
        String number = "123";

        Person.PhoneNumber.Builder phoneNumber =
                Person.PhoneNumber.newBuilder().setNumber(number);

        System.out.println("Is this a mobile, home, or work phone? ");
        String type = "mobile";
        if (type.equals("mobile")) {
            phoneNumber.setType(Person.PhoneType.MOBILE);
        } else if (type.equals("home")) {
            phoneNumber.setType(Person.PhoneType.HOME);
        } else if (type.equals("work")) {
            phoneNumber.setType(Person.PhoneType.WORK);
        } else {
            System.out.println("Unknown phone type.  Using default.");
        }

        person.addPhones(phoneNumber);

        person.setLastUpdated(Timestamp.newBuilder().setSeconds(123).build());
        return person.build();
    }

    // Main function:  Reads the entire address book from a file,
    //   adds one person based on user input, then writes it back out to the same
    //   file.
    public static void main(String[] args) throws Exception {
        String inputFilePath="ProtocolBufferDemo/addressbook";//生成的二进制文件

        AddressBook.Builder addressBook = AddressBook.newBuilder();

        // Read the existing address book.
        try {
            try (FileInputStream input = new FileInputStream(inputFilePath)) {
                addressBook.mergeFrom(input);//从二进制文件中读取message然后覆盖到本builder中
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.  Creating a new file.");
        }

        // Add an address.
        addressBook.addPeople(PromptForAddress());

        // Write the new address book back to disk.
        try (FileOutputStream output = new FileOutputStream(inputFilePath)) {
            addressBook.build().writeTo(output);
        }
    }
}
