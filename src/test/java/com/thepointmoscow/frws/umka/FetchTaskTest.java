package com.thepointmoscow.frws.umka;

import com.thepointmoscow.frws.*;
import com.thepointmoscow.frws.exceptions.FiscalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powermock.reflect.Whitebox;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UtilityConfig.class})
public class FetchTaskTest {

    public static final String CCM_ID = "1";
    private StatusResult status;
    private BackendGateway backendGatewayMock;
    private FiscalGateway fiscalGatewayMock;
    private FetchTask fetchTask;

    @BeforeEach
    void setup() {
        status = new StatusResult()
                .setOnline(true)
                .setErrorCode(0)
                .setFrDateTime(LocalDateTime.of(2012, 12, 11, 20, 3, 20))
                .setInn("7705293503")
                .setSerialNumber("0030070002059014")
                .setCurrentDocNumber(194)
                .setCurrentSession(21)
                .setModeFR((byte) 2)
                .setSubModeFR((byte) 0)
                .setStatusMessage("OK");

        backendGatewayMock = mock(BackendGateway.class);
        fiscalGatewayMock = mock(FiscalGateway.class);
        fetchTask = new FetchTask(backendGatewayMock, fiscalGatewayMock, null, Collections.singletonList(CCM_ID));
    }

    @Test
    void doRoundShouldDoNone() throws Exception {
        // GIVEN
        when(fiscalGatewayMock.status()).thenReturn(status);
        when(fiscalGatewayMock.closeSession()).thenThrow(new RuntimeException());

        when(backendGatewayMock.status(CCM_ID, status)).thenReturn(new BackendCommand().setCommand(BackendCommand.BackendCommandType.NONE));

        // WHEN
        boolean res = Whitebox.invokeMethod(fetchTask, "doRound", CCM_ID);

        // THEN
        assertThat(res).isFalse();
        verify(fiscalGatewayMock, times(1)).status();
    }

    @Test
    void doRoundShouldDoRegister() throws Exception {
        // GIVEN
        RegistrationResult registrationResult = new RegistrationResult();
        when(fiscalGatewayMock.status()).thenReturn(status);
        when(fiscalGatewayMock.register(null, null, false)).thenReturn(registrationResult);
        when(backendGatewayMock.status(CCM_ID, status)).thenReturn(new BackendCommand().setCommand(BackendCommand.BackendCommandType.REGISTER));
        BackendCommand backendCommand = new BackendCommand();
        when(backendGatewayMock.register(CCM_ID, registrationResult)).thenReturn(backendCommand);


        // WHEN
        boolean res = Whitebox.invokeMethod(fetchTask, "doRound", CCM_ID);


        // THEN
        assertThat(res).isTrue();
        verify(fiscalGatewayMock, times(1)).register(null, null, false);
        verify(backendGatewayMock, times(1)).register(CCM_ID, registrationResult);
    }

    @Test
    void doRoundShouldDoSelectDoc() throws Exception {
        // GIVEN
        SelectResult selectResult = new SelectResult();
        when(fiscalGatewayMock.status()).thenReturn(status);
        when(fiscalGatewayMock.selectDoc("123")).thenReturn(selectResult);
        when(backendGatewayMock.status(CCM_ID, status)).thenReturn(new BackendCommand().setCommand(BackendCommand.BackendCommandType.SELECT_DOC).setDocumentNumber("123").setIssueID(2L));
        BackendCommand backendCommand = new BackendCommand();
        when(backendGatewayMock.selectDoc(CCM_ID, null, selectResult)).thenReturn(backendCommand);


        // WHEN
        boolean res = Whitebox.invokeMethod(fetchTask, "doRound", CCM_ID);


        // THEN
        assertThat(res).isTrue();
        verify(fiscalGatewayMock, times(1)).selectDoc("123");
        verify(backendGatewayMock, times(1)).selectDoc(CCM_ID, 2L, selectResult);
    }


    @Test
    void doRoundShouldCloseSession() throws Exception {
        // GIVEN
        StatusResult statusResult = new StatusResult();
        when(fiscalGatewayMock.status()).thenReturn(status);
        when(fiscalGatewayMock.closeSession()).thenReturn(statusResult);
        when(backendGatewayMock.status(CCM_ID, status)).thenReturn(new BackendCommand().setCommand(BackendCommand.BackendCommandType.CLOSE_SESSION).setDocumentNumber("123").setIssueID(2L));


        // WHEN
        boolean res = Whitebox.invokeMethod(fetchTask, "doRound", CCM_ID);


        // THEN
        assertThat(res).isFalse();
        verify(fiscalGatewayMock, times(1)).closeSession();
    }

    @Test
    void doRoundShouldSendError() throws Exception {
        // GIVEN
        when(fiscalGatewayMock.status()).thenReturn(status);
        FiscalResultError fiscalResultError = new FiscalResultError(1, "ERROR");
        when(fiscalGatewayMock.closeSession()).thenThrow(new FiscalException(fiscalResultError));
        when(backendGatewayMock.status(CCM_ID, status)).thenReturn(new BackendCommand().setCommand(BackendCommand.BackendCommandType.CLOSE_SESSION).setDocumentNumber("123").setIssueID(2L));


        // WHEN
        boolean res = Whitebox.invokeMethod(fetchTask, "doRound", CCM_ID);


        // THEN
        assertThat(res).isFalse();
        verify(fiscalGatewayMock, times(1)).closeSession();
        verify(backendGatewayMock, times(1)).error(CCM_ID, 2L, fiscalResultError);

    }

}
