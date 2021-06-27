package com.phoenixhell.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
}
