package codegen.test;

import codegen.test.dao.StudentMapper;
import codegen.test.dao.example.StudentExample;
import codegen.test.pojo.mbg.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public void run(String... args) throws Exception {

        Student student1 = new Student();
        student1.setAge((byte) 12);
        student1.setName("ni");
        student1.setPhoto("ha");
        Student student2 = new Student();
        student2.setAge((byte) 1);
        student2.setName("hao");
        student2.setPhoto("hoho");

        HashSet<String> excludColNames = new HashSet<>(Set.of(Student.SEX, Student.PHOTO));
        studentMapper.insertBatchSelective(List.of(student1,student2), excludColNames);


        StudentExample studentExample = new StudentExample();
        studentExample.createCriteria().andIdEqualTo(12);
        studentMapper.updateByExampleSelective(new Student().withName("ninini"),studentExample);
    }
}
