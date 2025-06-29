package org.gs.dto;

import org.gs.model.Transfer;

public class TransferResult {
     private Transfer outgoingTransaction;
    //private Transfer incomingTransaction;
 
    public TransferResult(Transfer outgoingTransaction /*Transfer incomingTransaction*/) {
        this.outgoingTransaction = outgoingTransaction;
        //this.incomingTransaction = incomingTransaction;
    }

    public Transfer getOutgoingTransaction() {
        return outgoingTransaction;
    }
    public void setOutgoingTransaction(Transfer outgoingTransaction) {
        this.outgoingTransaction = outgoingTransaction;
    }
    // public Transfer getIncomingTransaction() {
    //     return incomingTransaction;
    // }
    // public void setIncomingTransaction(Transfer incomingTransaction) {
    //     this.incomingTransaction = incomingTransaction;
    // }

}
