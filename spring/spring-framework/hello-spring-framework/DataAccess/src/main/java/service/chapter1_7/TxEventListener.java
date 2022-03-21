package service.chapter1_7;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


public class TxEventListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleRollbackEvent(RollbackEvent rollbackEvent){
        System.out.println("rollback event");
    }
}
