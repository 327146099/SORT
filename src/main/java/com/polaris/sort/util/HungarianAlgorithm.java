package com.polaris.sort.util;

import lombok.Data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HungarianAlgorithm {

    /**
     * @param costMatrix          损失矩阵
     * @param costOfNonAssignment 未指派的损失
     * @return 分配结果
     */
    public static AssignResult assignDetectionsToTracks(double[][] costMatrix, double costOfNonAssignment) {
        int nRow = costMatrix.length;
        int nCol = costMatrix[0].length;
        double[][] distMatrix;
        boolean rotate = false;
        // 如果行大于列, 则旋转矩阵
        if (nRow > nCol) {
            rotate = true;
            distMatrix = rotateMatrix(costMatrix);
        } else {
            distMatrix = costMatrix.clone();
        }
        // 根据未分配损失扩充矩阵
        nRow = distMatrix.length;
        nCol = distMatrix[0].length;

        double[][] matrix = new double[nRow * 2][nCol * 2];
        // 拷贝原矩阵
        for (int i = 0; i < matrix.length; i++) {
            if (i < nRow) {
                System.arraycopy(distMatrix[i], 0, matrix[i], 0, distMatrix[i].length);
                Arrays.fill(matrix[i], distMatrix[i].length, matrix[i].length, Double.MAX_VALUE);
                matrix[i][nCol + i] = costOfNonAssignment;
            } else {
                Arrays.fill(matrix[i], 0, nCol, Double.MAX_VALUE);
                matrix[i][i - nRow] = costOfNonAssignment;
            }
        }


        int[][] solveMatrix = solve(matrix);

        AssignResult assignResult = new AssignResult();

        // 分配矩阵
        List<int[]> assignments = new LinkedList<>();
        // 未分配检测图
        List<Integer> unassignedDetections = new LinkedList<>();
        // 未分配的跟踪器
        List<Integer> unassignedTracks = new LinkedList<>();

        int[] tArray = new int[costMatrix.length];
        int[] dArray = new int[costMatrix[0].length];

        for (int i = 0; i < solveMatrix.length; i++) {
            for (int j = 0; j < solveMatrix[i].length; j++) {
                if (i >= nRow && j >= nCol) {
                    break;
                }
                if (solveMatrix[i][j] != 1) {
                    continue;
                }
                if (i < nRow && j < nCol) {
                    if (rotate) {
                        tArray[j] = 1;
                        dArray[i] = 1;
                        assignments.add(new int[]{j, i});
                    } else {
                        tArray[i] = 1;
                        dArray[j] = 1;
                        assignments.add(new int[]{i, j});
                    }
                } else if (i < nRow) {
                    if (rotate) {
                        dArray[i] = 2;
                    } else {
                        tArray[i] = 2;
                    }
                } else if (j < nCol) {
                    if (rotate) {
                        tArray[j] = 2;
                    } else {
                        dArray[j] = 2;
                    }
                }
            }
        }
        for (int i = 0; i < tArray.length; i++) {
            if (tArray[i] != 1) {
                unassignedTracks.add(i);
            }
        }
        for (int i = 0; i < dArray.length; i++) {
            if (dArray[i] != 1) {
                unassignedDetections.add(i);
            }
        }
        assignResult.setAssignments(assignments);
        assignResult.setUnassignedDetections(transIntegerListToArray(unassignedDetections));
        assignResult.setUnassignedTracks(transIntegerListToArray(unassignedTracks));
        return assignResult;
    }

    private static int[] transIntegerListToArray(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return new int[0];
        }
        int[] res = new int[list.size()];
        int index = 0;
        for (Integer i : list) {
            res[index++] = i;
        }
        return res;
    }

    private static int[][] solve(double[][] distMatrix) {
        boolean done = false;
        int nRow = distMatrix.length;
        int nCol = distMatrix[0].length;

        int k = Math.min(nRow, nCol);

        int step = 1;
        int[] rowCover = new int[nRow];
        int[] colCover = new int[nCol];
        int[][] m = new int[nRow][nCol];
        while (!done) {
            switch (step) {
                case 1:
                    step = stepOne(nRow, nCol, distMatrix);
                    break;
                case 2:
                    step = stepTwo(distMatrix, rowCover, colCover, m);
                    break;
                case 3:
                    step = stepThree(distMatrix, colCover, m, k);
                    break;
                case 4:
                    step = stepFour(distMatrix, rowCover, colCover, m);
                    break;
                case 6:
                    step = stepSix(distMatrix, rowCover, colCover);
                    break;
                case 7:
//                    stepSeven();
                    done = true;
                    break;
                default:
            }
        }
        return m;
    }

    private static void stepSeven() {
        System.out.println("\n\n---------Run Complete----------");
    }

    private static int stepSix(double[][] distMatrix, int[] rowCover, int[] colCover) {
        double minVal = findMmallest(distMatrix, rowCover, colCover);
        for (int r = 0; r < distMatrix.length; r++) {
            for (int c = 0; c < distMatrix[r].length; c++) {
                if (rowCover[r] == 1) {
                    distMatrix[r][c] += minVal;
                }
                if (colCover[c] == 0) {
                    distMatrix[r][c] -= minVal;
                }
            }
        }

        return 4;
    }

    private static double findMmallest(double[][] distMatrix, int[] rowCover, int[] colCover) {
        double minVal = Double.MAX_VALUE;
        for (int r = 0; r < distMatrix.length; r++) {
            for (int c = 0; c < distMatrix[r].length; c++) {
                if (rowCover[r] == 0 && colCover[c] == 0) {
                    if (minVal > distMatrix[r][c]) {
                        minVal = distMatrix[r][c];
                    }
                }
            }
        }
        return minVal;
    }

    private static int stepFive(int[][] m, int pathRow, int pathCol, int[] rowCover, int[] colCover) {
        boolean done;
        int r = -1;
        int c = -1;
        int pathCount = 1;
        List<int[]> path = new LinkedList<>();
        path.add(new int[]{pathRow, pathCol});
        done = false;
        while (!done) {
            r = findStarInCol(path.get(pathCount - 1)[1], m);
            if (r > -1) {
                pathCount += 1;
                path.add(new int[]{r, path.get(pathCount - 2)[1]});
            } else {
                done = true;
            }
            if (!done) {
                c = findPrimeInRow(path.get(pathCount - 1)[0], m);
                pathCount += 1;
                path.add(new int[]{path.get(pathCount - 2)[0], c});
            }
        }
        augmentPath(path, m);
        clearCovers(rowCover, colCover);
        erasePrimes(m);
        return 3;
    }

    private static void augmentPath(List<int[]> paths, int[][] m) {
        for (int[] path : paths) {
            if (m[path[0]][path[1]] == 1) {
                m[path[0]][path[1]] = 0;
            } else {
                m[path[0]][path[1]] = 1;
            }

        }
    }

    private static void erasePrimes(int[][] m) {
        for (int r = 0; r < m.length; r++) {
            for (int c = 0; c < m[r].length; c++) {
                if (m[r][c] == 2) {
                    m[r][c] = 0;
                }
            }
        }
    }


    private static void clearCovers(int[] rowCover, int[] colCover) {
        Arrays.fill(rowCover, 0);
        Arrays.fill(colCover, 0);
    }

    private static int findPrimeInRow(int r, int[][] m) {
        for (int j = 0; j < m[r].length; j++) {
            if (m[r][j] == 2) {
                return j;
            }
        }
        return -1;
    }


    private static int findStarInCol(int c, int[][] m) {
        for (int i = 0; i < m.length; i++) {
            if (m[i][c] == 1) {
                return i;
            }
        }
        return -1;
    }

    private static int[] findAZero(double[][] distMatrix, int[] rowCover, int[] colCover) {
        for (int r = 0; r < distMatrix.length; r++) {
            for (int c = 0; c < distMatrix[r].length; c++) {
                if (distMatrix[r][c] == 0 && rowCover[r] == 0 && colCover[c] == 0) {
                    return new int[]{r, c};
                }
            }
        }

        return new int[]{-1, -1};
    }

    private static int stepFour(double[][] distMatrix, int[] rowCover, int[] colCover, int[][] m) {
        int row;
        int col;
        boolean done;
        done = false;
        int step = 0;
        while (!done) {
            int[] ints = findAZero(distMatrix, rowCover, colCover);
            row = ints[0];
            col = ints[1];
            if (row == -1) {
                done = true;
                step = 6;
            } else {
                m[row][col] = 2;
                if (starInRow(row, m)) {
                    col = findStarInRow(row, m);
                    rowCover[row] = 1;
                    colCover[col] = 0;
                } else {
                    done = true;
                    // 执行第五步
                    stepFive(m, row, col, rowCover, colCover);
                    step = 3;
                }
            }
        }
        return step;
    }

    private static int findStarInRow(int row, int[][] m) {
        for (int c = 0; c < m[row].length; c++) {
            if (m[row][c] == 1) {
                return c;
            }
        }
        return -1;
    }

    private static boolean starInRow(int row, int[][] m) {
        for (int c = 0; c < m[row].length; c++) {
            if (m[row][c] == 1) {
                return true;
            }
        }
        return false;
    }

    private static int stepThree(double[][] distMatrix, int[] colCover, int[][] m, int k) {
        for (int r = 0; r < distMatrix.length; r++) {
            for (int c = 0; c < distMatrix[r].length; c++) {
                if (m[r][c] == 1) {
                    colCover[c] = 1;
                }
            }
        }

        int colcount = 0;
        for (int i : colCover) {
            if (i == 1) {
                colcount += 1;
            }
        }

        if (colcount >= k) {
            return 7;
        } else {
            return 4;
        }
    }

    private static int stepOne(int nRow, int nCol, double[][] distMatrix) {
        double minInRow;
        for (int r = 0; r < distMatrix.length; r++) {
            minInRow = distMatrix[r][0];
            for (int c = 1; c < distMatrix[r].length; c++) {
                if (distMatrix[r][c] < minInRow) {
                    minInRow = distMatrix[r][c];
                }
            }
            for (int c = 0; c < distMatrix[r].length; c++) {
                distMatrix[r][c] -= minInRow;
            }
        }
        return 2;
    }

    private static int stepTwo(double[][] distMatrix, int[] rowCover, int[] colCover, int[][] m) {
        for (int r = 0; r < distMatrix.length; r++) {
            for (int c = 0; c < distMatrix[r].length; c++) {
                if (distMatrix[r][c] == 0 && rowCover[r] == 0 && colCover[c] == 0) {
                    m[r][c] = 1;
                    rowCover[r] = 1;
                    colCover[c] = 1;
                }
            }
        }
        Arrays.fill(rowCover, 0);
        Arrays.fill(colCover, 0);

        return 3;
    }

    public static int[][] rotateMatrix(int[][] matrix) {
        int[][] newMatrix = new int[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                newMatrix[j][i] = matrix[i][j];
            }
        }
        return newMatrix;
    }

    /**
     * 矩阵行列互换
     *
     * @param matrix 输入矩阵
     * @return 行列互换后的矩阵
     */
    public static double[][] rotateMatrix(double[][] matrix) {
        double[][] newMatrix = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                newMatrix[j][i] = matrix[i][j];
            }
        }
        return newMatrix;
    }

    @Data
    public static class AssignResult {

        /**
         * 匹配结果
         */
        private List<int[]> assignments;

        /**
         * 未分配的跟踪器
         */
        private int[] unassignedTracks;

        /**
         * 未分配的检测图
         */
        private int[] unassignedDetections;

    }


}
