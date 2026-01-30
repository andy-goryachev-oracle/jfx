/*
 * Copyright (c) 2011, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.scene;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import com.sun.javafx.event.EventHandlerManager;

@Deprecated // FIX remove
public final class EventHandlerProperties {
    private final EventHandlerManager eventDispatcher;
    private final Object bean;

    public EventHandlerProperties(
        final EventHandlerManager eventDispatcher,
        final Object bean) {
        this.eventDispatcher = eventDispatcher;
        this.bean = bean;
    }

    private EventHandlerProperty<ContextMenuEvent> onMenuContextRequested;

    public final EventHandler<? super ContextMenuEvent> onContextMenuRequested() {
        return (onMenuContextRequested == null) ? null : onMenuContextRequested.get();
    }

    public ObjectProperty<EventHandler<? super ContextMenuEvent>> onContextMenuRequestedProperty() {
        if (onMenuContextRequested == null) {
            onMenuContextRequested = new EventHandlerProperty<>(
                bean,
                "onMenuContextRequested",
                ContextMenuEvent.CONTEXT_MENU_REQUESTED);
        }
        return onMenuContextRequested;
    }

    private EventHandlerProperty<MouseEvent> onMouseClicked;

    public final EventHandler<? super MouseEvent> getOnMouseClicked() {
        return (onMouseClicked == null) ? null : onMouseClicked.get();
    }

    public ObjectProperty<EventHandler<? super MouseEvent>> onMouseClickedProperty() {
        if (onMouseClicked == null) {
            onMouseClicked = new EventHandlerProperty<>(
                bean,
                "onMouseClicked",
                MouseEvent.MOUSE_CLICKED);
        }
        return onMouseClicked;
    }

    private EventHandlerProperty<MouseEvent> onMouseDragged;

    public final EventHandler<? super MouseEvent> getOnMouseDragged() {
        return (onMouseDragged == null) ? null : onMouseDragged.get();
    }

    public ObjectProperty<EventHandler<? super MouseEvent>> onMouseDraggedProperty() {
        if (onMouseDragged == null) {
            onMouseDragged = new EventHandlerProperty<>(
                bean,
                "onMouseDragged",
                MouseEvent.MOUSE_DRAGGED);
        }
        return onMouseDragged;
    }

    private EventHandlerProperty<MouseEvent> onMouseEntered;

    public final EventHandler<? super MouseEvent> getOnMouseEntered() {
        return (onMouseEntered == null) ? null : onMouseEntered.get();
    }

    public ObjectProperty<EventHandler<? super MouseEvent>> onMouseEnteredProperty() {
        if (onMouseEntered == null) {
            onMouseEntered = new EventHandlerProperty<>(
                bean,
                "onMouseEntered",
                MouseEvent.MOUSE_ENTERED);
        }
        return onMouseEntered;
    }

    private EventHandlerProperty<MouseEvent> onMouseExited;

    public final EventHandler<? super MouseEvent> getOnMouseExited() {
        return (onMouseExited == null) ? null : onMouseExited.get();
    }

    public ObjectProperty<EventHandler<? super MouseEvent>> onMouseExitedProperty() {
        if (onMouseExited == null) {
            onMouseExited = new EventHandlerProperty<>(
                bean,
                "onMouseExited",
                MouseEvent.MOUSE_EXITED);
        }
        return onMouseExited;
    }

    private EventHandlerProperty<MouseEvent> onMouseMoved;

    public final EventHandler<? super MouseEvent> getOnMouseMoved() {
        return (onMouseMoved == null) ? null : onMouseMoved.get();
    }

    public ObjectProperty<EventHandler<? super MouseEvent>> onMouseMovedProperty() {
        if (onMouseMoved == null) {
            onMouseMoved = new EventHandlerProperty<>(
                bean,
                "onMouseMoved",
                MouseEvent.MOUSE_MOVED);
        }
        return onMouseMoved;
    }

    private EventHandlerProperty<MouseEvent> onMouseReleased;

    public final EventHandler<? super MouseEvent> getOnMouseReleased() {
        return (onMouseReleased == null) ? null : onMouseReleased.get();
    }

    public ObjectProperty<EventHandler<? super MouseEvent>> onMouseReleasedProperty() {
        if (onMouseReleased == null) {
            onMouseReleased = new EventHandlerProperty<>(
                bean,
                "onMouseReleased",
                MouseEvent.MOUSE_RELEASED);
        }
        return onMouseReleased;
    }

    private EventHandlerProperty<ScrollEvent> onScroll;

    public final EventHandler<? super ScrollEvent> getOnScroll() {
        return (onScroll == null) ? null : onScroll.get();
    }

    public ObjectProperty<EventHandler<? super ScrollEvent>> onScrollProperty() {
        if (onScroll == null) {
            onScroll = new EventHandlerProperty<>(
                bean,
                "onScroll",
                ScrollEvent.SCROLL);
        }
        return onScroll;
    }

    private EventHandlerProperty<ScrollEvent> onScrollStarted;

    public final EventHandler<? super ScrollEvent> getOnScrollStarted() {
        return (onScrollStarted == null) ? null : onScrollStarted.get();
    }

    public ObjectProperty<EventHandler<? super ScrollEvent>> onScrollStartedProperty() {
        if (onScrollStarted == null) {
            onScrollStarted = new EventHandlerProperty<>(
                bean,
                "onScrollStarted",
                ScrollEvent.SCROLL_STARTED);
        }
        return onScrollStarted;
    }

    private EventHandlerProperty<ScrollEvent> onScrollFinished;

    public final EventHandler<? super ScrollEvent> getOnScrollFinished() {
        return (onScrollFinished == null) ? null : onScrollFinished.get();
    }

    public ObjectProperty<EventHandler<? super ScrollEvent>> onScrollFinishedProperty() {
        if (onScrollFinished == null) {
            onScrollFinished = new EventHandlerProperty<>(
                bean,
                "onScrollFinished",
                ScrollEvent.SCROLL_FINISHED);
        }
        return onScrollFinished;
    }

    private EventHandlerProperty<RotateEvent> onRotationStarted;

    public final EventHandler<? super RotateEvent> getOnRotationStarted() {
        return (onRotationStarted == null) ? null : onRotationStarted.get();
    }

    public ObjectProperty<EventHandler<? super RotateEvent>> onRotationStartedProperty() {
        if (onRotationStarted == null) {
            onRotationStarted = new EventHandlerProperty<>(
                bean,
                "onRotationStarted",
                RotateEvent.ROTATION_STARTED);
        }
        return onRotationStarted;
    }

    private EventHandlerProperty<RotateEvent> onRotate;

    public final EventHandler<? super RotateEvent> getOnRotate() {
        return (onRotate == null) ? null : onRotate.get();
    }

    public ObjectProperty<EventHandler<? super RotateEvent>> onRotateProperty() {
        if (onRotate == null) {
            onRotate = new EventHandlerProperty<>(
                bean,
                "onRotate",
                RotateEvent.ROTATE);
        }
        return onRotate;
    }

    private EventHandlerProperty<RotateEvent> onRotationFinished;

    public final EventHandler<? super RotateEvent> getOnRotationFinished() {
        return (onRotationFinished == null) ? null : onRotationFinished.get();
    }

    public ObjectProperty<EventHandler<? super RotateEvent>> onRotationFinishedProperty() {
        if (onRotationFinished == null) {
            onRotationFinished = new EventHandlerProperty<>(
                bean,
                "onRotationFinished",
                RotateEvent.ROTATION_FINISHED);
        }
        return onRotationFinished;
    }


    private EventHandlerProperty<SwipeEvent> onSwipeUp;

    public final EventHandler<? super SwipeEvent> getOnSwipeUp() {
        return (onSwipeUp == null) ? null : onSwipeUp.get();
    }

    public ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeUpProperty() {
        if (onSwipeUp == null) {
            onSwipeUp = new EventHandlerProperty<>(
                bean,
                "onSwipeUp",
                SwipeEvent.SWIPE_UP);
        }
        return onSwipeUp;
    }

    private EventHandlerProperty<SwipeEvent> onSwipeDown;

    public final EventHandler<? super SwipeEvent> getOnSwipeDown() {
        return (onSwipeDown == null) ? null : onSwipeDown.get();
    }

    public ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeDownProperty() {
        if (onSwipeDown == null) {
            onSwipeDown = new EventHandlerProperty<>(
                bean,
                "onSwipeDown",
                SwipeEvent.SWIPE_DOWN);
        }
        return onSwipeDown;
    }

    private EventHandlerProperty<SwipeEvent> onSwipeLeft;

    public final EventHandler<? super SwipeEvent> getOnSwipeLeft() {
        return (onSwipeLeft == null) ? null : onSwipeLeft.get();
    }

    public ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeLeftProperty() {
        if (onSwipeLeft == null) {
            onSwipeLeft = new EventHandlerProperty<>(
                bean,
                "onSwipeLeft",
                SwipeEvent.SWIPE_LEFT);
        }
        return onSwipeLeft;
    }

    private EventHandlerProperty<SwipeEvent> onSwipeRight;

    public final EventHandler<? super SwipeEvent> getOnSwipeRight() {
        return (onSwipeRight == null) ? null : onSwipeRight.get();
    }

    public ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeRightProperty() {
        if (onSwipeRight == null) {
            onSwipeRight = new EventHandlerProperty<>(
                bean,
                "onSwipeRight",
                SwipeEvent.SWIPE_RIGHT);
        }
        return onSwipeRight;
    }

    private EventHandlerProperty<MouseDragEvent> onMouseDragOver;

    public final EventHandler<? super MouseDragEvent> getOnMouseDragOver() {
        return (onMouseDragOver == null) ? null : onMouseDragOver.get();
    }

    public ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragOverProperty() {
        if (onMouseDragOver == null) {
            onMouseDragOver = new EventHandlerProperty<>(
                bean,
                "onMouseDragOver",
                MouseDragEvent.MOUSE_DRAG_OVER);
        }
        return onMouseDragOver;
    }

    private EventHandlerProperty<MouseDragEvent> onMouseDragReleased;

    public final EventHandler<? super MouseDragEvent> getOnMouseDragReleased() {
        return (onMouseDragReleased == null) ? null : onMouseDragReleased.get();
    }

    public ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragReleasedProperty() {
        if (onMouseDragReleased == null) {
            onMouseDragReleased = new EventHandlerProperty<>(
                bean,
                "onMouseDragReleased",
                MouseDragEvent.MOUSE_DRAG_RELEASED);
        }
        return onMouseDragReleased;
    }

    private EventHandlerProperty<MouseDragEvent> onMouseDragEntered;

    public final EventHandler<? super MouseDragEvent> getOnMouseDragEntered() {
        return (onMouseDragEntered == null) ? null : onMouseDragEntered.get();
    }

    public ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragEnteredProperty() {
        if (onMouseDragEntered == null) {
            onMouseDragEntered = new EventHandlerProperty<>(
                bean,
                "onMouseDragEntered",
                MouseDragEvent.MOUSE_DRAG_ENTERED);
        }
        return onMouseDragEntered;
    }

    private EventHandlerProperty<MouseDragEvent> onMouseDragExited;

    public final EventHandler<? super MouseDragEvent> getOnMouseDragExited() {
        return (onMouseDragExited == null) ? null : onMouseDragExited.get();
    }

    public ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragExitedProperty() {
        if (onMouseDragExited == null) {
            onMouseDragExited = new EventHandlerProperty<>(
                bean,
                "onMouseDragExited",
                MouseDragEvent.MOUSE_DRAG_EXITED);
        }
        return onMouseDragExited;
    }

    private EventHandlerProperty<MouseDragEvent> onMouseDragDone;

    public final EventHandler<? super MouseDragEvent> getOnMouseDragDone() {
        return (onMouseDragDone == null) ? null : onMouseDragDone.get();
    }

    public ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragDoneProperty() {
        if (onMouseDragDone == null) {
            onMouseDragDone = new EventHandlerProperty<>(bean, "onMouseDragDone", MouseDragEvent.MOUSE_DRAG_DONE);
        }
        return onMouseDragDone;
    }











    // Do we want DRAG_TRANSFER_MODE_CHANGED event?
    //    private EventHandlerProperty<DragEvent> onDragTransferModeChanged;
    //
    //    public final EventHandler<? super DragEvent> getOnDragTransferModeChanged() {
    //        return (onDragTransferModeChanged == null) ?
    //            null : onDragTransferModeChanged.get();
    //    }
    //
    //    public ObjectProperty<EventHandler<? super DragEvent>>
    //            onDragTransferModeChanged() {
    //        if (onDragTransferModeChanged == null) {
    //            onDragTransferModeChanged = new EventHandlerProperty<DragEvent>(
    //                                    DragEvent.DRAG_TRANSFER_MODE_CHANGED);
    //        }
    //        return onDragTransferModeChanged;
    //    }



    private final class EventHandlerProperty<T extends Event>
        extends SimpleObjectProperty<EventHandler<? super T>> {
        private final EventType<T> eventType;

        public EventHandlerProperty(final Object bean,
            final String name,
            final EventType<T> eventType) {
            super(bean, name);
            this.eventType = eventType;
        }

        @Override
        protected void invalidated() {
            eventDispatcher.setEventHandler(eventType, get());
        }
    }
}
