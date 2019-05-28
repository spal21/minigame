package com.king.services.scorestore.cache.impl;

import com.king.services.scorestore.cache.ObjectCache;
import com.king.services.scorestore.model.UserScore;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserScoreCache implements ObjectCache<Integer, List<UserScore>> {

    private ConcurrentMap<Integer, List<UserScore>> map;

    public UserScoreCache() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public List<UserScore> get(Integer key) {
        return map.get(key);
    }

    @Override
    public List<UserScore> put(Integer key, List<UserScore> value) {
        return map.put(key, value);
    }
}
