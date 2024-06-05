#!/usr/bin/env bash

dockerImage="registry.cn-shenzhen.aliyuncs.com/jetlinks-pro/intelligent-pms-standalone:$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)"
cd ../
./mvnw clean package -T 12 \
-Pstandalone -Pams -Pelms \
-Dmaven.test.skip=true \
-Dmaven.build.timestamp="$(date "+%Y-%m-%d%H:%M:%S")"
if [ $? -ne 0 ];then
    echo "构建失败!"
else
  cd bootstrap || exit
  docker build -t "$dockerImage" . && docker push "$dockerImage"
fi