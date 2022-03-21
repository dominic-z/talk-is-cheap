package collections;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectorsDemo {
    @Data
    @EqualsAndHashCode
    private static class Skill {
        private String skillName;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class EmployerWithSkill extends Skill {
        EmployerWithSkill(String skillName, int salary) {
            this.setSkillName(skillName);
            this.setSalary(salary);
        }

        private int salary;
    }

    @Test
    public void toMap() {
        List<EmployerWithSkill> employerWithSkillList = Arrays.asList(
                new EmployerWithSkill("eat", 100),
                new EmployerWithSkill("eat", 200),
                new EmployerWithSkill("sleep", 200)
        );

        Map<Skill, EmployerWithSkill> mergedMap = employerWithSkillList.stream().collect(
                Collectors.toMap(
                        employerWithSkill -> {
                            Skill skill = new Skill();
                            skill.setSkillName(employerWithSkill.getSkillName());
                            return skill;
                        },
                        employerWithSkill -> employerWithSkill,
                        (e1, e2) -> new EmployerWithSkill(e1.getSkillName(), e1.getSalary() + e2.getSalary())
                )
        );
        System.out.println(mergedMap);

    }

}
