package test;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.video.KalmanFilter;

/**
 * Kalman.java TODO:
 * 
 * @author Kim Dinh Son Email:sonkdbk@gmail.com
 */

public class Kalman extends KalmanFilter {
	private KalmanFilter kalman;
	private Mat measurement;
	private Point LastResult;
	private double deltatime;

	public void init() {

	}

	public Kalman(Point pt, double dt, double Accel_noise_mag) {
		kalman = new KalmanFilter(4, 2, 0, CvType.CV_32F);
		deltatime = dt;

		Mat transitionMatrix = new Mat(4, 4, CvType.CV_32F, new Scalar(0));
		float[] tM = { 
			    1, 0, 1, 0, 
			    0, 1, 0, 1,
			    0, 0, 1, 0,
			    0, 0, 0, 1 } ;
		transitionMatrix.put(0,0,tM);

		kalman.set_transitionMatrix(transitionMatrix);
		measurement = new Mat(2, 1, CvType.CV_32F, new Scalar(0));

		// init
		LastResult = pt;
		Mat statePre = new MatOfFloat(4, 1); // Toa do (x,y), van toc (0,0)
		statePre.put(0, 0, pt.x);
		statePre.put(1, 0, pt.y);
		statePre.put(2, 0, 0);
		statePre.put(3, 0, 0);
		kalman.set_statePre(statePre);

		Mat statePost = new MatOfFloat(2, 1);
		statePost.put(0, 0, pt.x);
		statePost.put(1, 0, pt.y);
		kalman.set_statePost(statePost);

		Mat processNoiseCov = new Mat(4, 4, CvType.CV_32F);
		float[] dTime = { (float) (Math.pow(deltatime, 4.0) / 4.0), 0,
				(float) (Math.pow(deltatime, 3.0) / 2.0), 0, 0,
				(float) (Math.pow(deltatime, 4.0) / 4.0), 0,
				(float) (Math.pow(deltatime, 3.0) / 2.0),
				(float) (Math.pow(deltatime, 3.0) / 2.0), 0,
				(float) Math.pow(deltatime, 2.0), 0, 0,
				(float) (Math.pow(deltatime, 3.0) / 2.0), 0,
				(float) Math.pow(deltatime, 2.0) };
		processNoiseCov.put(0, 0, dTime);

		processNoiseCov = processNoiseCov.mul(processNoiseCov, Accel_noise_mag);
		kalman.set_processNoiseCov(processNoiseCov);

		kalman.set_measurementNoiseCov(new Mat(2, 2, CvType.CV_32F, new Scalar(
				0.1)));
		kalman.set_errorCovPost(new Mat(4, 4, CvType.CV_32F, new Scalar(0.1)));
	}

	public Point getPrediction() {
		Mat prediction = kalman.predict();
		LastResult = new Point(prediction.get(0, 0)[0], prediction.get(1, 0)[0]);
		return LastResult;
	}

	public Point update(Point p, boolean dataCorrect) {
		measurement = new MatOfFloat(2, 1, CvType.CV_32F);
		if (!dataCorrect) {
			measurement.put(0, 0, LastResult.x);
			measurement.put(1, 0, LastResult.y);
		} else {
			measurement.put(0, 0, p.x);
			measurement.put(1, 0, p.y);
		}
		// Correction
		Mat estimated = kalman.correct(measurement);
		LastResult.x = estimated.get(0, 0)[0];
		LastResult.y = estimated.get(1, 0)[0];
		return LastResult;
	}
	
	public Point correction(){
		Mat estimated = kalman.correct(measurement);
		LastResult.x = estimated.get(0, 0)[0];
		LastResult.y = estimated.get(1, 0)[0];
		return LastResult;
	}

	public void setMeasurement(MatOfFloat measurement) {
		this.measurement = measurement;
	}

	public Mat getMeasurement() {
		return this.measurement;
	}

	/**
	 * @return the deltatime
	 */
	public double getDeltatime() {
		return deltatime;
	}

	/**
	 * @param deltatime
	 *            the deltatime to set
	 */
	public void setDeltatime(double deltatime) {
		this.deltatime = deltatime;
	}

	/**
	 * @return the lastResult
	 */
	public Point getLastResult() {
		return LastResult;
	}

	/**
	 * @param lastResult
	 *            the lastResult to set
	 */
	public void setLastResult(Point lastResult) {
		LastResult = lastResult;
	}
}