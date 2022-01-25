# SORT

多目标跟踪 java 实现, 使用卡尔曼滤波器进行位置预测

## 使用方式

```java
Sort sort = new Sort();
List<Location> detections = new ArrayList<>();
List<TrackResult> update = sort.update(detections);
```
detections为检测到的目标信息,不管是否检测到目标,每帧都需要调用`sort.update()`方法

方法中的卡尔曼滤波器调用JKalman实现,使用时注意引入`JKalman.jar`

[参考引用](https://blog.csdn.net/qq_40608730/article/details/118710715)
