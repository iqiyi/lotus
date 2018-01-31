
# DataReact（代号Lotus）

DataReact是用于解决页面间数据传递，通知事件，消息联动等问题的互动框架。DataReact基于经典的观察者模式搭载了Google Lifecycle-aware 从而观察者具有了生命周期属性。
<br/>
<img src="data-react.png" width="500" height="187"/>

## 适用业务场景：

*  生命周期互动事件： 当数据变化时发生事件数据通知给观察者，希望观察者在激活的状态（通常onResume）下收到通知执行任务（如更新UI、做动效、弹Dialog等）。
*   页面模块间普通互动事件：当数据变化时需要发送数据通知给 关心这类数据变化的观察者。例如：从Feed流进详情页，Feed详情页禁言 返回Feed流时需要同步禁言状态到Feed流。
*   互斥事件的互动事件：当设置观察者为互斥观察者时，则同一类数据通知只被一个观察者收到（默认后面覆盖前面）。例如：在用户未登录状态下圈子feed流举报、feed详情页举报、Feed流删除feed等多个地方会触发登录，而这几个地方都需要关注登录成功的结果，同时这几个地方有事互斥的。
*   数据接收方（观察者）接收数据事件通知，做拦截处理。     
## DataReact具有以下一些特性：
* 观察者具有生命周期属性
由于互动框架是基于Lifecycle-aware, 观察者注册时绑定自己所属的LifecycleOwner（Activity/Fragment）,则当有发布者发布事件数据，数据池接收数据在观察者LifecycleOwner状态为active时通知观察者。 LifecycleOwner即Activity/Fragment销毁时（onDestroy）绑定该LifecycleOwner的观察者会被解绑，使用者无需关心解绑。

* 全局单实例数据分发池
DataReact全局只有一个数据池，当调用者调用DataReact的API事实上是操作同一个数据池实例。所以，事件数据放送 方和接收方无需顾虑是否多实例问题，方便跨业务模块之间互动场景实现。

*  事件通知发布更高效
事件数据发布时，DataReact 并非通知所有观察者数据变化（全量通知），而是通知对应类型（DataReactType）的观察者数据变化（部分通知），甚至当注册观察者时绑定了ID，DataReact会精准通知到这一观察者，从而使事件数据发布效率更高。

*   支持互斥事件观察者
                针对部分业务场景，DataReact提供互斥观察者的支持，即同一类型（DataReactType）的这种观察者只能存在一个。

*   支持非生命周期观察者
    为了兼容老的业务场景，DataReact提供普通非生命周期的观察者注册方式，这种情况需要调用者手动去解注册观察者。

*   事件拦截处理机制
在观察者收到数据变化的通知后，观察者可以对收到的数据进行修改，之后观察者收到修改后的数据。甚至废弃，之后观察者收不到数据。

## 使用方法介绍：

1. 定义想要观察的数据类型比如在 DataReactType 类中:

```java
 public static final String LOGIN_SUCCESS = "type_1"
```

2. 代码中设置观察者:
*  普通方式：

```java
DataReact.observe(DataReactType.PLAY_VIDEO, (LifecycleRegistryOwner) context, new Observer<Data>() {
            @Override
            public void onChanged(@Nullable Data data) {
            //do something
            Log.e(TAG, "get data:" + data.getData());
            }
        });
```

* 注解方式：

```java
        @DataSubscribe(dataType ={DataReactType.LOGIN_SUCCESS})
        public void onLoginSuccess(Data data) { 
            //do something
            Log.e(TAG, "get data:" + data.getData());
        } 
```

3. Set data:

```java
  DataReact.set(new Data(DataReactType.LOGIN_SUCCESS).setData(userInfo));
```

