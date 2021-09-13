package com.phoenixhell.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.phoenixhell.gulimall.thirdparty.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class TestOssUpload {
    @Autowired
    OSSClient ossClient;
    @Test
    public void  test() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("L:\\videos\\movies\\ELF SLAVES FINAL COLLECTION\\ELF SLAVES FINAL COLLECTION\\Elf Slave2\\ES2-2nd option\\elfslave2-018-S.jpg");
        ossClient.putObject("phoenixhell-gulimall", "elf.jpg", fileInputStream);
        ossClient.shutdown();
        System.out.println("上传完成");
    }

    @Test
    public  void sendSms(){
        String host = "https://dfsns.market.alicloudapi.com";
        String path = "/data/send_sms";
        String method = "POST";
        String appcode = "bde5d799c3694f678ac97b0cf1dc44c4";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:1313,expire_at:5");
        bodys.put("phone_number", "17805066859");
        bodys.put("template_id", "TPL_0001");


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            //spring  restTemplate 也可以发送Http请求
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
