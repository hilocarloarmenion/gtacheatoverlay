# GTA Cheat Overlay multitouch (v0.3.0)

GTA joystick finger and cheat overlay finger can be routed separately where the game window supports split touch.

## Fix
- Both the **54dp bubble window** and the cheat panel window use `FLAG_SPLIT_TOUCH` with `FLAG_NOT_FOCUSABLE`, `FLAG_NOT_TOUCH_MODAL`, and `FLAG_LAYOUT_IN_SCREEN`.
- Only the bubble's actual 54dp bounds are touchable when the panel is closed. No invisible full-screen gesture interceptor.
- Pointer ID is tracked during drag so unrelated fingers cannot inadvertently move the bubble.
- Dragging displays a small red × target at the screen bottom. Drop the bubble over it to stop the overlay foreground service.
- The X is a short-lived, non-touchable, 60%-opaque overlay and does not intercept gameplay inputs.
- Dropping elsewhere snaps the bubble to the nearest edge and remembers its vertical position.
- Orientation changes clamp coordinates to the new screen.
- The built-in keyboard has a compact action row and disables fullscreen IME landscape mode.

## Device test
1. Start GTA SA Android 2.10 and the floating bubble.
2. **Hold the on-screen joystick with finger 1**, and, without releasing it, **tap the bubble using finger 2**.
3. Confirm both the gameplay movement and Tier 1 menu interaction continue.
4. Navigate into Tier 2/3 while still holding the joystick.
5. Drag the bubble to the red × (bottom center) to close it.
6. Reopen the overlay in the app and confirm it remembers vertical position.

**Android limitation:** `FLAG_SPLIT_TOUCH` allows split pointer routing **only when the underlying game window also cooperates**. No third-party non-root overlay can guarantee simultaneous input if GTA's window/game engine captures all pointers. This patch does not inject/forward arbitrary game touches or bypass Android security.
