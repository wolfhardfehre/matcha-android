package com.nicefontaine.matcha.data;


import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;
import timber.log.Timber;


public final class DefaultEventBus {


    private static final Object LOCK = new Object();

    private static DefaultEventBus instance;

    private DefaultEventBus() {
    }

    /**
     * Gets the single instance of {@link DefaultEventBus}.
     *
     * @return single instance of {@link DefaultEventBus}
     */
    public static DefaultEventBus getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DefaultEventBus();
                }
            }
        }

        return instance;
    }

    /**
     * Registers given {@link Object} to {@link EventBus}.
     *
     * @param subscriber the subscriber {@link Object}
     */
    public void register(final Object subscriber) {
        try {
            if (!EventBus.getDefault().isRegistered(subscriber)) {
                EventBus.getDefault().register(subscriber);
                Timber.d("Register '%s'", subscriber.getClass().getSimpleName());
            }
        } catch (final EventBusException exception) {
            Timber.d(exception.getMessage());
        }
    }

    /**
     * Unregister given {@link Object} from {@link EventBus}.
     *
     * @param subscriber the subscriber {@link Object}
     */
    public void unregister(final Object subscriber) {
        if (EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().unregister(subscriber);
            Timber.d("Unregister %s", subscriber.getClass().getSimpleName());
        }
    }

    /**
     * Returns the concrete {@link EventBus} implementation
     *
     * @return the {@link EventBus}
     */
    public EventBus getEventBusImplementation() {
        return EventBus.getDefault();
    }

    /**
     * Returns a sticky post event and remove it from event bus
     *
     * @param eventType the event type class
     * @param <T>       the class type
     * @return the instance
     */
    public <T> T getStickyEventAndRemoveIt(final Class<T> eventType) {
        final T stickyEvent = getEventBusImplementation().getStickyEvent(eventType);
        if (stickyEvent != null) {
            getEventBusImplementation().removeStickyEvent(stickyEvent);
        }

        return stickyEvent;
    }

    /**
     * Posts given {@link Object} to subscribers on {@link EventBus}
     *
     * @param event the event to post
     */
    public void post(final Object event) {
        EventBus.getDefault().post(event);
    }

}
