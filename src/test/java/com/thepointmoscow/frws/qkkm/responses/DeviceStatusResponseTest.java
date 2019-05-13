package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DeviceStatusResponseTest {

    @Test
    void shouldParseTheResponse() throws IOException {
        // GIVEN
        XmlMapper mapper = new XmlMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        String response = "<ControlProtocol messageType=\"answer\"><error id=\"1\" text=\"some text\"/>" +
                "<getDeviceStatus " +
                "isOnline=\"1\" " +
                "deviceErrorCode=\"0\" " +
                "dateFR=\"2012.12.11\" " +
                "timeFR=\"20:03:20\" " +
                "inn=\"88888888\" " +
                "serialNumber=\"123456\" " +
                "versionSoftFR=\"A.1\" " +
                "dateSoftFR=\"Пт мар 22 2002\" " +
                "buildSoftFR=\"4563\" " +
                "versionSoftFiscalMemory=\"1.1\" " +
                "dateSoftFiscalMemory=\"Пн сен 17 2001\" " +
                "numberFR=\"1\" " +
                "modeFR=\"8\" " +
                "subModeFR=\"0\" " +
                "flagsFiscalMemory=\"5\" " +
                "operatorNumber=\"30\" " +
                "portFR=\"0\" " +
                "countLeftRefiscalizations=\"14\" " +
                "currentDocNumber=\"284\" " +
                "numberLastClousedSession=\"631\" " +
                "countRefiscalizations=\"2\" " +
                "flagsFR=\"979\" " +
                "buildSoftFiscalMemory=\"31\" " +
                "countFreeRecordsInFiscalMemory=\"1469\" " +
                "isBufferNotEmpty=\"0\" " +
                "isCapOpen=\"0\" " +
                "isRollOperationJournalPresent=\"1\" " +
                "isOpticalSensorCheckTape=\"1\" " +
                "isDecimalPointPosition=\"1\" " +
                "isUpperSensorPresent=\"0\" " +
                "isLeverThermalHeadCheckTape=\"1\" " +
                "isLeverThermalHeadControlTape=\"1\" " +
                "isEklzFull=\"0\" " +
                "isErrorRightSensor=\"0\" " +
                "isErrorLeftSensor=\"0\" " +
                "isRollCheckTapePresent=\"1\" " +
                "isMoneyBoxOpen=\"0\" " +
                "isEklzPresent=\"0\" " +
                "isOpticalSensorOperationJournal=\"1\" " +
                "isLowerSensorPresent=\"0\" " +
                "statusMessageHTML=\"HTML-описание статуса\" />" +
                "</ControlProtocol>";
        // WHEN
        DeviceStatusResponse actual = mapper.readValue(response, DeviceStatusResponse.class);
        // THEN
        assertNotNull(actual);
        assertNotNull(actual.getError());
        assertEquals(1, actual.getError().getId());
        assertEquals("some text", actual.getError().getText());
        DeviceStatusResponse.DeviceStatus status = actual.getStatus();
        assertNotNull(status);
        assertEquals("1", status.getIsOnline());
        assertEquals(0, status.getDeviceErrorCode());
        assertEquals("2012.12.11", status.getDateFR());
        assertEquals("20:03:20", status.getTimeFR());
        assertEquals("88888888", status.getInn());
        assertEquals("123456", status.getSerialNumber());
        assertEquals(284, status.getCurrentDocNumber());
        assertEquals(631, status.getNumberLastClousedSession());
        assertEquals(8, status.getModeFR());
        assertEquals(0, status.getSubModeFR());
        assertEquals("HTML-описание статуса", status.getStatusMessageHTML());
    }


}