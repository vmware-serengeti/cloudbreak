package com.sequenceiq.cloudbreak.service.stack.handler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.Status;
import com.sequenceiq.cloudbreak.domain.User;
import com.sequenceiq.cloudbreak.domain.WebsocketEndPoint;
import com.sequenceiq.cloudbreak.repository.RetryingStackUpdater;
import com.sequenceiq.cloudbreak.service.stack.event.StackOperationFailure;
import com.sequenceiq.cloudbreak.websocket.WebsocketService;

import reactor.event.Event;

public class StackCreationFailureHandlerTest {

    public static final String DUMMY_EMAIL = "gipszjakab@myemail.com";
    public static final String STACK_NAME = "stackName";
    @InjectMocks
    private StackCreationFailureHandler underTest;

    @Mock
    private RetryingStackUpdater stackUpdater;

    @Mock
    private WebsocketService websocketService;

    private Event<StackOperationFailure> event;

    private Stack stack;

    @Before
    public void setUp() {
        underTest = new StackCreationFailureHandler();
        MockitoAnnotations.initMocks(this);
        event = createEvent();
        stack = createStack();
    }

    @Test
    public void testAcceptStackCreationFailureEvent() {
        // GIVEN
        given(stackUpdater.updateStackStatus(anyLong(), any(Status.class), anyString())).willReturn(stack);
        doNothing().when(websocketService).sendToTopicUser(anyString(), any(WebsocketEndPoint.class), any());
        // WHEN
        underTest.accept(event);
        // THEN
        verify(websocketService, times(1)).sendToTopicUser(anyString(), any(WebsocketEndPoint.class), any());
    }

    private Event<StackOperationFailure> createEvent() {
        StackOperationFailure data = new StackOperationFailure(1L, "message");
        return new Event<StackOperationFailure>(data);
    }

    private Stack createStack() {
        Stack stack = new Stack();
        stack.setId(1L);
        Cluster cluster = new Cluster();
        cluster.setEmailNeeded(false);
        stack.setCluster(cluster);
        stack.setName(STACK_NAME);
        User user = new User();
        user.setEmail(DUMMY_EMAIL);
        stack.setUser(user);
        return stack;
    }
}