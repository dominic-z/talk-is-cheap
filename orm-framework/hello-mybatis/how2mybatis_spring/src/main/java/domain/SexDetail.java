package domain;

public enum SexDetail {
    A(0,"male"),B(1,"female");

    @Override
    public String toString() {
        return "SexDetail{" +
                "i=" + i +
                ", gender='" + gender + '\'' +
                '}';
    }

    private int i;
    private String gender;
    private SexDetail(int i, String gender) {
        this.i=i;
        this.gender=gender;
    }

    public int getI() {
        return i;
    }

    public String getGender() {
        return gender;
    }
}
