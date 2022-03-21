package service.chapter1_7;

import org.springframework.context.ApplicationEvent;

public class RollbackEvent extends ApplicationEvent {
    private String rollbackInfo;
    public RollbackEvent(Object source, String str) {
        super(source);
        this.rollbackInfo =str;
    }

    public String getRollbackInfo() {
        return rollbackInfo;
    }

    public void setRollbackInfo(String rollbackInfo) {
        this.rollbackInfo = rollbackInfo;
    }
}

