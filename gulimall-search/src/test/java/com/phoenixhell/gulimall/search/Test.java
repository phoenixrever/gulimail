package com.phoenixhell.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.phoenixhell.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest()
class TestDemo {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Data
    class User {
        private String username;
        private Integer age;
        private String gender;
    }

    /**
     * 测试存储与更新(index 都可以)数据
     */
    @Test
    void testIndexApi() throws IOException {
        //保存方式一   参数为索引名字
        //文档https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-index.html
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUsername("phoenixhell");
        user.setAge(18);
        user.setGender("神");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        //IndexResponse[index=users,type=_doc,id=1,version=1,result=created,seqNo=0,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
        System.out.println(indexResponse);

    }

    /**
     * get 查询
     */
    @Test
    void testGetApi() throws IOException {
        GetRequest getRequest = new GetRequest("product", "45");
        GetResponse documentFields = restHighLevelClient.get(getRequest, ElasticSearchConfig.COMMON_OPTIONS);
        //{"_index":"users","_type":"_doc","_id":"1","_version":2,"_seq_no":1,"_primary_term":1,"found":true,"_source":{"age":18,"gender":"神","username":"phoenixhell"}}
        System.out.println(documentFields);
    }

    /**
     * 检索
     */
    @Test
    void searchApi() throws IOException {
        // 1 创建检索请求 参数为要检索的索引
        SearchRequest searchRequest = new SearchRequest("bank");

        //SearchSourceBuilder构造 检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        //name 构造的聚合名字  filed 如果 age 为text type 需要指定
        //keyword 不分词查询 keyword type则不需要
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        //子聚合
        AvgAggregationBuilder subBalanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        ageAgg.subAggregation(subBalanceAvg);
        searchSourceBuilder.aggregation(ageAgg);

        //平均年龄聚合
        AvgAggregationBuilder ageAvg = AggregationBuilders.avg("avgAgg").field("age");
        searchSourceBuilder.aggregation(ageAvg);

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);
        searchSourceBuilder.timeout(TimeValue.timeValueSeconds(1));
        searchRequest.source(searchSourceBuilder);

        //{"from":0,"size":5,"timeout":"1s","query":{"match":{"address":{"query":"mill","operator":"OR","prefix_length":0,"max_expansions":50,"fuzzy_transpositions":true,"lenient":false,"zero_terms_query":"NONE","auto_generate_synonyms_phrase_query":true,"boost":1.0}}}}
        System.out.println(searchSourceBuilder.toString());
        // 执行检索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //拿到外层数据hits
        SearchHits hits = searchResponse.getHits();
        //内层hits
        SearchHit[] innerHits = hits.getHits();
        for (SearchHit innerHit : innerHits) {
            String sourceAsString = innerHit.getSourceAsString();
            AccountEntity accountEntity = JSON.parseObject(sourceAsString, AccountEntity.class);
            System.out.println(accountEntity);
        }
        //获取聚合信息
        Aggregations aggregations = searchResponse.getAggregations();

        //AggregationBuilders.terms  转成terms buckets   或者其子类ParsedTerms
        Terms ageAggTerms = aggregations.get("ageAgg");

        List<? extends Terms.Bucket> buckets = ageAggTerms.getBuckets();
        buckets.forEach(bucket -> {
            //1)  "key" (age): 31,
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄:" + keyAsString);
            long docCount = bucket.getDocCount();
            System.out.println("聚合结果个数:" + docCount);

            //2) balance avg  子聚合
            //AggregationBuilders.avg
            Avg balanceAvg = bucket.getAggregations().get("balanceAvg");
            double balanceAvgValue = balanceAvg.getValue();
            System.out.println("此年龄平均薪资：" + balanceAvgValue);
        });

        //平均年龄聚合
        Avg avgAgg = aggregations.get("avgAgg");
        double avgAggValue = avgAgg.getValue();
        System.out.println("平均年龄聚合" + avgAggValue);
    }

    //
    public static class CompletableFutureDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                System.out.println("有返回值");
                int i = 100 / 5;
                return i;
                //returnValue上一部返回的值 whenCompleteAsync 异步处理返回结果
            }, Executors.newFixedThreadPool(10)).handle((returnValue, error) -> {
                System.out.println("returnValue：" + returnValue);
                //能感知到异常，当时无法修改返回数据
                System.out.println("如果出错:" + error);
                //handle方法可以改变返回值
                if (returnValue != null) {
                    return returnValue * 2;
                }
                return 404;
            });
            Integer integer = future.get();
            System.out.println(integer);
        }
    }

    //线程串行化  获取到上一部的执行结果
    public static class CompletableFutureThenDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
