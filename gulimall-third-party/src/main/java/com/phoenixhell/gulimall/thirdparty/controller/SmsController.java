package com.phoenixhell.gulimall.thirdparty.controller;

import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.thirdparty.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    SmsService smsService;

    @GetMapping("/send")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsService.sendSms(phone, code);
        return R.ok();
    }
}
