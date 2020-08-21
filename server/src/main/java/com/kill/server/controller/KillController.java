package com.kill.server.controller;

import com.kill.api.enums.StatusCode;
import com.kill.api.response.BaseResponse;
import com.kill.model.dto.KillDto;
import com.kill.model.dto.KillSuccessUserInfo;
import com.kill.model.entity.ItemKill;
import com.kill.model.mapper.ItemKillMapper;
import com.kill.model.mapper.ItemKillSuccessMapper;
import com.kill.server.Filter.BloomFilterHelper;
import com.kill.server.Filter.RedisBloomFilter;
import com.kill.server.service.IKillService;
import com.kill.server.service.RabbitSenderService;
import com.kill.server.utils.EhCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.testng.collections.Maps;
import com.kill.model.dto.KillDto;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class KillController {

    private static final Logger log = LoggerFactory.getLogger(KillController.class);

    private static final String prefix = "kill";

    private static ConcurrentHashMap<Integer,Boolean> sellOut=new ConcurrentHashMap<>();

    @Autowired
    private IKillService killService;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private Environment env;

    @Autowired
    RedisBloomFilter redisBloomFilter;

    @Autowired
    ItemKillMapper itemKillMapper;

    @Autowired
    private BloomFilterHelper bloomFilterHelper;

    @Autowired
    private EhCacheUtils ehCacheUtils;

    private static final String cacheName="ItemCache";


//    @PostConstruct
//    private void init(){
//        List<KillDto> kidUids=itemKillSuccessMapper.selectKidUid();
//        for(KillDto killDto:kidUids){
//            stringRedisTemplate.opsForValue().set(env.getProperty("redis.item.kill.Killedkey")+killDto.getKillId()+"-"+killDto.getUserId(),true+"");
//        }
//    }

    @RequestMapping(value = prefix+"/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto killDto, BindingResult result){
        if(result.hasErrors()||killDto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            Boolean aBoolean = killService.KillItem(killDto.getKillId(), killDto.getUserId());
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

    @RequestMapping(value = prefix+"/execute/mq/Jm",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executemqJm(@RequestBody @Validated KillDto dto, BindingResult result) {
        if (result.hasErrors()||dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        //JVM缓存
        if(sellOut.get(dto.getKillId())!=null){
            log.error("该商品已售空");
            return new BaseResponse(StatusCode.Fail.getCode(),"该商品已售磬");
        }

        System.out.println("本地缓存+++++++++++"+ehCacheUtils.get("ItemCache","LocalCacheKey-"+dto.getKillId()));

        System.out.println("redis缓存+++++++++++"+stringRedisTemplate.hasKey(env.getProperty("redis.item.kill.Itemkey") + dto.getKillId()));

        //查redis缓存
        if(!(stringRedisTemplate.hasKey(env.getProperty("redis.item.kill.Itemkey") + dto.getKillId())))
        {
            //查本地缓存
            if(ehCacheUtils.get("ItemCache","LocalCacheKey-"+dto.getKillId())==null){
                boolean b = redisBloomFilter.includeByBloomFilter(bloomFilterHelper, "bloom", dto.getKillId()+"");
                //布隆过滤器
                if(!b){
                    log.error("秒杀Id不存在");
                    return new BaseResponse(StatusCode.Fail.getCode(),"秒杀Id不存在");
                }
                //存在就查库存
                ItemKill itemKill=itemKillMapper.selectById(dto.getKillId());
                //存在加入缓存中
                if(itemKill!=null&&itemKill.getCanKill()==1){
                    ehCacheUtils.put(cacheName,"LocalCacheKey-"+itemKill.getId(),itemKill.getTotal());
                    stringRedisTemplate.opsForValue().set(env.getProperty("redis.item.kill.Itemkey")+itemKill.getId(),itemKill.getTotal()+"");
                }
            }
        }
        //如果存在redis缓存中进行原子减操作
        else {
            Long total=stringRedisTemplate.opsForValue().decrement(env.getProperty("redis.item.kill.Itemkey") + dto.getKillId());
            log.info("商品剩余：{}",total);
            if(total<0){
                stringRedisTemplate.opsForValue().increment(env.getProperty("redis.item.kill.Itemkey") + dto.getKillId());
                sellOut.put(dto.getKillId(),true);
                return new BaseResponse(StatusCode.Fail.getCode(),"该商品已售空");
            }
        }

        BaseResponse response=new BaseResponse();
        Map<String,Object> dataMap= Maps.newHashMap();
        try {
            dataMap.put("killId",dto.getKillId());
            dataMap.put("userId",dto.getUserId());
            response.setData(dataMap);
            response.setCode(StatusCode.Success.getCode());
            rabbitSenderService.sendKillExecuteMqMsg(dto);
        }catch (Exception e){
            log.info("最外层异常捕捉，进行库存还原");
            //异常还原库存
            if(sellOut.get(dto.getKillId())!=null){
                sellOut.remove(dto.getKillId());
            }
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
