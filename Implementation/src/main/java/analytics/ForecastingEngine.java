package analytics;

import model.ForecastResult;
import model.TemporalMetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Time-series forecasting and evaluation engine.
 * Implements Moving Average, Historical Average, Simple Exponential Smoothing (SES),
 * and First-Difference Drift Model (ARIMA(1,1,0) style).
 * Calculates MAE and RMSE performance metrics.
 */
public class ForecastingEngine {

    /**
     * Runs and compares all four time-series forecasting techniques against the historical series.
     */
    public List<ForecastResult> compareAllModels(List<TemporalMetric> metrics) {
        if (metrics == null || metrics.size() < 4) {
            return Collections.emptyList();
        }

        List<String> periods = new ArrayList<>();
        List<Double> actuals = new ArrayList<>();
        for (TemporalMetric m : metrics) {
            periods.add(m.getYearMonthStr());
            actuals.add((double) m.getTotalVulnerabilities());
        }

        List<ForecastResult> results = new ArrayList<>();
        results.add(forecastMovingAverage(periods, actuals, 3));
        results.add(forecastHistoricalAverage(periods, actuals));
        results.add(forecastExponentialSmoothing(periods, actuals, 0.3));
        results.add(forecastArimaFirstDifference(periods, actuals));

        return results;
    }

    /**
     * 1. Moving Average (Trailing Window k=3)
     */
    public ForecastResult forecastMovingAverage(List<String> periods, List<Double> actuals, int k) {
        int n = actuals.size();
        List<Double> forecasts = new ArrayList<>(Collections.nCopies(n, 0.0));

        double sumAbsErr = 0.0;
        double sumSqErr = 0.0;
        int evalCount = 0;

        for (int t = 0; t < n; t++) {
            if (t < k) {
                // Expanding mean for initial warm-up period
                double sum = 0;
                for (int j = 0; j <= t; j++) sum += actuals.get(j);
                forecasts.set(t, sum / (t + 1));
            } else {
                double windowSum = 0;
                for (int j = t - k; j < t; j++) {
                    windowSum += actuals.get(j);
                }
                double f = windowSum / k;
                forecasts.set(t, f);

                double err = actuals.get(t) - f;
                sumAbsErr += Math.abs(err);
                sumSqErr += (err * err);
                evalCount++;
            }
        }

        double mae = evalCount > 0 ? (sumAbsErr / evalCount) : 0.0;
        double rmse = evalCount > 0 ? Math.sqrt(sumSqErr / evalCount) : 0.0;

        // Next period forecast
        double lastWindowSum = 0;
        for (int j = n - k; j < n; j++) {
            lastWindowSum += actuals.get(j);
        }
        double nextPeriod = lastWindowSum / k;

        return new ForecastResult("Moving Average (k=3)", periods, actuals, forecasts, mae, rmse,
                nextPeriod, "Simple trailing 3-month rolling average");
    }

    /**
     * 2. Historical Average (Cumulative Mean)
     */
    public ForecastResult forecastHistoricalAverage(List<String> periods, List<Double> actuals) {
        int n = actuals.size();
        List<Double> forecasts = new ArrayList<>(Collections.nCopies(n, 0.0));

        double sumAbsErr = 0.0;
        double sumSqErr = 0.0;
        int evalCount = 0;
        double runningSum = 0.0;

        for (int t = 0; t < n; t++) {
            if (t == 0) {
                forecasts.set(0, actuals.get(0));
            } else {
                double f = runningSum / t;
                forecasts.set(t, f);

                double err = actuals.get(t) - f;
                sumAbsErr += Math.abs(err);
                sumSqErr += (err * err);
                evalCount++;
            }
            runningSum += actuals.get(t);
        }

        double mae = evalCount > 0 ? (sumAbsErr / evalCount) : 0.0;
        double rmse = evalCount > 0 ? Math.sqrt(sumSqErr / evalCount) : 0.0;
        double nextPeriod = runningSum / n;

        return new ForecastResult("Historical Average", periods, actuals, forecasts, mae, rmse,
                nextPeriod, "Cumulative average of all historical observations");
    }

    /**
     * 3. Simple Exponential Smoothing (SES, alpha=0.3)
     */
    public ForecastResult forecastExponentialSmoothing(List<String> periods, List<Double> actuals, double alpha) {
        int n = actuals.size();
        List<Double> forecasts = new ArrayList<>(Collections.nCopies(n, 0.0));

        double sumAbsErr = 0.0;
        double sumSqErr = 0.0;
        int evalCount = 0;

        double s = actuals.get(0);
        forecasts.set(0, s);

        for (int t = 1; t < n; t++) {
            forecasts.set(t, s);

            double err = actuals.get(t) - s;
            sumAbsErr += Math.abs(err);
            sumSqErr += (err * err);
            evalCount++;

            s = (alpha * actuals.get(t)) + ((1.0 - alpha) * s);
        }

        double mae = evalCount > 0 ? (sumAbsErr / evalCount) : 0.0;
        double rmse = evalCount > 0 ? Math.sqrt(sumSqErr / evalCount) : 0.0;
        double nextPeriod = s;

        return new ForecastResult("Exponential Smoothing (α=0.3)", periods, actuals, forecasts, mae, rmse,
                nextPeriod, "Single exponential smoothing with recent observation weighting");
    }

    /**
     * 4. First-Difference Drift Model (ARIMA(1,1,0) approximation)
     */
    public ForecastResult forecastArimaFirstDifference(List<String> periods, List<Double> actuals) {
        int n = actuals.size();
        List<Double> forecasts = new ArrayList<>(Collections.nCopies(n, 0.0));

        double sumAbsErr = 0.0;
        double sumSqErr = 0.0;
        int evalCount = 0;

        // Warm-up initial periods
        forecasts.set(0, actuals.get(0));
        if (n > 1) {
            forecasts.set(1, actuals.get(0));
        }

        // For t >= 2, forecast y_t = y_{t-1} + phi * (y_{t-1} - y_{t-2})
        double phi = 0.4; // Autoregressive momentum parameter
        for (int t = 2; t < n; t++) {
            double diff = actuals.get(t - 1) - actuals.get(t - 2);
            double f = Math.max(0.0, actuals.get(t - 1) + (phi * diff));
            forecasts.set(t, f);

            double err = actuals.get(t) - f;
            sumAbsErr += Math.abs(err);
            sumSqErr += (err * err);
            evalCount++;
        }

        double mae = evalCount > 0 ? (sumAbsErr / evalCount) : 0.0;
        double rmse = evalCount > 0 ? Math.sqrt(sumSqErr / evalCount) : 0.0;

        double lastDiff = actuals.get(n - 1) - actuals.get(n - 2);
        double nextPeriod = Math.max(0.0, actuals.get(n - 1) + (phi * lastDiff));

        return new ForecastResult("ARIMA(1,1,0) Drift", periods, actuals, forecasts, mae, rmse,
                nextPeriod, "Autoregressive difference model with trend momentum");
    }
}
