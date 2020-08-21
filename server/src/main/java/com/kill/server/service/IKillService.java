package com.kill.server.service;

import com.kill.model.dto.KillDto;

import java.util.List;
import java.util.Map;

public interface IKillService {
    Boolean KillItem(Integer killId,Integer userId) throws Exception;

    public Map<String,Object> checkUserKillResult(Integer killId, Integer userId) throws Exception;


}
