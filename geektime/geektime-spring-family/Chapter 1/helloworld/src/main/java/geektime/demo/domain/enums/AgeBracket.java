package geektime.demo.domain.enums;

/**
 * @author dominiczhu
 * @date 2020/8/11 12:53 下午
 */
public enum AgeBracket {
    YOUTH("0-29"), MIDDLE_AGE("30-49"), OLD_AGE("50-90");

    private final String ageBracketDescr;

    AgeBracket(String ageBracketDescr) {
        this.ageBracketDescr = ageBracketDescr;
    }

    public String getAgeBracketDescr() {
        return ageBracketDescr;
    }
}
