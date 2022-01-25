package com.polaris.sort;

import jama.Matrix;
import jkalman.JKalman;

import java.util.Arrays;

/**
 * <p>This class represents the internal state of individual tracked objects observed as bbox.</p>
 * <p>创建日期：2022-01-19</p>
 *
 * @author 杨洲 yangzhou@neusoft.com
 */
public class KalmanBoxTracker {

    private final JKalman kalmanFilter;

    public KalmanBoxTracker(double[] bbox) throws Exception {
        // 内部使用KalmanFilter，7个状态变量和4个观测输入
//        kalmanFilter = new KalmanFilter(7, 4);
        // # F是状态变换模型，为7*7的方阵
        double[][] f = {{1, 0, 0, 0, 1, 0, 0},
                {0, 1, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 1},
                {0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1}};
        // # H是量测矩阵，是4*7的矩阵
        double[][] h = {
                {1, 0, 0, 0, 0, 0, 0}, {0, 1, 0, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0, 0}, {0, 0, 0, 1, 0, 0, 0}
        };
        // 先验估计协方差矩阵
        double[] p = {10, 10, 10, 10, Math.pow(10, 4), Math.pow(10, 4), Math.pow(10, 4)};
        // 测量噪声的协方差矩阵
        double[] r = {1, 1, 10, 10};
        // 过程激励噪声的协方差矩阵
        double[] q = {1, 1, 1, 1, 0.01, 0.01, Math.pow(10, -4)};
        kalmanFilter = new JKalman(7, 4);

        kalmanFilter.setTransition_matrix(new Matrix(f));
        kalmanFilter.setMeasurement_matrix(new Matrix(h));
        // # P是先验估计的协方差
        kalmanFilter.setError_cov_pre(new Matrix(byte2DiagMatrix(p)));
        // # R是测量噪声的协方差，即真实值与测量值差的协方差
        kalmanFilter.setMeasurement_noise_cov(new Matrix(byte2DiagMatrix(r)));
        // # Q是过程激励噪声的协方差
        kalmanFilter.setProcess_noise_cov(new Matrix(byte2DiagMatrix(q)));
        // 获得当前值
        double[] currentState = convertBboxToZ(bbox);
        Matrix state = kalmanFilter.getState_post();
        for (int i = 0; i < currentState.length; i++) {
            state.set(i, 0, currentState[i]);
        }
    }

    /**
     * 更新
     *
     * @param bbox 检测框信息
     */
    public void update(double[] bbox) {
        double[] bboxToZ = convertBboxToZ(bbox);
        Matrix matrix = new Matrix(bboxToZ.length, 1);
        for (int i = 0; i < bboxToZ.length; i++) {
            matrix.set(i, 0, bboxToZ[i]);
        }
        //#根据观测结果修改内部状态x
        this.kalmanFilter.Correct(matrix);
    }

    /**
     * 预测新位置
     *
     * @return 新位置信息
     */
    public double[] predict() {
        Matrix statePre = kalmanFilter.getState_post();
        if ((statePre.get(6, 0) + statePre.get(2, 0)) <= 0) {
            statePre.set(6, 0, 0.0);
        }
        // 预测
        kalmanFilter.Predict();

        double[][] preState = this.kalmanFilter.getState_pre().getArray();
        return convertXToBbox(preState);
    }

    private static double[][] byte2DiagMatrix(double[] arr) {
        int length = arr.length;
        double[][] matrix = new double[length][length];
        for (int i = 0; i < arr.length; i++) {
            matrix[i][i] = arr[i];
        }
        return matrix;
    }

    /**
     * 将[x1,y1,x2,y2]形式的检测框转为滤波器的状态表示形式[x,y,s,r]。
     * 其中x、y是框的中心坐标点，s 是面积尺度，r 是宽高比w/h
     * :param bbox: [x1,y1,x2,y2] 分别是左上角坐标和右下角坐标 即 [左上角的x坐标，左上角的y坐标，右下角的x坐标，右下角的y坐标]
     * :return: [ x, y, s, r ] 4行1列，其中x、y是box中心位置的坐标，s是面积，r是纵横比w/h
     *
     * @param bbox 矩形框
     */
    private static double[] convertBboxToZ(double[] bbox) {
        double w = bbox[2] - bbox[0];
        double h = bbox[3] - bbox[1];
        double x = bbox[0] + w / 2;
        double y = bbox[1] + h / 2;
        double s = w * h;
        double r = w / h;
        return new double[]{
                x, y, s, r
        };
    }

    /**
     * Takes a bounding box in the centre form [x,y,s,r] and returns it in the form
     * [x1,y1,x2,y2] where x1,y1 is the top left and x2,y2 is the bottom right
     */
    private static double[] convertXToBbox(double[][] x) {

        double w = Math.sqrt(x[2][0] * x[3][0]);
        double h = x[2][0] / w;
        return new double[]{x[0][0] - w / 2, x[1][0] - h / 2, x[0][0] + w / 2., x[1][0] + h / 2};

    }


    public static void main(String[] args) throws Exception {
        double x = 1, y = 5;
        KalmanBoxTracker kalmanBoxTracker = new KalmanBoxTracker(new double[]{x, x, y, y});
        for (int i = 1; i < 30; i++) {
            double[] predict = kalmanBoxTracker.predict();
            System.out.println(Arrays.toString(predict));
            kalmanBoxTracker.update(new double[]{x + i, x + i, y + i, y + i});
        }
    }

}
