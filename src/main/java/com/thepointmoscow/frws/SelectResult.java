package com.thepointmoscow.frws;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SelectResult extends StatusResult {
    private Document document;

    public SelectResult() {
        super("SELECT");
    }

    @Data
    @Accessors(chain = true)
    public static class Document {
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
        private JsonNode payload;
    }

}
