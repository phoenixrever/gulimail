package com.phoenixhell.gulimall.seckill.schduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

//@Slf4j
//@Component
public class Hello {

    /**
     *   秒 分 时 日 月 周
     *
     *   日和周的位置随便谁是? 都行
     */
    //希望异步执行的方法 定时任务不阻塞
    //@Async
    //@Scheduled(cron = "* * * * * 7")
    public void hello(){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now().withHour(23).withMinute(53).withSecond(59);
        LocalDateTime localDateTime1 = localDateTime.plusDays(1);
        LocalDateTime localDateTime2 = localDateTime.plusDays(2);
        System.out.println(localDateTime1);
        System.out.println(localDateTime2);

        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
