# Termback Architecture

## Components

```
                    +------------------------------+   bindSelected()  +------------------------------+
                    | TermbackSettingsConfigurable |------------------>| TermbackSettings             |
                    +------------------------------+                   | (Application-level Service)  |
                                                                       +------------------------------+
                                                                                                ^
                                                                skipPopupWhenSingleNotification |
                                                                                                |
                 +------------------------------------------------------+                       |
                 |                                                      |                       |
                 |             +---------------------------------+  +----------------------------------+
                 |   +---------| TermbackLocalTerminalCustomizer |  | TermbackSelectNotificationAction |
                 |   |         | (Terminal Extension)            |  | (Alt + Shift + T)                |
                 |   |         +---------------------------------+  +----------------------------------+
                 |   |                |  |                               |                      |
                 |   | register()     |  |                               |                      |
getAllSessions() |   | unregister()   |  |                               |                      |
                 v   v                |  |                               |  +-------------------+---+
  +-----------------------------+     |  |   getUnexpiredNotifications() |  | navigateToTab()       |
  | TermbackSessionRegistry     |     |  |                               v  v                       |
  | (Application-level Service) |     |  |           new()  +-----------------+                     |
  +-----------------------------+     |  +----------------->| TermbackSession |------------------+  |
    ^                                 |                     +-----------------+                  |  |
    | findById(), findByContent()     |                       ^                                  |  |
    |                                 |                       | addNotification()                |  |
    |                                 |                       | takeSuppressedNotifications()    |  |
    |    expireSessionNotifications() |                       | takeAllNotifications()           |  |
    |                                 v                       |                                  |  |
    |                              +-----------------------------+                               |  |
    +------------------------------| TermbackNotifier            |---------------+---------------+  |
                                   | (Application-level Service) |               |                  |
                                   +-----------------------------+               | expire()         |
                                     ^            ^       |                      v                  |
     expireSuppressedNotifications() |   notify() |       |   +----------------------------+        |
  +-----+----------------------------+            |       |   | TermbackNotificationHandle |        |
  |     |                                         |       |   +----------------------------+        |
  |     |                                         |       |               ^      |                  |
  |  +---------------------------------------+    |       |   postToIde() |      | expire()         |
  |  | TermbackApplicationActivationListener |    |       |               |      v                  |
  |  | (Application-level Listener)          |    |       |               |   +--------------+      |
  |  +---------------------------------------+    |       |               |   | Notification |------+
  |                                               |       |               |   +--------------+
+-------------------------------------------+     |       |               |
| TermbackTerminalToolWindowManagerListener |     |       |               |
| (Project-level Listener)                  |     |       |               |
+-------------------------------------------+     |       | post()        |
                                                  |       v               |
                              +---------------------+   +-----------------------------+
                              | TermbackRestService |   | TermbackNotificationChannel | 
                              | (HTTP endpoint)     |   +-----------------------------+
                              +---------------------+
```
