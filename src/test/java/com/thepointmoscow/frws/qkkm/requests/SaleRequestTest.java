package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaleRequestTest {

    @Test
    void shouldSerializeSaleCommand() throws IOException {
        // GIVEN
        XmlMapper mapper = new XmlMapper();
        Sale sale = new Sale();
        sale.setAmount(1500).setPrice(200).setGroup("grp-1")
                .setTax1(1).setTax2(2).setTax3(3).setTax4(4).setText("text");
        QkkmRequest command = new SaleRequest().setSale(sale);
        // WHEN
        String result = mapper.writeValueAsString(command);
        // THEN
        assertEquals("<ControlProtocol messageType=\"request\">" +
                "<Sale Text=\"text\" Amount=\"1500\" Price=\"200\"" +
                " Group=\"grp-1\" Tax1=\"1\" Tax2=\"2\" Tax3=\"3\" Tax4=\"4\"/></ControlProtocol>", result);
    }

    @Test
    void shouldSerializeReturnSaleCommand() throws IOException {
        // GIVEN
        XmlMapper mapper = new XmlMapper();
        Sale sale = new Sale();
        sale.setAmount(1500).setPrice(200).setGroup("grp-1")
                .setTax1(1).setTax2(2).setTax3(3).setTax4(4).setText("text");
        QkkmRequest command = new ReturnSaleRequest().setSale(sale);
        // WHEN
        String result = mapper.writeValueAsString(command);
        // THEN
        assertEquals("<ControlProtocol messageType=\"request\">" +
                "<ReturnSale Text=\"text\" Amount=\"1500\" Price=\"200\"" +
                " Group=\"grp-1\" Tax1=\"1\" Tax2=\"2\" Tax3=\"3\" Tax4=\"4\"/></ControlProtocol>", result);
    }
}