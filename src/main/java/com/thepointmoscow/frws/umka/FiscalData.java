package com.thepointmoscow.frws.umka;

import lombok.Data;

import java.util.List;

@Data
class FiscalData {
    private String docName;
    private int moneyType;
    private long sum;
    private int type;
    private List<FiscalProperty> fiscprops;
}
