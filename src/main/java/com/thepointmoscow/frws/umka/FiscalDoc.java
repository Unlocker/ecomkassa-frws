package com.thepointmoscow.frws.umka;

import lombok.Data;

@Data
class FiscalDoc {
    private String sessionId;
    private int print = 0;
    private int result = 0;
    private FiscalData data;
}
