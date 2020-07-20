package com.kill.server.controller;

import com.kill.api.enums.StatusCode;
import com.kill.api.response.BaseResponse;
import com.kill.model.dto.KillDto;
import com.kill.model.mapper.ItemKillSuccessMapper;
import com.kill.server.service.IKillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;

import java.awt.*;

@Controller
public class KillController {

    private static final Logger log = LoggerFactory.getLogger(KillController.class);

    private static final String prefix = "kill";

    @Autowired
    private IKillService killService;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @RequestMapping(value = prefix+"/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto killDto, BindingResult result){
        if(result.hasErrors()||killDto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        Integer userId=killDto.getUserId();
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            Boolean aBoolean = killService.KillItem(killDto.getKillId(), userId);
            if(!aBoolean){
                return new BaseResponse(StatusCode.Fail.getCode(),"商品抢购完毕");
            }

        } catch (Exception e) {
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }
}
