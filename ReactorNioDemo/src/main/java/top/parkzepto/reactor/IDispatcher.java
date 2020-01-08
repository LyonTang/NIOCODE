package top.parkzepto.reactor;

import java.nio.channels.SelectionKey;

public interface IDispatcher {
    void dispatch(SelectionKey selectionKey);
}
