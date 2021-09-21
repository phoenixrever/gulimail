FROM java:8
EXPOSE 8080

VOLUME /tmp
ADD renren-fast.jar  /app.jar
RUN bash -c 'touch /app.jar' #如果已经有了 只是改下修改时间 具体没啥用
#容器启动默认运行的命令
ENTRYPOINT ["java","-jar","/app.jar"]
