package com.gft.digitalbank.exchange.actors;

import com.gft.digitalbank.exchange.engine.TransactionRegister;
import com.gft.digitalbank.exchange.engine.TransactionRegisterImpl;

/**
 * Created by krzysztof on 31/07/16.
 */
public class TransactionRegisterActor {

    private final TransactionRegister transactionRegister;

    public TransactionRegisterActor() {
        this.transactionRegister = new TransactionRegisterImpl();
    }

}
