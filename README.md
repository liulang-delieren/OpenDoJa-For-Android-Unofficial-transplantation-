关于面向隔壁某些“怀旧游戏群体”的声明：

流浪的猎人不欢迎任何bilibili和某些公开平台的内容创作者，别觉得任何人做一个东西就该白给你拿去糊弄别人+自以为是的附加情绪价值，也别真觉得“自己想天天吹大神多么多么伟大帮你白嫖想忽悠别人无偿帮你白干活”是干了啥了不起的事了，别一天天看见啥稀奇玩意就跑bilibili起某些堪比“哥伦布发现新大陆”的辣鸡没见识标题发视频吹别人了，我才不需要这种情绪价值来做这件事，我也没有义务面向“等着其他人发没见过的稀奇玩意一通乱吹的群体服务”。


都是在给Discord的大神当搬运工+二次创作，谁也不比谁高级，你们居然还好意思自我感动上了，笑死。没有人有义务在自己发布了一些东西就该被你们直接使用，也别找我发类似“我第一次用GitHub，别TM给我代码，我只要安装包”的伸手党行为，Github不是“一站式资源下载站”，也别问我为什么不直接提供APK了


不明白什么意思？某些站台二道贩子的家伙别一天天真把自己干那些事情当回事，我只知道我干这事和bilibili这种傻缺玩情绪价值没有半毛钱关系，不要一天天的看别人干啥了起个“XX年前神作”然后隔这自我感动起来了，做这件事哪怕不和隔壁那帮神经病一样“又当标题党又玩情怀”也不会有任何影响。


PS：本项目正在开发中，是否会提供可直接安装的APK会根据当前互联网环境酌情考虑，流浪的猎人只会遵循GPL V3协议开源OpenDoJa For Android的所有代码。


关于i-mode： i-mode（iモード）是由日本DoCoMo公司于1999年推出的日本移动互联网服务，是全球首个允许手机接入互联网并使用多种网络服务的移动互联网服务。为i-mode服务开发的应用程序被称为i-αppli（iアプリ），运行在DoCoMo Java（DoJa）平台上。i-mode网站作为该服务的一部分，提供电子游戏商店服务。i-mode网站已于2021年11月30日关闭。整个i-mode服务则于2026年3月23日正式停止运营。

OpenDoJa For Android(非官方移植)


移植：流浪的猎人(Wandering Hunter)

这是“OpenDoJa”的非官方Android移植，由流浪的猎人(Wandering Hunter)使用OpenCode+Xiaomi Mimo V2.6免费版实现移植，这个版本的出现只是因为流浪的猎人自己想在Android掌机上玩这些游戏，然后尝试基于OpenDoJa 0.25版本的源代码实现了移植，本项目的移植不属于“流浪的猎人响应任何白嫖党需求造福人类”的产物，属于流浪的猎人解决部分个人需求之后公开代码。



目前OpenDoJa For Android(非官方移植)的兼容性暂时无法和OpenDoJa的Windows版本相对比，这是一个临时过渡方案，我本人和模拟器开发几乎是沾不上边的，建议各位还是等待OpenDoJa的官方Android版本。



本移植版本要求Android9.0以上系统，不包含lib库依赖，所以理论上可以在大部分Android设备运行，OpenDoJa For Android可以让你在Android系统运行日本DoCoMo Java(DoJa)平台的手机游戏。



目前实现的一些功能：



按键映射(目前不会自动识别Android设备的手柄硬件，需要用户自行映射)



开启/关闭屏幕虚拟键盘



安装/删除游戏



已知BUG还没有修复：



1.GUNDAM U.C 0079的FPS异常BUG



2.怪物猎人i的一些图像BUG



3.部分游戏爆音+掉帧



随机BUG：东方水幻境多运行几次有可能会出现网络效验



感谢名单：



本项目在开发Android版本的过程中引用了JL-MOD开源代码里面的JAR2DEX实现方式，这里要感谢JL-MOD和J2ME Loader的开发者。



还有Magstic在本项目开发过程中提供的一些思路和指出的问题，还有对部分Doja API问题的解答。



还有Keitai World Discord频道的一些人，没有你们在这之前对手机的Dump和维护就不会有这个项目了。



最后我还要感谢OpenDoJa的开发者，如果没有OpenDoja项目，我是没有可能在他的基础之上尝试移植到Android系统的。



OpenDoJa For Android将会遵循GPL V3协议开源所有代码，本项目仅提供代码，不会提供已经编译完成的APK，请使用Android Studio尝试编译。



版本号:



20260930 



第一个版本，修复了部分图像渲染和API在Android系统调用的BUG，未完全修复GUNDAM U.C 0079的FPS异常BUG和怪物猎人i的一些图像BUG，可能有一些游戏仍然存在BUG。



       OpenDoJa For Android（非官方移植）—— 构建 APK 说明



