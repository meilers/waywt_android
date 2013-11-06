package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.RemoteObject;

public abstract class RemotePreProcessor<T extends RemoteObject> {

    public abstract void preProcessRemoteRecords(List<T> records);

}
