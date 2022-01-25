package com.polaris.sort.track;

import com.polaris.sort.entity.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>跟踪 service</p>
 * <p>创建日期：2022-01-25</p>
 *
 * @author 杨洲 yangzhou@neusoft.com
 */
public class TrackService {

    private final List<Tracker> tracks = new ArrayList<>();

    private long nextId = 0;

    private long frameCount = 0;

    /**
     * 预测跟踪器位置
     *
     * @return 预测的位置
     */
    public List<Location> predictNewLocationsOfTracks() {
        if (tracks.isEmpty()) {
            return Collections.emptyList();
        }
        return tracks.stream().map(Tracker::predict)
                .collect(Collectors.toList());
    }

    /**
     * 创建新的跟踪器
     *
     * @param unassignedDetections 未分配的检测结果索引
     * @param dets                 检测到的目标
     */
    public void createNewTracks(int[] unassignedDetections, List<Location> dets) {
        if (unassignedDetections == null || unassignedDetections.length == 0) {
            return;
        }
        for (int index : unassignedDetections) {
            Location location = dets.get(index);
            Tracker tracker = new Tracker(nextId++, location);
            tracks.add(tracker);
        }
    }

    /**
     * 更新分配的轨迹
     *
     * @param assignments 匹配结果
     * @param dets        检测到的目标
     */
    public void updateAssignedTracks(List<int[]> assignments, List<Location> dets) {
        if (assignments == null || assignments.isEmpty()) {
            return;
        }
        for (int[] assignment : assignments) {
            int trackIndex = assignment[0];
            int detIndex = assignment[1];
            Tracker tracker = tracks.get(trackIndex);
            Location location = dets.get(detIndex);
            tracker.update(location);
        }
    }

    public List<Tracker> deleteLostTracks(int minHits, int maxAge) {
        if (tracks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Tracker> res = tracks.stream().filter(tracker -> {
            return tracker.getTimeSinceUpdate() < 1 && (tracker.getHitStreak() >= minHits || frameCount < minHits);
        }).collect(Collectors.toList());
        // 删除过期的跟踪器
        tracks.removeIf(track -> track.getTimeSinceUpdate() > maxAge);
        return res;
    }

    /**
     * 更新未匹配跟踪器
     *
     * @param unassignedTracks 未匹配跟踪器 id
     */
    public void updateUnassignedTracks(int[] unassignedTracks) {
        if (unassignedTracks == null || unassignedTracks.length == 0) {
            return;
        }
        for (int index : unassignedTracks) {
            Tracker tracker = tracks.get(index);
        }
    }

    /**
     * 获取跟踪器
     *
     * @return 跟踪器集合
     */
    public List<Tracker> getTracks() {
        return tracks;
    }

    public List<Location> getTrackLocation() {
        List<Location> location = new ArrayList<>();
        for (Tracker track : tracks) {
            location.add(track.getLocation());
        }
        return location;
    }

}
