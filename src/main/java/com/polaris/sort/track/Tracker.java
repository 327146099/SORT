package com.polaris.sort.track;

import com.polaris.sort.KalmanBoxTracker;
import com.polaris.sort.entity.Location;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>跟踪器</p>
 * <p>创建日期：2022-01-25</p>
 *
 * @author 杨洲 yangzhou@neusoft.com
 */
@Data
public class Tracker {

    private long id;

    private int hits;

    private int hitStreak;

    private int age;

    /**
     * 连续预测的次数,每执行predict一次即进行time_since_update+=1
     */
    private int timeSinceUpdate;

    /**
     * 运动历史
     */
    private List<Location> history = new ArrayList<>();

    /**
     * 位置信息
     */
    private Location location;

    /**
     * 卡尔曼滤波器
     */
    private KalmanBoxTracker kalmanBoxTracker;

    @SneakyThrows
    public Tracker(long id, Location location) {
        this.id = id;
        this.location = location;
        kalmanBoxTracker = new KalmanBoxTracker(location.toArray());
    }

    /**
     * 预测新位置
     *
     * @return 预测的位置
     */
    public Location predict() {
        double[] predict = kalmanBoxTracker.predict();
        Location predictLocation = new Location(predict[0], predict[1], predict[2], predict[3], 1.0);
        location = predictLocation;
        age++;
        if (timeSinceUpdate > 0) {
            hitStreak = 0;
        }
        timeSinceUpdate++;
        history.add(predictLocation);
        return predictLocation;
    }

    /**
     * 更新
     */
    public void update(Location location) {
        timeSinceUpdate = 0;
        hits++;
        hitStreak++;
        kalmanBoxTracker.update(new double[]{location.getX1(), location.getY1(), location.getX2(), location.getY2()});
    }

    public Location getState() {
        return location;
    }

}
