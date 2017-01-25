package com.imran.wali.sharetango.Wifi;

import java.util.HashMap;
import java.util.Map;

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

    private Map activeClientRepository;

    private WifiClientRepository() {
        activeClientRepository = new HashMap();
    }




}
