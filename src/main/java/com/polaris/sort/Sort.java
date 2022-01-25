package com.polaris.sort;

import com.polaris.sort.entity.Location;
import com.polaris.sort.entity.Position;
import com.polaris.sort.entity.TrackResult;
import com.polaris.sort.track.TrackService;
import com.polaris.sort.track.Tracker;
import com.polaris.sort.util.HungarianAlgorithm;
import com.polaris.sort.util.SortUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>sort 多目标跟踪算法</p>
 * <p>创建日期：2022-01-19</p>
 *
 * @author 杨洲 yangzhou@neusoft.com
 */
public class Sort {

    /**
     * 最大遗失帧数
     */
    private int maxAge = 1;

    /**
     * 确认目标存在的最小连续跟踪帧数
     */
    private int minHits = 3;

    /**
     * iou阈值
     */
    private double iouThreshold = 0.3;

    /**
     * 帧计数
     */
    private long frameCount;

    private final TrackService trackService = new TrackService();

    /**
     * 输入的是检测结果[x1,y1,x2,y2,score]形式
     *
     * @param dets 检测结果
     * @return 跟踪结果
     */
    public List<TrackResult> update(List<Location> dets) {
        frameCount++;
        // get predicted locations from existing trackers.
        List<Location> predictLocations = trackService.predictNewLocationsOfTracks();
        // 匈牙利匹配算法进行匹配
        HungarianAlgorithm.AssignResult assignResult = detectionToTrackAssignment(predictLocations, dets, iouThreshold);
        // 分配好的轨迹更新
        trackService.updateAssignedTracks(assignResult.getAssignments(), dets);
        // 未分配的轨迹更新
        trackService.updateUnassignedTracks(assignResult.getUnassignedTracks());
        // 创建新轨迹
        trackService.createNewTracks(assignResult.getUnassignedDetections(), dets);
        // 删除丢掉的轨迹
        List<Tracker> trackers = trackService.deleteLostTracks(minHits, maxAge);
        return trackers.stream().map(tracker -> {
            TrackResult trackResult = new TrackResult();
            trackResult.setId(tracker.getId());
            Location location = tracker.getLocation();
            trackResult.setPosition(new Position(location.getX1(), location.getY1(), location.getX2() - location.getX1(), location.getY2() - location.getY1()));
            return trackResult;
        }).collect(Collectors.toList());
    }

    /**
     * 匹配
     *
     * @param tracks 跟踪器列表
     * @param dets   检测的跟踪信息
     * @return 匹配结果
     */
    private HungarianAlgorithm.AssignResult detectionToTrackAssignment(List<Location> tracks, List<Location> dets, double iouThreshold) {
        if (tracks == null || tracks.isEmpty() || dets == null || dets.isEmpty()) {
            HungarianAlgorithm.AssignResult assignResult = new HungarianAlgorithm.AssignResult();

            // 设置未匹配的检测图
            if (tracks == null || tracks.isEmpty()) {
                if (dets != null && !dets.isEmpty()) {
                    int[] unassignedDetections = new int[dets.size()];
                    for (int i = 0; i < unassignedDetections.length; i++) {
                        unassignedDetections[i] = i;
                    }
                    assignResult.setUnassignedDetections(unassignedDetections);
                }
            }

            // 设置未匹配的跟踪器
            if (dets == null || dets.isEmpty()) {
                if (tracks != null && !tracks.isEmpty()) {
                    int[] unassignedTracks = new int[tracks.size()];
                    for (int i = 0; i < unassignedTracks.length; i++) {
                        unassignedTracks[i] = i;
                    }
                    assignResult.setUnassignedTracks(unassignedTracks);
                }
            }

            return assignResult;
        }
        double[][] costMatrix = new double[tracks.size()][dets.size()];
        int i = 0;
        for (Location track : tracks) {
            int j = 0;
            for (Location bbox : dets) {
                // 损失矩阵计算, 为两个矩阵距离
                costMatrix[i][j++] = 1 - SortUtils.iou(track.toArray(), bbox.toArray());
            }
            i++;
        }

        // 获取匹配结果
        return HungarianAlgorithm.assignDetectionsToTracks(costMatrix, 1 - iouThreshold);
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setMinHits(int minHits) {
        this.minHits = minHits;
    }

    public void setIouThreshold(double iouThreshold) {
        this.iouThreshold = iouThreshold;
    }

    public long getFrameCount() {
        return frameCount;
    }
}
