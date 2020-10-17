package com.hii.service;

import com.hii.model.*;
import com.hii.util.GenerationUtils;
import com.hii.util.PropertiesUtils;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ajbrown.namemachine.Gender;
import org.ajbrown.namemachine.NameGenerator;
import org.apache.commons.lang3.RandomUtils;
import se.emirbuc.randomsentence.RandomSentences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Setter
public class InitDataService extends BaseService {

  public Future<JsonObject> initData() {
    JsonObject jsonObject = new JsonObject();
    Future<JsonObject> future = Future.future();

    List<Future> insertAllFutures = new ArrayList<>();

    Future<List<User>> createUsersFuture = createUsers();
    createUsersFuture.compose(
        users -> {
          jsonObject.put("users", users);
          List<Future> insertUserFutures = new ArrayList<>();
          for (User user : users) {
            insertUserFutures.add(insertUser(user));
          }
          insertAllFutures.addAll(insertUserFutures);

          Future<List<UserStatus>> createUserStatusesFuture = createUserStatuses(users);
          Future<List<FriendList>> createFriendListsFuture = createFriendLists(users);
          Future<List<ChatList>> createChatListsFuture = createChatLists(users);

          CompositeFuture cp =
              CompositeFuture.all(
                  createUserStatusesFuture, createFriendListsFuture, createChatListsFuture);
          cp.setHandler(
              ar -> {
                if (ar.succeeded()) {
                  List<UserStatus> userStatuses = cp.resultAt(0);
                  jsonObject.put("userStatuses", userStatuses);
                  List<Future> insertUserStatusFutures = new ArrayList<>();
                  for (UserStatus userStatus : userStatuses) {
                    insertUserStatusFutures.add(dataRepository.insertUserStatus(userStatus));
                  }
                  insertAllFutures.addAll(insertUserStatusFutures);

                  List<FriendList> friendLists = cp.resultAt(1);
                  jsonObject.put("friendLists", friendLists);
                  List<Future> insertFriendListFutures = new ArrayList<>();
                  for (FriendList friendList : friendLists) {
                    insertFriendListFutures.add(dataRepository.insertFriendList(friendList));
                  }
                  insertAllFutures.addAll(insertFriendListFutures);

                  List<ChatList> chatLists = cp.resultAt(2);
                  jsonObject.put("chatLists", chatLists);
                  List<Future> insertChatListFutures = new ArrayList<>();
                  for (ChatList chatList : chatLists) {
                    insertChatListFutures.add(dataRepository.insertChatList(chatList));
                  }
                  insertAllFutures.addAll(insertChatListFutures);

                  Future<List<ChatMessage>> createChatMessagesFuture =
                      createChatMessages(chatLists);
                  createChatMessagesFuture.compose(
                      chatMessages -> {
                        List<Future> insertChatMessageFutures = new ArrayList<>();
                        for (ChatMessage chatMessage : chatMessages) {
                          insertChatMessageFutures.add(
                              insertChatMessagesAndUpdateChatListAndUpdateUnseenCount(chatMessage));
                        }
                        insertAllFutures.addAll(insertChatMessageFutures);

                        CompositeFuture cp2 = CompositeFuture.all(insertAllFutures);
                        cp.setHandler(
                            ar2 -> {
                              if (ar2.succeeded()) {
                                future.complete(jsonObject);
                              } else {
                                future.fail(ar2.cause());
                              }
                            });
                      },
                      Future.future()
                          .setHandler(
                              handler -> {
                                future.fail(handler.cause());
                              }));

                } else {
                  future.fail(ar.cause());
                }
              });
        },
        Future.future()
            .setHandler(
                handler -> {
                  future.fail(handler.cause());
                }));

    return future;
  }

  private Future<List<User>> createUsers() {
    Future<List<User>> future = Future.future();
    List<User> users = new ArrayList<>();

    List<User> defaultUsers =
        PropertiesUtils.getInstance().getUsers("test.users").stream()
            .map(
                e ->
                    User.builder()
                        .userId(GenerationUtils.generateId())
                        .userName(e.getUserName())
                        .fullName(e.getFullName())
                        .password(e.getPassword())
                        .build())
            .collect(Collectors.toList());

    users.addAll(defaultUsers);

    List<User> randomUsers =
        new NameGenerator()
                .generateNames(
                    PropertiesUtils.getInstance().getIntValue("users.random.number"), Gender.FEMALE)
                .stream()
                .map(
                    e ->
                        User.builder()
                            .userId(GenerationUtils.generateId())
                            .userName(e.toString().trim().toLowerCase().replace(" ", "_"))
                            .fullName(e.toString().trim())
                            .password("123")
                            .build())
                .collect(Collectors.toList());

    users.addAll(randomUsers);

    future.complete(users);
    return future;
  }

