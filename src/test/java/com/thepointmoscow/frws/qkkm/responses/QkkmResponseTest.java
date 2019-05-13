package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QkkmResponseTest {

    @Test
    void shouldParseTheResponse() throws IOException {
        // GIVEN
        XmlMapper mapper = new XmlMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        String response = "<ControlProtocol messageType=\"request\"><error id=\"1\" text=\"some text\"/>" +
                "<CutCheck/></ControlProtocol>";
        // WHEN
        QkkmResponse val = mapper.readValue(response, QkkmResponse.class);
        // THEN
        assertNotNull(val);
        assertNotNull(val.getError());
        assertEquals(1, val.getError().getId());
        assertEquals("some text", val.getError().getText());
    }

}