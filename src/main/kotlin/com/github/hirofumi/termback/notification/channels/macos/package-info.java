/**
 * Uses {@code NSUserNotification} (deprecated in macOS 10.14) because {@code UNUserNotificationCenter}
 * requires Objective-C blocks, which IntelliJ's {@code Foundation} class (built on JNA) cannot handle.
 *
 * <p>If using {@code UNUserNotificationCenter}, Termback would need these delegate methods,
 * which require block parameters:
 * <ul>
 *   <li>{@code userNotificationCenter:willPresentNotification:withCompletionHandler:}
 *       (to show notifications even when IDE is in foreground)
 *   <li>{@code userNotificationCenter:didReceiveNotificationResponse:withCompletionHandler:}
 *       (to handle clicks and navigate to the terminal tab)
 * </ul>
 *
 * @see <a href="https://github.com/java-native-access/jna/issues/571">JNA issue #571</a>
 */
package com.github.hirofumi.termback.notification.channels.macos;
