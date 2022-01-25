package com.polaris.sort.util;

public class SortUtils {

    /**
     * Computes IOU between two bboxes in the form [x1,y1,x2,y2]
     *
     * @return
     */
    public static double iou(double[] bb_test, double[] bb_gt) {
        double xx1 = Math.max(bb_test[0], bb_gt[0]);
        double yy1 = Math.max(bb_test[1], bb_gt[1]);
        double xx2 = Math.min(bb_test[2], bb_gt[2]);
        double yy2 = Math.min(bb_test[3], bb_gt[3]);
        double w = Math.max(0, xx2 - xx1);
        double h = Math.max(0, yy2 - yy1);
        double wh = w * h;
        //  #IOU =（bb_test和bb_gt框相交部分面积）/(bb_test框面积 + bb_gt框面积 - 两者相交面积)
        return wh / ((bb_test[2] - bb_test[0]) * (bb_test[3] - bb_test[1])
                + (bb_gt[2] - bb_gt[0]) * (bb_gt[3] - bb_gt[1]) - wh);
    }

}
