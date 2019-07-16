package au.com.venilia.xbee.listener;

import au.com.venilia.xbee.event.DataEvent;

public interface DataEventListener {

    public void dataReceived(final DataEvent dataEvent);
}
