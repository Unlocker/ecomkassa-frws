package com.thepointmoscow.frws;

import com.google.common.base.Strings;
import com.thepointmoscow.frws.umka.ItemVatType;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class Order {
    private Long _id;
    private String orderType;
    private String status;
    private String saleCharge;
    private Firm firm;
    private Cashier cashier;
    private Customer customer;
    private List<Item> items = Collections.emptyList();
    private List<Payment> payments = Collections.emptyList();
    private Boolean isElectronic;
    private Correction correction;
    private String additionalCheckProperty;

    @Data
    @Accessors(chain = true)
    public static class Firm {
        private String timezone;
        private TaxVariant taxVariant;
        private String address;
        private String taxIdentityNumber;
    }

    @Data
    @Accessors(chain = true)
    public static class Cashier {
        private String firstName;
        private String lastName;

        @Override
        public String toString() {
            return firstName + " " + lastName;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Customer {
        private String phone;
        private String email;
        private String name;
        private String taxNumber;

        public String getId() {
            return !Strings.isNullOrEmpty(email) ? email : phone;
        }
    }

    @Data
    @Accessors(chain = true)
    @Slf4j
    public static class Item {
        private String name;
        private Long price;
        private Long amount;
        private ItemVatType vatType;
        private String paymentMethod;
        private String paymentObject;
        private String measurementUnit;
        private String userData;
        private SupplierInfo supplier;
        private AgentInfo agent;
        private String nomenclatureCode;

        public PaymentMethod paymentMethod() {
            final var paymentMethodDefault = PaymentMethod.FULL_PAYMENT;
            try {
                return Optional.ofNullable(paymentMethod)
                        .map(PaymentMethod::valueOf)
                        .orElse(paymentMethodDefault);
            } catch (Exception e) {
                log.warn(
                        "Cannot parse a payment method from '{}', switched to the default '{}'"
                        , paymentMethod
                        , paymentMethodDefault
                );
                return paymentMethodDefault;
            }
        }

        public PaymentObject paymentObject() {
            final var paymentObjectDefault = PaymentObject.COMMODITY;
            try {
                return Optional.ofNullable(paymentObject)
                        .map(PaymentObject::valueOf)
                        .orElse(paymentObjectDefault);
            } catch (Exception e) {
                log.warn(
                        "Cannot parse a payment object from '{}', switched to the default '{}'"
                        , paymentObject
                        , paymentObjectDefault
                );
                return paymentObjectDefault;
            }
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Payment {
        private String paymentType;
        private Long amount;
    }

    @Data
    @Accessors(chain = true)
    public static class Correction {
        private String correctionType;
        private ItemVatType vatType;
        private String description;
        private String documentDate;
        private String documentNumber;
    }
}