//            thenAcceptAsync();
//            thenApplyAsync();
//            thenCombine();
//            thenEither();
            allOfAllAny();
        }

        //能接收上一步的返回结果但是无返回值
        private static void thenAcceptAsync() {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CompletableFuture.supplyAsync(() -> {
                int i = 100 / 10;
                System.out.println("异步任务1启动了 线程号：" + Thread.currentThread().getName());
                return i;
            }, executor).thenAcceptAsync((res) -> {
                System.out.println("异步任务2启动了 线程号：" + Thread.currentThread().getName());
                System.out.println("接收到上一步的返回结果" + res);
            }, executor);
        }

        //接收上一步的返回结果  并返回一个值
        private static void thenApplyAsync() throws ExecutionException, InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                int i = 100 / 10;
                System.out.println("异步任务1启动了 线程号：" + Thread.currentThread().getName());
                return i;
            }, executor).thenApplyAsync((res) -> {
                System.out.println("异步任务2启动了 线程号：" + Thread.currentThread().getName());
                System.out.println("接收到上一步的返回结果" + res);
                return res * 100;
            }, executor);
            Integer integer = future.get();
            System.out.println("接收到改变的返回值：" + integer);
        }

        //合并2换个异步任务2都要完成
        private static void thenCombine() throws ExecutionException, InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            //第一个异步任务
            CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
                System.out.println("异步任务1启动了 线程号：" + Thread.currentThread().getName());
                return "左奶";
            }, executor);

            CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
                System.out.println("异步任务2启动了 线程号：" + Thread.currentThread().getName());
                return "右奶";
            }, executor);

            future1.runAfterBothAsync(future2, () -> {
                System.out.println("异步任务3启动了 线程号：" + Thread.currentThread().getName());
                System.out.println("可以感知2个线程都结束了 但是不能返回值");
            }, executor);

            //参数为 future1 future2 的返回值
            future1.thenAcceptBothAsync(future2, (res1, res2) -> {
                System.out.println("异步任务4启动了 线程号：" + Thread.currentThread().getName());
                System.out.println("可以接收值 但是不能返回值 接受值：" + res1 + res2);
            }, executor);

            //召唤神龙
            String result = future1.thenCombineAsync(future2, (res1, res2) -> {
                System.out.println("召唤升龙");
                return res1 + res2;
            }, executor).get();
            System.out.println(Thread.currentThread().getName() + "=====>" + result);
        }

        // 2个方法只要有一个完成 带async的都可以传executor 异步执行
        private static void thenEither() throws ExecutionException, InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            //第一个异步任务
            CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
                System.out.println("异步任务1启动了 线程号：" + Thread.currentThread().getName());
                return "左奶";
            }, executor);

            CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("异步任务2启动了 线程号：" + Thread.currentThread().getName());
                return "右奶";
            }, executor);

            //只感知线程谁先结束 不接收值不返回值
            future1.runAfterEitherAsync(future2, () -> {
                System.out.println("有线程先完成了 异步线程3开始执行" + Thread.currentThread().getName());
            }, executor);

            //接收参数为 future1 future2 先结束的返回值
            future1.acceptEitherAsync(future2, (res) -> {
                if (res.equals("左奶")) {
                    System.out.println("线程1先结束了接受到线程1的值"+res);
                } else {
                    System.out.println("线程2先结束了接受到线程1的值"+res);
                }
                System.out.println("异步任务4启动了 线程号：" + Thread.currentThread().getName());
            }, executor);

            //接收2个线程先完成的值并返回一个值
            String result = future1.applyToEitherAsync(future2, res -> {
                if (res.equals("左奶")) {
                    System.out.println("线程1先结束了接受到线程1的值"+res);
                } else {
                    System.out.println("线程1先结束了接受到线程1的值"+res);
                }
                System.out.println("异步任务5启动了 线程号：" + Thread.currentThread().getName());
                return  res+"...一个奶也凑合";
            }, executor).get();
            System.out.println(Thread.currentThread().getName()+"异步任务5获取的值=====>"+result);
        }

        // 多个任务组合  只要有线程池 方法就是无限期存在的 阻塞的
        //没有线程池 线程执行完成后会立即结束延迟的线程不会打印结果
        private static void allOfAllAny() throws ExecutionException, InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(10);

            CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
                System.out.println("查看商品图片信息");
                return "beauty.jpg";
            }, executor);


            CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
                System.out.println("查看商品名称");
                return "pistol";
            }, executor);

            CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("查看商品价格");
                return "$99.9";
            }, executor);
            //阻塞式等待 每个都要等前面完成
            //future1.get();future2.get();future3.get();

            //任何一个线程结束 返回值先结束线程的返回值
            CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future1, future2, future3);
            Object o = anyOf.get();
            System.out.println("先结束的线程的商品属性："+o.toString());

            //等待所有结果完成 void 不返回值 返回值需要去get()
            CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2, future3);
            allOf.get();
            System.out.println("main 执行完毕 商品为："+future2.get()+" "+future1.get()+" "+future3.get());
        }
    }
}