一、环境要求
------------------------------------------------------------------------

1. JDK 17 或以上（推荐使用 Android Studio 自带的 jbr 目录，或 JDK 17/21）
2. Android SDK，且已安装 Android SDK Platform 34
   （可用 Android Studio 的 SDK Manager 安装，或命令行：
    sdkmanager "platforms;android-34"）
3. 首次编译需要联网：自动下载 Gradle 8.13 发行版、AGP 8.13.2 及依赖
   （数百 MB 级）
4. 可选：adb（用于把编译好的 APK 安装到手机）

------------------------------------------------------------------------

二、获取代码
------------------------------------------------------------------------

方式一：git 克隆

    git clone https://github.com/liulang-delieren/OpenDoJa-for-Android-Unofficial-Port-.git
    cd OpenDoJa-for-Android-Unofficial-Port-

方式二：在 GitHub 页面点击 Code -> Download ZIP，解压后进入目录。

------------------------------------------------------------------------

三、配置 Android SDK 路径（二选一）
------------------------------------------------------------------------

方式 1：在项目根目录新建文件 local.properties（内容如下，该文件已被
.gitignore 忽略，不会提交到仓库）：

    # Windows
    sdk.dir=C:/Users/你的用户名/AppData/Local/Android/Sdk
    
    # macOS / Linux 示例
    # sdk.dir=/Users/用户名/Library/Android/sdk

方式 2：设置环境变量 ANDROID_HOME（或 ANDROID_SDK_ROOT），指向 SDK 目录，
        此时不需要 local.properties 文件。

------------------------------------------------------------------------

四、编译 APK
------------------------------------------------------------------------

Windows（CMD 或 PowerShell），在项目根目录执行：

    gradlew.bat assembleDebug

macOS / Linux，在项目根目录执行：

    chmod +x gradlew          # 仅首次需要
    ./gradlew assembleDebug

等待出现 BUILD SUCCESSFUL 即成功，编译产物路径：

    app/build/outputs/apk/debug/app-debug.apk

说明：

- 产物为 debug 签名 APK（使用本机 ~/.android/debug.keystore），可直接
  安装到手机。如需发布正式版，请自行在 app/build.gradle 配置
  signingConfig 并使用你自己的 keystore。
- 仓库中的 run-build.cmd 和 run-build-nolog.cmd 是作者电脑专用的快捷
  脚本（写死了作者机器上的 Gradle 和 JDK 路径），其他电脑请直接使用
  上面的 gradlew 命令，或打开脚本修改开头几行路径后再用。

------------------------------------------------------------------------

五、安装（可选）
------------------------------------------------------------------------

    adb install -r app\build\outputs\apk\debug\app-debug.apk

系统要求：Android 9.0（API 28）及以上。

------------------------------------------------------------------------

六、编译常见问题
------------------------------------------------------------------------

1. 报错 SDK location not found
   -> 按第三节配置 local.properties 的 sdk.dir 或设置 ANDROID_HOME。

2. 报错 Failed to find target with hash string 'android-34'
   -> 用 SDK Manager 安装 Android SDK Platform 34。

3. 报错 Java 版本问题（invalid source release、
   Unsupported class file major version 等）
   -> 把 JAVA_HOME 指向 JDK 17+；Android Studio 用户可指向其安装目录
      下的 jbr 文件夹。

4. 依赖下载失败或很慢
   -> settings.gradle 开头把阿里云镜像放在最前面；若你的网络访问不了
      阿里云，删除其中 maven.aliyun.com 的几行即可，后面已经有
      google() 和 mavenCentral() 兜底。

5. 编译成功但游戏内文字乱码（移植特性说明）
   -> 不要升级 targetSdk，必须保持 30，原因见 app/build.gradle 中的
      注释。

6. 本项目不内置任何游戏，安装后通过应用内 右上角⋮ -> 加载JAM 导入
   .jam / .jar 游戏文件。

========================================================================









原始OpenDoja介绍：



openDoJa 是一个专注于桌面端的、基于“洁净室”原则重构的 DoJa 5.1 运行时及相关 API 实现，旨在让 i-appli Java 游戏能在现代计算机上运行。



系统要求



Java 22 及以上



构建



mvn -q -DskipTests package



GitHub Actions



每次向 master 分支推送代码时，都会重新构建 GitHub 的滚动夜间版本，并替换其附带的 JAR 文件。



当 pom.xml 中的版本号发生变化时，系统会自动创建一个 GitHub 发布包。



下载



夜间构建（最新）版本：https://github.com/GrenderG/openDoJa/releases/download/nightly/opendoja-nightly.jar



最新（稳定）版本：https://github.com/GrenderG/openDoJa/releases/latest

