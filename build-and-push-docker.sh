#!/usr/bin/env bash
servers="$1"

if [ -z "$servers" ]||[ "$servers" = "all" ];then
servers="api-gateway-service,authentication-service,iot-service,file-service,asset-service,elms-service,subsystem-service"
fi

IFS=","
arr=($a)

version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "start build : $servers : $version"

./mvnw -Dmaven.test.skip=true \
-Pmicroservice \
-Dmaven.build.timestamp="$(date "+%Y-%m-%d %H:%M:%S")" \
-Dgit-commit-id="$(git rev-parse HEAD)" \
clean package
if [ $? -ne 0 ];then
    echo "构建失败!"
else

for s in ${servers}
do
 cd "./micro-services/${s}" || exit
 dockerImage="registry.cn-shenzhen.aliyuncs.com/jetlinks-pro/intelligent-pms-microservice-$s:$version"
 echo "build $s docker image $dockerImage"
 docker build -t "$dockerImage" . && docker push "$dockerImage"
 cd ../../
done

fi