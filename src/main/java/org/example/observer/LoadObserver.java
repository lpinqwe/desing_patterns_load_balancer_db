package org.example.observer;

import org.example.interfaces.DbNode;

public interface LoadObserver {

    void onLoadChanged(DbNode node, int load);

}
