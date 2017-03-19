package com.imran.wali.sharetango.DataRepository;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by wali on 24/01/17.
 */

public class WifiClientRepository {

    private static WifiClientRepository INSTANCE;

    public static WifiClientRepository getInstance() {
        if (INSTANCE == null) {
            return new WifiClientRepository();
        }
        return INSTANCE;
    }

    private ArrayList<WifiP2pDevice> activeClientRepository;

    private WifiClientRepository() {
        activeClientRepository = new ArrayList<>();
    }

    public ArrayList<WifiP2pDevice> getListOfDevicesToRemove(Collection<WifiP2pDevice> newList){
        ArrayList<WifiP2pDevice> listOfDevicesToRemove = new ArrayList<>();
        if (newList == null)return listOfDevicesToRemove;

        for (WifiP2pDevice device : newList) {
            if (!activeClientRepository.contains(device)) {
                listOfDevicesToRemove.add(device);
            }
        }
        return listOfDevicesToRemove;
    }

    public ArrayList<WifiP2pDevice> getListOfDevicesToAdd(Collection<WifiP2pDevice> newList){
        ArrayList<WifiP2pDevice> listOfDevicesToAdd = new ArrayList<>();
        if (newList == null) return listOfDevicesToAdd;
        for (WifiP2pDevice device : activeClientRepository) {
            if (!newList.contains(device)) {
                listOfDevicesToAdd.add(device);
            }
        }
        return listOfDevicesToAdd;
    }

    public synchronized void setActiveList(Collection<WifiP2pDevice> newList){
        activeClientRepository.clear();
        activeClientRepository.addAll(newList);
    }

    public synchronized ArrayList<WifiP2pDevice> getActiveList(){
        return activeClientRepository;
    }



}
