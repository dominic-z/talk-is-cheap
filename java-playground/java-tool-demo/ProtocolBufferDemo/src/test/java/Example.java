import com.example.tutorial.AddressBookProtos;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import examples.AddPerson;
import org.junit.Test;
import pkg1.S1;
import pkg2.S2;

import java.util.Arrays;



/**
 * @author dominiczhu
 * @date 2020/11/20 下午7:43
 * 需要先mvn compile
 */
public class Example {
    @Test
    public void constructMessage(){
        AddressBookProtos.Person.Builder personBuilder= AddressBookProtos.Person.newBuilder();
        personBuilder.setId(1);
        personBuilder.setName("name");
        personBuilder.setLastUpdated(Timestamp.newBuilder().setSeconds(123).build());
        AddressBookProtos.AddressBook.Builder addressBookBuilder=AddressBookProtos.AddressBook.newBuilder();
        addressBookBuilder.addPeople(personBuilder);
        System.out.println(addressBookBuilder.build());
    }

    @Test
    public void serialization() throws InvalidProtocolBufferException {
        AddressBookProtos.Person person=AddPerson.PromptForAddress();
        System.out.println(person.toByteString());
        System.out.println(Arrays.toString(person.toByteArray()));
        System.out.println(person);

        AddressBookProtos.Person newPerson=AddressBookProtos.Person.parseFrom(person.toByteString());
        System.out.println(newPerson);

        AddressBookProtos.Person newPerson2=AddressBookProtos.Person.parseFrom(person.toByteArray());
        System.out.println(newPerson2);
    }

    @Test
    public void mergeFromTest() throws InvalidProtocolBufferException {
        AddressBookProtos.Person person1=AddPerson.PromptForAddress();
        AddressBookProtos.Person person2=AddPerson.PromptForAddress();
        System.out.println(person1);


        AddressBookProtos.Person.Builder builder = AddressBookProtos.Person.newBuilder();
        builder.mergeFrom(person1);
        builder.mergeFrom(person2);
//        有builder有该方法，合并另外一个message对象，非repeated字段会覆盖，repeated字段则合并两个集合
        System.out.println(builder.build().getPhonesCount());
    }

    @Test
    public void serializationDiffProto() throws InvalidProtocolBufferException {
//        只要message一样，就可以成功反序列化
        S1.Student.Builder s1Builder=S1.Student.newBuilder().setName("abc").setAge(20).addBook("c").addBook("sha");
        ByteString bytesStr=s1Builder.build().toByteString();

        S2.Student s2=S2.Student.parseFrom(bytesStr);
    }
}
