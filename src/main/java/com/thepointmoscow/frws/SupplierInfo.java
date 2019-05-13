package com.thepointmoscow.frws;

import lombok.Data;

import java.util.List;

@Data
public class SupplierInfo {
    List<String> supplierPhones;
    String supplierName;
    String supplierInn;
}
