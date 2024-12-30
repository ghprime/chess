package serverfacade;

public interface NotificationHandler {
  void notify(String message);
  void error(String error);
}
