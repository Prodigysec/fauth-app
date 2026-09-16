package io.fusionauth.api.service.messenger;

import io.fusionauth.api.domain.message.SendMessageResult;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;

public interface Messenger {
  SendMessageResult send(Message paramMessage, BaseMessengerConfiguration paramBaseMessengerConfiguration) throws MessengerException;
}
