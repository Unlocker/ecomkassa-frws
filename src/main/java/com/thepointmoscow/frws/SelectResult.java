package com.thepointmoscow.frws;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SelectResult extends StatusResult {
    private Status status;

    public SelectResult() {
        super("SELECT");
    }

    @Data
    @Accessors(chain = true)
    public static class Status {
        /**
         * ИНН кассы
         */
        private String taxNumber;
        /**
         * РН кассы
         */
        private String regNumber;
        /**
         * ЗН кассы
         */
        private String serialNumber;
        /**
         * ФН
         */
        private String storageNumber;
        /**
         * ФД
         */
        private String docNumber;
        /**
         * Дата документа
         */
        private ZonedDateTime docDate;
        /**
         * Документ целиком, как был получен от кассового сервиса
         */
        private String payload;
    }

}
