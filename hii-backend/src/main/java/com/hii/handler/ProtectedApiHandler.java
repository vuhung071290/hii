package com.hii.handler;

import com.hii.manager.JwtManager;
import com.hii.model.*;
import com.hii.service.APIService;
import com.hii.util.ErrorCode;
import com.hii.util.HttpStatus;
import com.hii.util.JsonUtils;
import com.hii.util.LogUtils;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class ProtectedApiHandler extends BaseHandler {

  public static final String AUTHENTICATION_SCHEME = "Bearer";
  public JwtManager jwtManager;
  private APIService apiService;

  public void handle(RoutingContext rc) {
    HttpServerRequest request = rc.request();
    HttpServerResponse response = rc.response();
    String requestPath = request.path();
    String path = StringUtils.substringAfter(requestPath, "/api/protected");
    try {
      String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);
      if (StringUtils.isBlank(authorization)) {
        throw new HttpStatusException(
            HttpStatus.UNAUTHORIZED.code(), HttpStatus.UNAUTHORIZED.message());
      }
      authorization = authorization.replace(AUTHENTICATION_SCHEME, "").trim();

      JsonObject authObj = new JsonObject().put("jwt", authorization);
      jwtManager.authenticate(
          authObj,
          event -> {
            if (event.succeeded()) {
              String userId = event.result().principal().getString("userId");
              JsonObject jsonObject = null;
              if (rc.getBody() != null && rc.getBody().length() > 0)
                jsonObject = rc.getBodyAsJson();
              switch (path) {
                case "/ping":
                  ping(request, response);
                  break;
                case "/chatlist":
                  LogUtils.userLog("User  " + userId + " request chat list");
                  getChatList(request, response, jsonObject, userId);
                  break;
                case "/addressbook":
                  LogUtils.userLog("User  " + userId + " request address book");
                  getAddressBook(request, response, jsonObject, userId);
                  break;
                case "/usernameexisted":
                  LogUtils.userLog("User  " + userId + " check username " + jsonObject);
                  checkUsernameExisted(request, response, jsonObject, userId);
                  break;
                case "/sessionidbyuserid":
                  LogUtils.userLog("User  " + userId + " get session id " + jsonObject);
                  getSessionIdByUserId(request, response, jsonObject, userId);
                  break;
                case "/waitingchatheader":
                  LogUtils.userLog("User  " + userId + " get temporarily chat header " + jsonObject);
                  waitingChatHeader(request, response, jsonObject, userId);
                  break;
                case "/addfriend":
                  LogUtils.userLog("User  " + userId + " add new friend " + jsonObject);
                  addFriend(request, response, jsonObject, userId);
                  break;
                case "/status":
                  LogUtils.userLog("User  " + userId + " change status " + jsonObject);
                  changeStatus(request, response, jsonObject, userId);
                  break;
                case "/user":
                  LogUtils.userLog("User  " + userId + " get profile " + jsonObject);
                  getUserProfile(request, response, jsonObject, userId);
                  break;
              }
            } else {
              throw new HttpStatusException(
                  HttpStatus.UNAUTHORIZED.code(), HttpStatus.UNAUTHORIZED.message());
            }
          });
    } catch (HttpStatusException e) {
      JsonObject obj = new JsonObject();
      obj.put("code", ErrorCode.AUTHORIZED_FAILED.code());
      obj.put("message", e.getPayload());
      response
          .setStatusCode(e.getStatusCode())
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(JsonUtils.toErrorJSON(obj));
    }
  }

  public void getChatList(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {

    Future<ChatListResponse> getChatListFuture = apiService.getChatList(userId);

    getChatListFuture.compose(
        chatListResponse -> {
          response
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(chatListResponse));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

  public void getAddressBook(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {

    Future<AddressBookResponse> getAddressBookFuture = apiService.getAddressBook(userId);

    getAddressBookFuture.compose(
        addressBookResponse -> {
          response
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(addressBookResponse));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

  public void checkUsernameExisted(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {
    UsernameExistedRequest usernameExistedRequest =
        requestObject.mapTo(UsernameExistedRequest.class);

    Future<UsernameExistedResponse> checkUsernameExistedFuture =
        apiService.checkUsernameExisted(usernameExistedRequest, userId);

    checkUsernameExistedFuture.compose(
        usernameExistedResponse -> {
          response
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(usernameExistedResponse));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

  public void getSessionIdByUserId(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {
    GetSessionIdRequest getSessionIdRequest = requestObject.mapTo(GetSessionIdRequest.class);

    Future<GetSessionIdResponse> getSessionIdByUserIdFuture =
        apiService.getSessionIdByUserId(getSessionIdRequest, userId);

    getSessionIdByUserIdFuture.compose(
        getSessionIdResponse -> {
          response
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(getSessionIdResponse));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

  public void waitingChatHeader(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {
    WaitingChatHeaderRequest waitingChatHeaderRequest =
        requestObject.mapTo(WaitingChatHeaderRequest.class);

    Future<WaitingChatHeaderResponse> waitingChatHeaderFuture =
        apiService.waitingChatHeader(waitingChatHeaderRequest, userId);

    waitingChatHeaderFuture.compose(
        waitingChatHeaderResponse -> {
          response
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(waitingChatHeaderResponse));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

  public void addFriend(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {
    AddFriendRequest addFriendRequest = requestObject.mapTo(AddFriendRequest.class);

    Future<AddFriendResponse> addFriendFuture = apiService.addFriend(addFriendRequest, userId);

    addFriendFuture.compose(
        addFriendResponse -> {
          response
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(addFriendResponse));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

  public void changeStatus(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {
    ChangeStatusRequest changeStatusRequest = requestObject.mapTo(ChangeStatusRequest.class);

    Future<JsonObject> insertUserStatusFuture =
        apiService.changeStatus(changeStatusRequest, userId);

    insertUserStatusFuture.compose(
        jsonObject -> {
          response
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(jsonObject));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

  public void getUserProfile(
      HttpServerRequest request,
      HttpServerResponse response,
      JsonObject requestObject,
      String userId) {
    Future<UserProfileResponse> getUserProfileFuture = apiService.getUserProfile(userId);

    getUserProfileFuture.compose(
        userProfileResponse -> {
          response
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(userProfileResponse));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }
}
