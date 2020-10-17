package com.hii.service;

import com.hii.manager.JwtManager;
import com.hii.model.*;
import com.hii.repository.DataRepository;
import com.hii.util.GenerationUtils;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Getter
@Setter
public abstract class BaseService {

  public static final String AUTHENTICATION_SCHEME = "Bearer";

  protected DataRepository dataRepository;

  protected JwtManager jwtManager;

  public Future<User> insertUser(User user) {

    Future<User> future = Future.future();

    UserAuth userAuth = new UserAuth();
    user.setUserId(user.getUserId() != null ? user.getUserId() : GenerationUtils.generateId());
    userAuth.setUserName(user.getUserName());
    userAuth.setUserId(user.getUserId());
    userAuth.setHashedPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

    UserFull userFull = new UserFull();
    userFull.setUserId(user.getUserId());
    userFull.setUserName(user.getUserName());
    userFull.setFullName(user.getFullName());

    List<Future> insertUserAuthAndUserFullFuture = new ArrayList<>();
    insertUserAuthAndUserFullFuture.add(dataRepository.insertUserAuth(userAuth));
    insertUserAuthAndUserFullFuture.add(dataRepository.insertUserFull(userFull));

    CompositeFuture cp = CompositeFuture.all(insertUserAuthAndUserFullFuture);
    cp.setHandler(
        ar -> {
          if (ar.succeeded()) {
            future.complete(user);
          } else {
            future.fail(ar.cause());
          }
        });

    return future;
  }

  public Future<ChatMessage> insertChatMessagesAndUpdateChatListAndUpdateUnseenCount(
      ChatMessage chatMessage) {

    Future<ChatMessage> future = Future.future();

    // Find chat list key by session id
    String keyPattern = "chat:list:" + chatMessage.getSessionId() + "*";
    Future<List<String>> getKeysByPatternFuture = dataRepository.getKeysByPattern(keyPattern);
    getKeysByPatternFuture.compose(
        chatListKeys -> {

          // Get current chat list for update later
          Future<ChatList> getChatListFuture = dataRepository.getChatList(chatListKeys.get(0));
          getChatListFuture.compose(
              chatList -> {

                // Insert new chat message
                Future<ChatMessage> insertChatMessageFuture =
                    dataRepository.insertChatMessage(chatMessage);

                // Update chat list
                chatList.setLastMessage(chatMessage.getMessage());
                chatList.setUpdatedDate(chatMessage.getCreatedDate());
                Future<ChatList> insertChatListFuture = dataRepository.insertChatList(chatList);

                // increase unseen count
                List<String> userFriendIds = new ArrayList<>();
                for (UserHash userHash : chatList.getUserHashes()) {
                  if (!chatMessage.getUserHash().getUserId().equals(userHash.getUserId())) {
                    userFriendIds.add(userHash.getUserId());
                  }
                }
                Future<HashMap<String, Long>> increaseUnseenCountFuture =
                    increaseUnseenCount(userFriendIds, chatList.getSessionId());

                CompositeFuture cp =
                    CompositeFuture.all(
                        insertChatListFuture, insertChatMessageFuture, increaseUnseenCountFuture);
                cp.setHandler(
                    ar -> {
                      if (ar.succeeded()) {
                        future.complete(chatMessage);

                      } else {
                        throw new RuntimeException(ar.cause());
                      }
                    });
              },
              Future.future()
                  .setHandler(
                      handler -> {
                        future.fail(handler.cause());
                      }));
        },
        Future.future()
            .setHandler(
                handler -> {
                  future.fail(handler.cause());
                }));

    return future;
  }

  public Future<HashMap<String, Long>> increaseUnseenCount(List<String> userIds, String sessionId) {

    Future<HashMap<String, Long>> future = Future.future();

    List<Future> increaseUnseenCountFutures = new ArrayList<>();

    for (String userId : userIds) {
      increaseUnseenCountFutures.add(dataRepository.increaseUnseenCount(userId, sessionId));
    }

    CompositeFuture cp = CompositeFuture.all(increaseUnseenCountFutures);
    cp.setHandler(
        ar -> {
          if (ar.succeeded()) {

            HashMap<String, Long> userIdToUnseenCountMap = new HashMap<>();

            for (int index = 0; index < increaseUnseenCountFutures.size(); ++index) {
              Long unSeenCount = cp.resultAt(index);
              userIdToUnseenCountMap.put(userIds.get(index), unSeenCount);
            }

            future.complete(userIdToUnseenCountMap);

          } else {
            future.fail(ar.cause());
          }
        });

    return future;
  }
}
