package com.kill.server.service;

import java.util.Map;

public interface IKillService {
    Boolean KillItem(Integer killId,Integer userId) throws Exception;

    public Map<String,Object> checkUserKillResult(Integer killId, Integer userId) throws Exception;

}
