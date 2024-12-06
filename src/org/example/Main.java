package org.example;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class Main {


    static {
        try {
            System.load("F:\\game\\Programs\\2.4.9\\opencv\\\\build\\java\\x64\\opencv_java249.dll");
            System.out.println("OpenCV library loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV native library.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int[] xAlign;
    private static int[] yAlign;
    private static int numAlign;
    private static int qrSize = 0;

    public static void toBlockMatrixQR(int[][] qrBlockMatrix) {
        try {
            File inputFile = new File("output.jpg");
            BufferedImage inputImage = ImageIO.read(inputFile);
            BufferedImage grayImage = QRTransform.convertToGrayImage(inputImage);
            int[][] binaryMatrix = QRTransform.convertToBinaryMatrix(grayImage);
            QRTransform.convertToBlockMatrix(binaryMatrix, qrBlockMatrix, grayImage.getWidth(), grayImage.getHeight(), 7, QRTransform.getBlockSize(), QRTransform.getqrWidth(), qrSize);
            BufferedImage matrixToImage = QRTransform.drawImageFromBinaryMatrix(qrBlockMatrix);
            File f = new File("qrBlockMatrix.png");
            ImageIO.write(matrixToImage, "png", f);

        } catch (Exception e) {
            System.out.println("Somethong wrong processing");
        }
    }


    public static int calculateVersion() {
        System.out.println("Start Calculate version");
        BufferedImage inputImage;
        try {
            File inputFile = new File("QRAffine.png");
            inputImage = ImageIO.read(inputFile);
        } catch (IOException e) {
            System.out.println("Error Load Image");
            throw new RuntimeException(e);
        }
        int offsetEye = QRTransform.getoffsetEye();
        int[][] binaryImage = QRTransform.convertToBinaryMatrix(inputImage);
        int qrWidth = QRTransform.getqrWidth();
        int qrBlockSize = QRTransform.getBlockSize();
        int offsetX = offsetEye + qrBlockSize * 3;
        int offsetY = offsetEye + qrBlockSize * 3;
        int[] countBlock = {13, 0};
        System.out.println("offsetX=" + offsetX);

        for (int x = offsetX; x < qrWidth - offsetEye; x++) {
            int currentState = binaryImage[x][offsetY];
            for (int i = x; i < qrWidth - offsetEye; i++) {
                if (binaryImage[i][offsetY] != currentState) {
                    //System.out.println("i=" + i + " ,currentstate=" + currentState);
                    countBlock[currentState]++;
                    currentState = binaryImage[i][offsetY];
                    x = i;
                }
            }
        }
        int totalBlockH = countBlock[0] + countBlock[1];
        System.out.println("countBlockBlackH=" + countBlock[0]);
        System.out.println("countBlockWhiteH=" + countBlock[1]);
        countBlock[0] = 13;
        countBlock[1] = 0;
        for (int y = offsetY; y < qrWidth - offsetEye; y++) {
            int currentState = binaryImage[offsetX][y];
            for (int i = y; i < qrWidth - offsetEye; i++) {
                if (binaryImage[offsetX][i] != currentState) {
                    //System.out.println("i=" + i + " ,currentstate=" + currentState);
                    countBlock[currentState]++;
                    currentState = binaryImage[offsetX][i];
                    y = i;
                }
            }
        }
        int totalBlockV = countBlock[0] + countBlock[1];
        System.out.println("countBlockBlackV=" + countBlock[0]);
        System.out.println("countBlockWhiteV=" + countBlock[1]);

        int expectedBlockCount = Math.round(qrWidth / qrBlockSize);
        System.out.println("expectedBlock=" + expectedBlockCount);
        double percentageH = QRTransform.percentageDif((double) totalBlockH, (double) expectedBlockCount);
        double percentageV = QRTransform.percentageDif((double) totalBlockV, (double) expectedBlockCount);
        int totalBlock = 0;
        System.out.println("percentageH=" + percentageH);
        System.out.println("percentageV=" + percentageV);
        if (percentageH > percentageV) {
            totalBlock = totalBlockV;
        } else {
            totalBlock = totalBlockH;
        }
        qrSize = totalBlock;
        int version = (totalBlock - 21) / 4 + 1;
        return version;
    }

    public static void findAlignmentPattern(String inputPath) {
        int qrWidth = QRTransform.getqrWidth();
        int qrBlockSize = QRTransform.getBlockSize();
        System.out.println("Start Find Alignment Pattern");
        //QRTransform.startFindEye();
        try {
            File inputFile = new File(inputPath);
            BufferedImage inputImage = ImageIO.read(inputFile);
            BufferedImage grayImage = QRTransform.convertToGrayImage(inputImage);
            System.out.println("Start Find Alignment Pattern here");
            int[][] binaryImage = QRTransform.convertToBinaryMatrix(grayImage);
            xAlign = new int[qrWidth + 1];
            yAlign = new int[qrWidth + 1];
            numAlign = QRTransform.findAlignPattern(binaryImage, grayImage.getWidth(), grayImage.getHeight(), xAlign, yAlign, qrBlockSize);
        } catch (IOException e) {
            System.out.println("Error Load Image");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        // Load an image and apply the homography
        String imagePath = "QR8.png"; // Replace with your image path
        Mat srcImage = Highgui.imread(imagePath);
        QRTransform.startFindEye(imagePath);
        findAlignmentPattern(imagePath);


        int xEye1 = QRTransform.getxEye1();
        int xEye2 = QRTransform.getxEye2();
        int xEye3 = QRTransform.getxEye3();
        int yEye1 = QRTransform.getyEye1();
        int yEye2 = QRTransform.getyEye2();
        int yEye3 = QRTransform.getyEye3();
        int xAlignBefore = xAlign[numAlign - 1];
        int yAlignBefore = yAlign[numAlign - 1];
        int qrWidth = QRTransform.getqrWidth();
        int qrBlockSize = QRTransform.getBlockSize();
        int qrOffsetEye = QRTransform.getoffsetEye();
        System.out.println("qrWidth= " + qrWidth);
        System.out.println("qrBlockSize= " + qrBlockSize);
        System.out.println("qrOffsetEye= " + qrOffsetEye);


        int version = calculateVersion();
        System.out.println("qrSize= " + qrSize);
        int qrVersion = calculateVersion();
        System.out.println("qrVersion= " + qrVersion);

        int xNew1 = qrBlockSize * 4;
        int yNew1 = qrBlockSize * 4;
        int xNew2 = qrBlockSize * (qrSize - 4 + 1);
        int yNew2 = qrBlockSize * 4;
        int xNew3 = qrBlockSize * 4;
        int yNew3 = qrBlockSize * (qrSize - 4 + 1);

        int xAlignTransform = 18 + 4 * (qrVersion - 2);
        int yAlignTransform = 18 + 4 * (qrVersion - 2);
        Point[] srcPoints = {
                new Point(xEye1, yEye1),
                new Point(xEye2, yEye2),
                new Point(xEye3, yEye3),
                new Point(xAlignBefore, yAlignBefore)

        };

//        Point[] dstPoints = {
//                new Point(qrOffsetEye, qrOffsetEye),
//                new Point(qrWidth - qrOffsetEye, qrOffsetEye),
//                new Point(qrOffsetEye, qrWidth - qrOffsetEye),
//                new Point(xAlignTransform * qrBlockSize + offsetQR, yAlignTransform * qrBlockSize + offsetQR)
//        };
        Point[] dstPoints = {
                new Point(xNew1, yNew1),
                new Point(xNew2, yNew2),
                new Point(xNew3, yNew3),
                new Point((xAlignTransform + 1) * qrBlockSize, (yAlignTransform + 1) * qrBlockSize)
        };

        // Convert points to Mat
        MatOfPoint2f srcMat = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dstMat = new MatOfPoint2f(dstPoints);

        // Compute homography using RANSAC
        Mat homographyMatrix = Calib3d.findHomography(srcMat, dstMat, Calib3d.RANSAC, 3);

        // Print the homography matrix
        System.out.println("Homography Matrix:");
        System.out.println(homographyMatrix.dump());


        if (srcImage.empty()) {
            System.out.println("Error: Cannot load image!");
            return;
        }

        Mat dstImage = new Mat();
        Imgproc.warpPerspective(srcImage, dstImage, homographyMatrix, srcImage.size());

        String outputPath = "output.jpg";
        Highgui.imwrite(outputPath, dstImage);
        System.out.println("Transformed image saved as output.jpg");
        System.out.println("Convert to block Matrix");
        int[][] qrMatrix = new int[qrSize][qrSize];
        toBlockMatrixQR(qrMatrix);

    }

    /**
     * Calculate the affine transform matrix for three points.
     *
     * @param src  Source points [x0, y0, x1, y1, x2, y2].
     * @param dest Destination points [x0', y0', x1', y1', x2', y2'].
     * @return AffineTransform matrix.
     */
    private static AffineTransform calculateAffineTransform(double[] src, double[] dest) {
        double x0 = src[0], y0 = src[1], x1 = src[2], y1 = src[3], x2 = src[4], y2 = src[5];
        double x0p = dest[0], y0p = dest[1], x1p = dest[2], y1p = dest[3], x2p = dest[4], y2p = dest[5];

        double dx1 = x1 - x0, dy1 = y1 - y0;
        double dx2 = x2 - x0, dy2 = y2 - y0;
        double dx1p = x1p - x0p, dy1p = y1p - y0p;
        double dx2p = x2p - x0p, dy2p = y2p - y0p;

        double det = dx1 * dy2 - dx2 * dy1;
        double a11 = (dx1p * dy2 - dx2p * dy1) / det;
        double a21 = (dy1p * dy2 - dy2p * dy1) / det;
        double a12 = (dx2p * dx1 - dx1p * dx2) / det;
        double a22 = (dy2p * dx1 - dy1p * dx2) / det;
        double a13 = x0p - a11 * x0 - a12 * y0;
        double a23 = y0p - a21 * x0 - a22 * y0;

        return new AffineTransform(a11, a21, a12, a22, a13, a23);
    }
//    private static AffineTransform calculateAffineTransformLeastSquare(double[][] src, double[][] dest){
//
//        // Create matrices A and b
//        int numPoints = src.length;
//        double[][] A = new double[numPoints * 2][6];
//        double[] b = new double[numPoints * 2];
//
//        for (int i = 0; i < numPoints; i++) {
//            double x = src[i][0];
//            double y = src[i][1];
//            double xPrime = dest[i][0];
//            double yPrime = dest[i][1];
//
//            // Fill A matrix
//            A[2 * i] = new double[] {x, y, 1, 0, 0, 0};
//            A[2 * i + 1] = new double[] {0, 0, 0, x, y, 1};
//
//            // Fill b vector
//            b[2 * i] = xPrime;
//            b[2 * i + 1] = yPrime;
//        }
//
//        // Solve for T using least squares
//        RealMatrix matrixA = new Array2DRowRealMatrix(A);
//        RealVector vectorB = new ArrayRealVector(b);
//        DecompositionSolver solver = new SingularValueDecomposition(matrixA).getSolver();
//        RealVector solution = solver.solve(vectorB);
//
//        // Extract and display transformation matrix
//        double[] t = solution.toArray();
//        return new AffineTransform(t[0], t[3], t[1], t[4], t[2], t[5]);
//    }
}