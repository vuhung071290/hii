package com.hii.server;

import com.hii.BaseVerticleTestSuite;
import com.hii.model.ChatContainerRequest;
import com.hii.model.ChatMessageRequest;
import com.hii.model.IWsMessage;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class WsServerTestSuite extends BaseVerticleTestSuite {
  @Test
  public void test_handleChatContainerRequest(TestContext context) {
    final Async async = context.async();
    getClient()
        .websocket(
            8090,
            getHost(),
            "?jwt=" + getJwtToTest(),
            wsConnect -> {
              wsConnect.handler(
                  event -> {
                    context.assertTrue(true);
                    wsConnect.close();
                    async.complete();
                  });
              ChatContainerRequest chatContainerRequest = new ChatContainerRequest();
              chatContainerRequest.setType(IWsMessage.TYPE_CHAT_ITEM_REQUEST);
              chatContainerRequest.setSessionId(getSessionIdToTest());
              wsConnect.writeTextMessage(Json.encodePrettily(chatContainerRequest));
            });
  }

  @Test
  public void test_handleChatMessageRequest(TestContext context) {
    final Async async = context.async();
    getClient()
        .websocket(
            8090,
            getHost(),
            "?jwt=" + getJwtToTest(),
            wsConnect -> {
              wsConnect.handler(
                  event -> {
                    context.assertTrue(true);
                    wsConnect.close();
                    async.complete();
                  });
              ChatMessageRequest chatMessageRequest = new ChatMessageRequest();
              chatMessageRequest.setType(IWsMessage.TYPE_CHAT_MESSAGE_REQUEST);
              chatMessageRequest.setMessage("Test Message");
              chatMessageRequest.setSessionId("-1");
              chatMessageRequest.setGroupChat(true);
              List<String> users = new ArrayList<String>();
              users.add(getAnotherUserIdToTest());
              chatMessageRequest.setUsernames(users);
              wsConnect.writeTextMessage(Json.encodePrettily(chatMessageRequest));
            });
  }
}
