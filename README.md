# 智慧园区管理系统（Intelligent Park Management System）

模块说明

```bash
---intelligent-pms
        |--bootstrap 单体项目启动模块
        |--micro-services 微服务相关模块
        |-----|----api-gateway-service API网关服务
        |-----|----authentication-service  IAM统一认证服务
        |-----|----file-service 文件服务
        |-----|----iot-service IoT物联网服务
        |-----|----low-code-service 低代码服务
        |--modules 子模块
```
## 使用

### github

在github中使用时,直接打开地址: https://github.com/jetlinks-v2/intelligent-pms/generate .
创建新的仓库即可.

### gitee

新建仓库时选择使用模版仓库 `intelligent-pms`

### 本地创建

```bash
git clone -o template git@github.com:jetlinks-v2/intelligent-pms.git {新的项目名称}

cd {新的项目名称}

#添加新的远程仓库地址
git remote add origin {项目远程仓库地址}

# 执行编译看是否正常
mvn clean compile
```

### 配置maven密码

修改maven的`settings.xml`,添加:
```xml
<servers>
    <server>
        <id>jetlinks</id>
        <username>{用户名}</username>
        <password>{密码}</password>
    </server>
</servers>

```

## 开发

项目开发建议使用maven模块化管理,在`modules`目录创建相应的业务模块,然后在`bootstrap`模块中引入依赖进行启动.