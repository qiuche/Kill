package com.kill.server.controller;

import com.kill.api.enums.StatusCode;
import com.kill.api.response.BaseResponse;
import com.kill.model.dto.KillDto;
import com.kill.model.dto.KillSuccessUserInfo;
import com.kill.model.mapper.ItemKillSuccessMapper;
import com.kill.server.service.IKillService;
import com.kill.server.service.RabbitSenderService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.testng.collections.Maps;

import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.Map;

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
    public BaseResponse execute(@RequestBody @Validated KillDto killDto, BindingResult result, HttpSession session){
        if(result.hasErrors()||killDto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        Object uid=session.getAttribute("uid");
        if(uid==null){
            return new BaseResponse(StatusCode.UserNotLogin);
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

    @RequestMapping(value = prefix+"/record/detail/{orderNo}",method = RequestMethod.GET)
    public String killRecordDetail(@PathVariable String orderNo, ModelMap modelMap){
        if(StringUtils.isBlank(orderNo)){
            return "error";
        }
        KillSuccessUserInfo info=itemKillSuccessMapper.selectByCode(orderNo);
        if(info==null){
            return "error";
        }
        modelMap.put("info",info);
        return "killRecord";
    }

    //抢购成功跳转页面
    @RequestMapping(value = prefix+"/execute/success",method = RequestMethod.GET)
    public String executeSuccess(){
        return "executeSuccess";
    }

    //抢购失败跳转页面
    @RequestMapping(value = prefix+"/execute/fail",method = RequestMethod.GET)
    public String executeFail(){
        return "executeFail";
    }

    @Autowired
    private RabbitSenderService rabbitSenderService;

    @RequestMapping(value = prefix+"/execute/mq",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executemq(@RequestBody @Validated KillDto dto, BindingResult result,HttpSession session) {
        if (result.hasErrors()||dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        Object uid=session.getAttribute("uid");
        if(uid==null){
            return new BaseResponse(StatusCode.UserNotLogin);
        }
        Integer userId=(Integer)uid;

        BaseResponse response=new BaseResponse(StatusCode.Success);
        Map<String,Object> dataMap= Maps.newHashMap();
        try {
            dataMap.put("killId",dto.getKillId());
            dataMap.put("userId",userId);
            response.setData(dataMap);
            dto.setUserId(userId);
            rabbitSenderService.sendKillExecuteMqMsg(dto);
        }catch (Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

    @RequestMapping(value = prefix+"/execute/mq/to/result",method = RequestMethod.GET)
    public String executeToResult(@RequestParam Integer killId,HttpSession session,ModelMap modelMap){
        Object uId=session.getAttribute("uid");
        if (uId!=null){
            Integer userId= (Integer)uId ;
            modelMap.put("killId",killId);
            modelMap.put("userId",userId);
        }
        return "executeMqResult";
    }

    @RequestMapping(value = prefix+"/execute/mq/result",method = RequestMethod.GET)
    @ResponseBody
    public BaseResponse executeResult(@RequestParam Integer killId,@RequestParam Integer userId){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            Map<String,Object> resMap=killService.checkUserKillResult(killId,userId);
            response.setData(resMap);
        }catch (Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

}