  private Future<List<UserStatus>> createUserStatuses(List<User> users) {
    Future<List<UserStatus>> future = Future.future();
    List<UserStatus> userStatuses = new ArrayList<>();

    for (User user : users) {
      UserStatus userStatus = new UserStatus();
      userStatus.setUserId(user.getUserId());
      userStatus.setStatus(getRandomSentence());

      userStatuses.add(userStatus);
    }

    future.complete(userStatuses);
    return future;
  }

  private Future<List<FriendList>> createFriendLists(List<User> users) {
    Future<List<FriendList>> future = Future.future();
    List<FriendList> friendLists = new ArrayList<>();

    for (int i = 1; i < users.size(); i++) {
      FriendList userAFriendList = new FriendList();
      userAFriendList.setCurrentUserHashes(
          new UserHash(users.get(0).getUserId(), users.get(0).getFullName()));
      userAFriendList.setFriendUserHashes(
          new UserHash(users.get(i).getUserId(), users.get(i).getFullName()));

      friendLists.add(userAFriendList);
    }

    future.complete(friendLists);
    return future;
  }

  private Future<List<ChatList>> createChatLists(List<User> users) {
    Future<List<ChatList>> future = Future.future();
    List<ChatList> chatLists = new ArrayList<>();

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -1); // to get previous year add -1
    Date previousYear = cal.getTime();

    for (int i = 1; i < users.size(); i++) {
      ChatList userAChatList = new ChatList();
      List<UserHash> userAUserHashes = new ArrayList<>();
      userAUserHashes.add(new UserHash(users.get(0).getUserId(), users.get(0).getFullName()));
      userAUserHashes.add(new UserHash(users.get(i).getUserId(), users.get(i).getFullName()));
      userAChatList.setUserHashes(userAUserHashes);
      if(i == 1){
        userAChatList.setSessionId(PropertiesUtils.getInstance().getValue("test.session.id"));
      }else{
        userAChatList.setSessionId(GenerationUtils.generateId());
      }
      userAChatList.setUpdatedDate(previousYear);

      chatLists.add(userAChatList);
    }

    future.complete(chatLists);
    return future;
  }

  private Future<List<ChatMessage>> createChatMessages(List<ChatList> chatLists) {
    Future<List<ChatMessage>> future = Future.future();
    List<ChatMessage> chatMessages = new ArrayList<>();

    for (int i = 0; i < chatLists.size(); i++) {
      ChatList chatList = chatLists.get(i);
      chatMessages.addAll(
          createChatMessages(
              chatList.getSessionId(),
              chatList.getUserHashes(),
              chatList.getUpdatedDate().getTime()));
    }

    future.complete(chatMessages);
    return future;
  }

  private List<ChatMessage> createChatMessages(
      String sessionId, List<UserHash> userHashes, long createdDate) {
    List<ChatMessage> chatMessages = new ArrayList<>();

    int numMessage =
        RandomUtils.nextInt(
            1, PropertiesUtils.getInstance().getIntValue("messages.random.max.number"));
    for (int i = 0; i < numMessage; i++) {
      int randomUserId = RandomUtils.nextInt(0, userHashes.size());
      ChatMessage chatMessage = new ChatMessage();
      chatMessage.setUserHash(userHashes.get(randomUserId));
      chatMessage.setSessionId(sessionId);
      chatMessage.setCreatedDate(new Date(createdDate + i * 1000 * 60 * 5));
      chatMessage.setMessage(getRandomSentence());

      chatMessages.add(chatMessage);
    }

    return chatMessages;
  }

  private String getRandomSentence(){
    int ran = RandomUtils.nextInt(1, 4);
    switch (ran) {
      case 1:
        return RandomSentences.generateRandomSentence(RandomSentences.Length.LONG);
      case 2:
        return RandomSentences.generateRandomSentence(RandomSentences.Length.MEDIUM);
      case 3:
        return RandomSentences.generateRandomSentence(RandomSentences.Length.SHORT);
    }
    return "";
  }
}
