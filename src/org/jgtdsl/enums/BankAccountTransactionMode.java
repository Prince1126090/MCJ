package org.jgtdsl.enums;

public enum BankAccountTransactionMode {

	CASH(0,"Cash"),
	CHECK(1,"Check"),
	BANK_ORDER(2,"Bank Order");

    private String label;
    private int id;

    private BankAccountTransactionMode(int id,String label) {
        this.id=id;
    	this.label = label;
    }

    public String getLabel() {
        return label;
    }
    public int getId() {
        return id;
    }
    
}
