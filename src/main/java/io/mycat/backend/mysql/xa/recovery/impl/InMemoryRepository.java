package io.mycat.backend.mysql.xa.recovery.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import io.mycat.backend.mysql.xa.CoordinatorLogEntry;
import io.mycat.backend.mysql.xa.TxState;
import io.mycat.backend.mysql.xa.recovery.Repository;

/**
 * Created by zhangchao on 2016/10/18.
 */
public class InMemoryRepository implements Repository {

    private Map<String, CoordinatorLogEntry> storage = new ConcurrentHashMap<String, CoordinatorLogEntry>();
    private ReentrantLock lock = new ReentrantLock();

    public ReentrantLock getLock() {
		return lock;
	}

	private boolean closed = true;
    @Override
    public void init() {
        closed=false;
    }

    @Override
    public void put(String id, CoordinatorLogEntry coordinatorLogEntry) {
		lock.lock();
		try {
			storage.put(id, coordinatorLogEntry);
		} finally {
			lock.unlock();
		}
    }

    @Override
    public CoordinatorLogEntry get(String coordinatorId) {
        return storage.get(coordinatorId);
    }

    @Override
    public void close() {
		lock.lock();
		try {
			storage.clear();
		} finally {
			lock.unlock();
		}
        closed=true;
    }

    @Override
    public Collection<CoordinatorLogEntry> getAllCoordinatorLogEntries() {
        return storage.values();
    }

    @Override
    public boolean writeCheckpoint(
            Collection<CoordinatorLogEntry> checkpointContent) {
        throw new UnsupportedOperationException();
    }



    public boolean isClosed() {
        return closed;
    }

	@Override
	public void remove(String id) {
		lock.lock();
		try {
			if(storage.get(id).getTxState()==TxState.TX_COMMITED_STATE ||storage.get(id).getTxState()==TxState.TX_ROLLBACKED_STATE){
				storage.remove(id);
			}
		} finally {
			lock.unlock();
		}
	}
}
