package com.djt.jukeanator_engine.ui.components;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.Timer;

public class IdleMonitor {

  //private static final long IDLE_TIMEOUT_MS = 120_000;
  private static final long IDLE_TIMEOUT_MS = 30_000;

  private long lastActivity = System.currentTimeMillis();

  private final Timer timer;

  public IdleMonitor(Runnable onIdle, Runnable onActive) {

    Toolkit.getDefaultToolkit().addAWTEventListener(e -> {

      if (e instanceof MouseEvent || e instanceof KeyEvent || e instanceof MouseWheelEvent) {

        lastActivity = System.currentTimeMillis();

        onActive.run();
      }

    }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
        | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

    timer = new Timer(1000, e -> {

      if (System.currentTimeMillis() - lastActivity >= IDLE_TIMEOUT_MS) {

        onIdle.run();
      }
    });

    timer.start();
  }
}
